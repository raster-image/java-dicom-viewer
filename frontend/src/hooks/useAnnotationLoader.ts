import { useEffect } from 'react';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import type { Annotation } from '../types';

// Delay in milliseconds to wait for viewport to be ready before restoring annotations
// This ensures the rendering engine and viewports are fully initialized
const VIEWPORT_READY_DELAY_MS = 500;

interface UseAnnotationLoaderProps {
  studyInstanceUid: string;
  enabled?: boolean;
  toolGroupId?: string;
}

/**
 * Hook to load and restore annotations from backend when opening a study.
 * Restores saved annotations to Cornerstone tool state for visualization.
 */
export function useAnnotationLoader({
  studyInstanceUid,
  enabled = true,
  toolGroupId = 'default',
}: UseAnnotationLoaderProps) {
  // Fetch annotations for the study
  const { data: annotationsData, isLoading, error } = useQuery({
    queryKey: ['annotations', studyInstanceUid],
    queryFn: () => apiClient.getAnnotationsByStudy(studyInstanceUid),
    enabled: enabled && !!studyInstanceUid,
  });

  // Restore annotations to Cornerstone when data is loaded
  useEffect(() => {
    if (!annotationsData?.annotations || annotationsData.annotations.length === 0) {
      return;
    }

    // Wait for the viewport to be ready before restoring annotations
    const restoreAnnotations = () => {
      try {
        const annotations = annotationsData.annotations as Annotation[];

        annotations.forEach((annotation) => {
          // Skip if annotation is not visible
          if (!annotation.visible) return;

          // Build the image ID from annotation metadata
          const imageId = buildImageId(
            annotation.studyInstanceUid,
            annotation.seriesInstanceUid,
            annotation.sopInstanceUid,
            annotation.frameIndex || 0
          );

          // Create annotation data structure for Cornerstone
          const annotationData = createAnnotationFromSaved(annotation, imageId);

          if (annotationData) {
            // Add annotation to Cornerstone tool state
            try {
              cornerstoneTools.annotation.state.addAnnotation(annotationData, toolGroupId);
            } catch (err) {
              console.warn(`Failed to restore annotation ${annotation.id}:`, err);
            }
          }
        });

        console.log(`Restored ${annotations.length} annotations for study ${studyInstanceUid}`);
      } catch (err) {
        console.error('Failed to restore annotations:', err);
      }
    };

    // Delay restoration to ensure viewport is ready
    const timer = setTimeout(restoreAnnotations, VIEWPORT_READY_DELAY_MS);

    return () => clearTimeout(timer);
  }, [annotationsData, studyInstanceUid, toolGroupId]);

  return {
    annotations: annotationsData?.annotations || [],
    count: annotationsData?.count || 0,
    isLoading,
    error,
  };
}

/**
 * Build WADO-RS image ID from DICOM UIDs
 */
function buildImageId(
  studyUid: string,
  seriesUid: string,
  sopUid: string,
  frameIndex: number
): string {
  const frame = frameIndex + 1; // WADO uses 1-based indexing
  return `wadors:/api/wado/studies/${studyUid}/series/${seriesUid}/instances/${sopUid}/frames/${frame}`;
}

/**
 * Create Cornerstone annotation data from saved annotation
 */
function createAnnotationFromSaved(
  annotation: Annotation,
  imageId: string
): any {
  try {
    // Create handles from points
    const handles: Record<string, any> = {};

    if (annotation.points && annotation.points.length > 0) {
      const points = annotation.points;

      // Map points to handles based on tool type
      if (annotation.toolName === 'ArrowAnnotate') {
        // Arrow requires start and end points
        if (points.length >= 2) {
          handles.points = [
            [points[0].x, points[0].y, points[0].z || 0],
            [points[1].x, points[1].y, points[1].z || 0],
          ];
        }
      } else if (annotation.toolName === 'PlanarFreehandROI') {
        // Freehand polyline uses array of points
        handles.points = points.map(p => [p.x, p.y, p.z || 0]);
      }
    }

    if (Object.keys(handles).length === 0) {
      return null;
    }

    // Create annotation structure
    const annotationObj: any = {
      annotationUID: `restored-${annotation.id}`,
      metadata: {
        toolName: annotation.toolName,
        referencedImageId: imageId,
        FrameOfReferenceUID: 'unknown', // Will be set by Cornerstone
      },
      data: {
        handles,
        text: annotation.text || '',
        label: annotation.text || '',
      },
      highlighted: false,
      invalidated: false,
      isLocked: annotation.locked || false,
      isVisible: annotation.visible,
    };

    // Add style if available
    if (annotation.style) {
      annotationObj.styles = annotation.style;
    }

    // Add color if available
    if (annotation.color) {
      if (!annotationObj.styles) {
        annotationObj.styles = {};
      }
      annotationObj.styles.color = annotation.color;
    }

    return annotationObj;
  } catch (err) {
    console.error('Failed to create annotation from saved data:', err);
    return null;
  }
}
