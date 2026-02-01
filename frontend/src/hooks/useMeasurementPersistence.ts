import { useCallback, useRef, useEffect } from 'react';
import { eventTarget } from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { apiClient } from '../services/api';
import type { Measurement, MeasurementType, Point3D, ROIStats } from '../types';

// Map Cornerstone tool names to measurement types
// These are quantitative measurement tools that produce numerical results
const MEASUREMENT_TOOL_MAP: Record<string, MeasurementType> = {
  Length: 'LENGTH',
  Angle: 'ANGLE',
  CobbAngle: 'COBB_ANGLE',
  RectangleROI: 'RECTANGLE_ROI',
  EllipticalROI: 'ELLIPSE_ROI',
  Probe: 'PROBE',
};

// Annotation tools are handled by useAnnotationPersistence
// Note: PlanarFreehandROI can be used for both freehand measurements and annotations
// When used as TextMarker in the toolbar, it's treated as an annotation

interface UseMeasurementPersistenceProps {
  studyInstanceUid: string;
  seriesInstanceUid: string | null;
  onMeasurementSaved?: (measurement: Measurement) => void;
  onError?: (error: Error) => void;
}

/**
 * Hook to handle measurement persistence to backend.
 * Listens to Cornerstone annotation events and saves measurements automatically.
 */
export function useMeasurementPersistence({
  studyInstanceUid,
  seriesInstanceUid,
  onMeasurementSaved,
  onError,
}: UseMeasurementPersistenceProps) {
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
      // For tools with array of points (e.g., ROI tools)
      for (const point of handles.points) {
        const p = point as number[];
        points.push({ x: p[0], y: p[1], z: p[2] });
      }
    } else {
      // For tools with named points (e.g., Length, Angle)
      for (const [key, value] of Object.entries(handles)) {
        if (key === 'textBox' || key === 'activeHandleIndex') continue;
        if (Array.isArray(value) && value.length >= 2) {
          points.push({ x: value[0], y: value[1], z: value[2] || 0 });
        }
      }
    }

    return points;
  }, []);

  // Extract ROI statistics if available
  const extractRoiStats = useCallback((annotationData: unknown): ROIStats | undefined => {
    const data = annotationData as Record<string, unknown>;
    const cachedStats = data.cachedStats as Record<string, unknown> | undefined;

    if (!cachedStats) return undefined;

    // Find the first valid stats entry (keyed by image ID)
    for (const value of Object.values(cachedStats)) {
      const stats = value as Record<string, unknown>;
      if (stats && typeof stats.mean === 'number') {
        return {
          mean: stats.mean as number,
          stdDev: ((stats.stdDev || stats.Std) as number) || 0,
          min: (stats.min as number) || 0,
          max: (stats.max as number) || 0,
          area: ((stats.area || stats.areaUnit) as number) || 0,
          perimeter: stats.perimeter as number | undefined,
          pixelCount: stats.pixelCount as number | undefined,
        };
      }
    }

    return undefined;
  }, []);

  // Save measurement to backend
  const saveMeasurement = useCallback(
    async (annotation: unknown, sopInstanceUid: string, frameIndex = 0) => {
      if (!studyInstanceUid || !seriesInstanceUid) return;

      const annotationObj = annotation as Record<string, unknown>;
      const annotationUID = annotationObj.annotationUID as string;
      const metadata = annotationObj.metadata as Record<string, unknown> | undefined;
      const toolName = metadata?.toolName as string;

      // Skip if already saved or not a measurement tool
      if (!annotationUID || savedAnnotations.current.has(annotationUID)) return;
      if (!toolName || !MEASUREMENT_TOOL_MAP[toolName]) return;

      const data = annotationObj.data as Record<string, unknown>;
      if (!data) return;

      try {
        const points = extractPoints(data);
        const roiStats = extractRoiStats(data);

        // Get measurement value and unit
        let value: number | undefined;
        let unit: string | undefined;

        const cachedStats = data.cachedStats as Record<string, unknown> | undefined;
        if (cachedStats) {
          for (const stats of Object.values(cachedStats)) {
            const s = stats as Record<string, unknown>;
            if (s && typeof s.length === 'number') {
              value = s.length as number;
              unit = 'mm';
              break;
            }
            if (s && typeof s.angle === 'number') {
              value = s.angle as number;
              unit = '°';
              break;
            }
            if (s && typeof s.area === 'number') {
              value = s.area as number;
              unit = 'mm²';
              break;
            }
          }
        }

        const measurementData: Omit<Measurement, 'id' | 'createdAt' | 'updatedAt'> = {
          studyInstanceUid,
          seriesInstanceUid,
          sopInstanceUid,
          frameIndex,
          measurementType: MEASUREMENT_TOOL_MAP[toolName],
          toolName,
          value,
          unit,
          points,
          roiStats,
          visible: true,
        };

        const savedMeasurement = await apiClient.createMeasurement(measurementData);
        savedAnnotations.current.add(annotationUID);

        onMeasurementSaved?.(savedMeasurement);
      } catch (error) {
        console.error('Failed to save measurement:', error);
        onError?.(error as Error);
      }
    },
    [
      studyInstanceUid,
      seriesInstanceUid,
      extractPoints,
      extractRoiStats,
      onMeasurementSaved,
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

      saveMeasurement(annotation, sopInstanceUid, frameIndex);
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
  }, [saveMeasurement]);

  // Clear saved annotations when study changes
  useEffect(() => {
    savedAnnotations.current.clear();
  }, [studyInstanceUid]);

  return {
    savedCount: savedAnnotations.current.size,
  };
}
