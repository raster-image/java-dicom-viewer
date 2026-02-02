import { useEffect } from 'react';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import type { Measurement } from '../types';

// Delay in milliseconds to wait for viewport to be ready before restoring measurements
// This ensures the rendering engine and viewports are fully initialized
const VIEWPORT_READY_DELAY_MS = 500;

interface UseMeasurementLoaderProps {
  studyInstanceUid: string;
  enabled?: boolean;
  toolGroupId?: string;
}

/**
 * Hook to load and restore measurements from backend when opening a study.
 * Restores saved measurements to Cornerstone tool state for visualization.
 */
export function useMeasurementLoader({
  studyInstanceUid,
  enabled = true,
  toolGroupId = 'default',
}: UseMeasurementLoaderProps) {
  // Fetch measurements for the study
  const { data: measurementsData, isLoading, error } = useQuery({
    queryKey: ['measurements', studyInstanceUid],
    queryFn: () => apiClient.getMeasurementsByStudy(studyInstanceUid),
    enabled: enabled && !!studyInstanceUid,
  });

  // Restore measurements to Cornerstone when data is loaded
  useEffect(() => {
    if (!measurementsData?.measurements || measurementsData.measurements.length === 0) {
      return;
    }

    // Wait for the viewport to be ready before restoring measurements
    const restoreMeasurements = () => {
      try {
        const measurements = measurementsData.measurements as Measurement[];

        measurements.forEach((measurement) => {
          // Skip if measurement is not visible
          if (!measurement.visible) return;

          // Build the image ID from measurement metadata
          const imageId = buildImageId(
            measurement.studyInstanceUid,
            measurement.seriesInstanceUid,
            measurement.sopInstanceUid,
            measurement.frameIndex || 0
          );

          // Create annotation data structure for Cornerstone
          const annotationData = createAnnotationFromMeasurement(measurement, imageId);

          if (annotationData) {
            // Add annotation to Cornerstone tool state
            const ToolClass = getToolClass(measurement.toolName);
            if (ToolClass) {
              try {
                // Use the tool's state manager to add the annotation
                cornerstoneTools.annotation.state.addAnnotation(annotationData, toolGroupId);
              } catch (err) {
                console.warn(`Failed to restore measurement ${measurement.id}:`, err);
              }
            }
          }
        });

        console.log(`Restored ${measurements.length} measurements for study ${studyInstanceUid}`);
      } catch (err) {
        console.error('Failed to restore measurements:', err);
      }
    };

    // Delay restoration to ensure viewport is ready
    const timer = setTimeout(restoreMeasurements, VIEWPORT_READY_DELAY_MS);

    return () => clearTimeout(timer);
  }, [measurementsData, studyInstanceUid, toolGroupId]);

  return {
    measurements: measurementsData?.measurements || [],
    count: measurementsData?.count || 0,
    isLoading,
    error,
  };
}

/**
 * Build WADO-RS image ID from DICOM UIDs.
 * 
 * @param studyUid - Study Instance UID
 * @param seriesUid - Series Instance UID
 * @param sopUid - SOP Instance UID
 * @param frameIndex - Zero-based frame index (converted to 1-based for WADO-RS)
 * @returns WADO-RS URL format image ID
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

// Type for Cornerstone tool classes
type ToolClass = typeof cornerstoneTools.LengthTool 
  | typeof cornerstoneTools.AngleTool 
  | typeof cornerstoneTools.CobbAngleTool 
  | typeof cornerstoneTools.RectangleROITool 
  | typeof cornerstoneTools.EllipticalROITool 
  | typeof cornerstoneTools.ProbeTool;

/**
 * Get Cornerstone tool class by name
 */
function getToolClass(toolName: string): ToolClass | undefined {
  const toolMap: Record<string, ToolClass> = {
    Length: cornerstoneTools.LengthTool,
    Angle: cornerstoneTools.AngleTool,
    CobbAngle: cornerstoneTools.CobbAngleTool,
    RectangleROI: cornerstoneTools.RectangleROITool,
    EllipticalROI: cornerstoneTools.EllipticalROITool,
    Probe: cornerstoneTools.ProbeTool,
  };

  return toolMap[toolName];
}

/**
 * Create Cornerstone annotation data from saved measurement
 */
function createAnnotationFromMeasurement(
  measurement: Measurement,
  imageId: string
): any {
  try {
    // Create handles from points
    const handles: Record<string, any> = {};

    if (measurement.points && measurement.points.length > 0) {
      const points = measurement.points;

      // Map points to handles based on tool type
      if (measurement.toolName === 'Length') {
        if (points.length >= 2) {
          handles.points = [
            [points[0].x, points[0].y, points[0].z || 0],
            [points[1].x, points[1].y, points[1].z || 0],
          ];
        }
      } else if (measurement.toolName === 'Angle' || measurement.toolName === 'CobbAngle') {
        if (points.length >= 3) {
          handles.points = points.map(p => [p.x, p.y, p.z || 0]);
        }
      } else if (measurement.toolName === 'RectangleROI' || measurement.toolName === 'EllipticalROI') {
        if (points.length >= 2) {
          handles.points = [
            [points[0].x, points[0].y, points[0].z || 0],
            [points[1].x, points[1].y, points[1].z || 0],
          ];
        }
      } else if (measurement.toolName === 'Probe') {
        if (points.length >= 1) {
          handles.points = [[points[0].x, points[0].y, points[0].z || 0]];
        }
      }
    }

    if (Object.keys(handles).length === 0) {
      return null;
    }

    // Create annotation structure
    const annotation: any = {
      annotationUID: `restored-${measurement.id}`,
      metadata: {
        toolName: measurement.toolName,
        referencedImageId: imageId,
        FrameOfReferenceUID: 'unknown', // Will be set by Cornerstone
      },
      data: {
        handles,
        label: measurement.label || '',
        cachedStats: {},
      },
      highlighted: false,
      invalidated: false,
      isLocked: false,
      isVisible: measurement.visible,
    };

    // Add cached stats for ROI measurements
    if (measurement.roiStats) {
      annotation.data.cachedStats[imageId] = {
        mean: measurement.roiStats.mean,
        stdDev: measurement.roiStats.stdDev,
        min: measurement.roiStats.min,
        max: measurement.roiStats.max,
        area: measurement.roiStats.area,
      };
    }

    // Add measurement value to cached stats
    if (measurement.value !== undefined) {
      if (!annotation.data.cachedStats[imageId]) {
        annotation.data.cachedStats[imageId] = {};
      }

      if (measurement.unit === 'mm') {
        annotation.data.cachedStats[imageId].length = measurement.value;
      } else if (measurement.unit === '°') {
        annotation.data.cachedStats[imageId].angle = measurement.value;
      } else if (measurement.unit === 'mm²') {
        annotation.data.cachedStats[imageId].area = measurement.value;
      }
    }

    return annotation;
  } catch (err) {
    console.error('Failed to create annotation from measurement:', err);
    return null;
  }
}
