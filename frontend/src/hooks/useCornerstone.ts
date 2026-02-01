import { useEffect, useState } from 'react'
import {
  RenderingEngine,
  Enums,
  init as csInit,
} from '@cornerstonejs/core'
import * as cornerstoneTools from '@cornerstonejs/tools'
// @ts-ignore - dicom-image-loader doesn't have type definitions
import cornerstoneDICOMImageLoader from '@cornerstonejs/dicom-image-loader'
import dicomParser from 'dicom-parser'

const {
  WindowLevelTool,
  PanTool,
  ZoomTool,
  StackScrollTool,
  ToolGroupManager,
  Enums: csToolsEnums,
} = cornerstoneTools

interface CornerstoneState {
  isInitialized: boolean
  error: Error | null
}

let isGloballyInitialized = false

export function useCornerstone(): CornerstoneState {
  const [state, setState] = useState<CornerstoneState>({
    isInitialized: isGloballyInitialized,
    error: null,
  })

  useEffect(() => {
    async function initializeCornerstone() {
      if (isGloballyInitialized) {
        setState({ isInitialized: true, error: null })
        return
      }

      try {
        // Initialize Cornerstone Core
        await csInit()

        // Configure DICOM image loader
        cornerstoneDICOMImageLoader.external.cornerstone = {
          metaData: {
            get: (_type: string, _imageId: string) => {
              // Return undefined for now - metadata handling will be added
              return undefined
            },
          },
        }
        cornerstoneDICOMImageLoader.external.dicomParser = dicomParser

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
        }
        cornerstoneDICOMImageLoader.webWorkerManager.initialize(config)

        // Initialize Cornerstone Tools
        cornerstoneTools.init()

        // Register tools globally
        cornerstoneTools.addTool(WindowLevelTool)
        cornerstoneTools.addTool(PanTool)
        cornerstoneTools.addTool(ZoomTool)
        cornerstoneTools.addTool(StackScrollTool)

        isGloballyInitialized = true
        setState({ isInitialized: true, error: null })
        console.log('Cornerstone initialized successfully')
      } catch (error) {
        console.error('Failed to initialize Cornerstone:', error)
        setState({ isInitialized: false, error: error as Error })
      }
    }

    initializeCornerstone()
  }, [])

  return state
}

export { Enums, RenderingEngine, ToolGroupManager, csToolsEnums }
