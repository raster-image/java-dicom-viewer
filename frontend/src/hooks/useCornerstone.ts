import { useEffect, useState } from 'react';
import { RenderingEngine, Enums, init as csInit } from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
// @ts-expect-error - dicom-image-loader doesn't have type definitions
import cornerstoneDICOMImageLoader from '@cornerstonejs/dicom-image-loader';
import dicomParser from 'dicom-parser';

const {
  // Navigation tools
  WindowLevelTool,
  PanTool,
  ZoomTool,
  StackScrollTool,
  // Phase 3: Measurement tools
  LengthTool,
  AngleTool,
  CobbAngleTool,
  RectangleROITool,
  EllipticalROITool,
  ProbeTool,
  // Phase 3: Annotation tools
  ArrowAnnotateTool,
  PlanarFreehandROITool,
  // Tool management
  ToolGroupManager,
  Enums: csToolsEnums,
} = cornerstoneTools;

interface CornerstoneState {
  isInitialized: boolean;
  error: Error | null;
}

let isGloballyInitialized = false;

export function useCornerstone(): CornerstoneState {
  const [state, setState] = useState<CornerstoneState>({
    isInitialized: isGloballyInitialized,
    error: null,
  });

  useEffect(() => {
    async function initializeCornerstone() {
      if (isGloballyInitialized) {
        setState({ isInitialized: true, error: null });
        return;
      }

      try {
        // Initialize Cornerstone Core
        await csInit();

        // Configure DICOM image loader
        cornerstoneDICOMImageLoader.external.cornerstone = {
          metaData: {
            get: (_type: string, _imageId: string) => {
              // Return undefined for now - metadata handling will be added
              return undefined;
            },
          },
        };
        cornerstoneDICOMImageLoader.external.dicomParser = dicomParser;

        // Initialize web workers for image decoding
        const config = {
          maxWebWorkers: navigator.hardwareConcurrency || 4,
          startWebWorkersOnDemand: true,
          taskConfiguration: {
            decodeTask: {
              initializeCodecsOnStartup: false,
              strict: false,
            },
          },
        };
        cornerstoneDICOMImageLoader.webWorkerManager.initialize(config);

        // Initialize Cornerstone Tools
        cornerstoneTools.init();

        // Register navigation tools globally
        cornerstoneTools.addTool(WindowLevelTool);
        cornerstoneTools.addTool(PanTool);
        cornerstoneTools.addTool(ZoomTool);
        cornerstoneTools.addTool(StackScrollTool);

        // Phase 3: Register measurement tools globally
        cornerstoneTools.addTool(LengthTool);
        cornerstoneTools.addTool(AngleTool);
        cornerstoneTools.addTool(CobbAngleTool);
        cornerstoneTools.addTool(RectangleROITool);
        cornerstoneTools.addTool(EllipticalROITool);
        cornerstoneTools.addTool(ProbeTool);

        // Phase 3: Register annotation tools globally
        cornerstoneTools.addTool(ArrowAnnotateTool);
        cornerstoneTools.addTool(PlanarFreehandROITool);

        isGloballyInitialized = true;
        setState({ isInitialized: true, error: null });
        console.log('Cornerstone initialized successfully with Phase 3 tools');
      } catch (error) {
        console.error('Failed to initialize Cornerstone:', error);
        setState({ isInitialized: false, error: error as Error });
      }
    }

    initializeCornerstone();
  }, []);

  return state;
}

export { Enums, RenderingEngine, ToolGroupManager, csToolsEnums };
