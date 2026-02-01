import { useCallback, useRef, useEffect } from 'react';
import { eventTarget } from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { apiClient } from '../services/api';
import type { Annotation, AnnotationType, Point3D, AnnotationStyle } from '../types';

// Map Cornerstone tool names to annotation types
const TOOL_TYPE_MAP: Record<string, AnnotationType> = {
  ArrowAnnotate: 'ARROW',
  TextMarker: 'TEXT',
  PlanarFreehandROI: 'POLYLINE',
};

interface UseAnnotationPersistenceProps {
  studyInstanceUid: string;
  seriesInstanceUid: string | null;
  onAnnotationSaved?: (annotation: Annotation) => void;
  onError?: (error: Error) => void;
}

/**
 * Hook to handle annotation persistence to backend.
 * Listens to Cornerstone annotation events and saves annotations automatically.
 */
export function useAnnotationPersistence({
  studyInstanceUid,
  seriesInstanceUid,
  onAnnotationSaved,
  onError,
}: UseAnnotationPersistenceProps) {
  // Keep track of saved annotation UIDs to avoid duplicates
  const savedAnnotations = useRef<Set<string>>(new Set());

  // Extract points from Cornerstone annotation data
  const extractPoints = useCallback((annotationData: unknown): Point3D[] => {
    const data = annotationData as Record<string, unknown>;
    const handles = data.handles as Record<string, unknown> | undefined;

    if (!handles) return [];

    const points: Point3D[] = [];

    // Handle different tool types
    if (handles.points && Array.isArray(handles.points)) {
      for (const point of handles.points) {
        const p = point as number[];
        points.push({ x: p[0], y: p[1], z: p[2] });
      }
    } else {
      // For tools with named points (e.g., Arrow - start/end)
      for (const [key, value] of Object.entries(handles)) {
        if (key === 'textBox' || key === 'activeHandleIndex') continue;
        if (Array.isArray(value) && value.length >= 2) {
          points.push({ x: value[0], y: value[1], z: value[2] || 0 });
        }
      }
    }

    return points;
  }, []);

  // Extract text from annotation
  const extractText = useCallback((annotationData: unknown): string | undefined => {
    const data = annotationData as Record<string, unknown>;

    // Try different locations where text might be stored
    if (data.text) return data.text as string;

    const handles = data.handles as Record<string, unknown> | undefined;
    if (handles?.textBox) {
      const textBox = handles.textBox as Record<string, unknown>;
      if (textBox.text) return textBox.text as string;
    }

    // For ArrowAnnotate, text is in data.text
    if (data.label) return data.label as string;

    return undefined;
  }, []);

  // Extract style from annotation
  const extractStyle = useCallback((annotationObj: unknown): AnnotationStyle | undefined => {
    const annotation = annotationObj as Record<string, unknown>;
    const styles = annotation.styles as Record<string, unknown> | undefined;

    if (!styles) return undefined;

    return {
      lineWidth: styles.lineWidth as number | undefined,
      lineDash: styles.lineDash as number[] | undefined,
      shadow: styles.shadow as boolean | undefined,
      textBox: styles.textBox as AnnotationStyle['textBox'],
    };
  }, []);

  // Save annotation to backend
  const saveAnnotation = useCallback(
    async (annotation: unknown, sopInstanceUid: string, frameIndex = 0) => {
      if (!studyInstanceUid || !seriesInstanceUid) return;

      const annotationObj = annotation as Record<string, unknown>;
      const annotationUID = annotationObj.annotationUID as string;
      const metadata = annotationObj.metadata as Record<string, unknown> | undefined;
      const toolName = metadata?.toolName as string;

      // Skip if already saved or not an annotation tool
      if (!annotationUID || savedAnnotations.current.has(annotationUID)) return;
      if (!toolName || !TOOL_TYPE_MAP[toolName]) return;

      const data = annotationObj.data as Record<string, unknown>;
      if (!data) return;

      try {
        const points = extractPoints(data);
        const text = extractText(data);
        const style = extractStyle(annotationObj);

        const annotationData: Omit<Annotation, 'id' | 'createdAt' | 'updatedAt'> = {
          studyInstanceUid,
          seriesInstanceUid,
          sopInstanceUid,
          frameIndex,
          annotationType: TOOL_TYPE_MAP[toolName],
          toolName,
          text,
          points,
          style,
          visible: true,
          locked: false,
        };

        const savedAnnotation = await apiClient.createAnnotation(annotationData);
        savedAnnotations.current.add(annotationUID);

        onAnnotationSaved?.(savedAnnotation);
      } catch (error) {
        console.error('Failed to save annotation:', error);
        onError?.(error as Error);
      }
    },
    [
      studyInstanceUid,
      seriesInstanceUid,
      extractPoints,
      extractText,
      extractStyle,
      onAnnotationSaved,
      onError,
    ]
  );

  // Set up event listener for annotation completed events
  useEffect(() => {
    // Handler for when annotation is completed
    const handleAnnotationCompleted = (event: Event) => {
      const customEvent = event as CustomEvent;
      const { annotation } = customEvent.detail || {};

      if (!annotation) return;

      const annotationObj = annotation as Record<string, unknown>;
      const metadata = annotationObj.metadata as Record<string, unknown> | undefined;
      const toolName = metadata?.toolName as string;

      // Only process annotation tools, not measurement tools
      if (!toolName || !TOOL_TYPE_MAP[toolName]) return;

      // Extract SOP Instance UID from the referenced image ID
      const referencedImageId = metadata?.referencedImageId as string;
      if (!referencedImageId) return;

      // Parse SOP Instance UID from WADO-RS URL
      // Format: wadors:/api/wado/studies/{study}/series/{series}/instances/{sop}/frames/1
      const sopMatch = referencedImageId.match(/instances\/([^/]+)/);
      const sopInstanceUid = sopMatch ? sopMatch[1] : '';

      // Extract frame index
      const frameMatch = referencedImageId.match(/frames\/(\d+)/);
      const frameIndex = frameMatch ? parseInt(frameMatch[1], 10) - 1 : 0;

      saveAnnotation(annotation, sopInstanceUid, frameIndex);
    };

    // Listen for annotation completed event
    eventTarget.addEventListener(
      cornerstoneTools.Enums.Events.ANNOTATION_COMPLETED,
      handleAnnotationCompleted
    );

    return () => {
      eventTarget.removeEventListener(
        cornerstoneTools.Enums.Events.ANNOTATION_COMPLETED,
        handleAnnotationCompleted
      );
    };
  }, [saveAnnotation]);

  // Clear saved annotations when study changes
  useEffect(() => {
    savedAnnotations.current.clear();
  }, [studyInstanceUid]);

  return {
    savedCount: savedAnnotations.current.size,
  };
}
