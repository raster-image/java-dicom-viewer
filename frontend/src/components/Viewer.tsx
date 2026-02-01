import { useEffect, useRef, useState, useCallback } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import * as cornerstone from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { apiClient } from '../services/api';
import { useCornerstone } from '../hooks/useCornerstone';
import { Toolbar, ViewerTool, WindowLevelPreset } from './Toolbar';
import type { Series, Instance } from '../types';

const TOOL_GROUP_ID = 'STACK_TOOL_GROUP';
const VIEWPORT_ID = 'MAIN_VIEWPORT';
const RENDERING_ENGINE_ID = 'VIEWER_ENGINE';

interface ViewerState {
  currentImageIndex: number;
  totalImages: number;
  selectedSeriesUid: string | null;
  windowWidth: number | null;
  windowCenter: number | null;
}

export default function Viewer() {
  const { studyInstanceUid } = useParams();
  const [searchParams] = useSearchParams();
  const pacsId = searchParams.get('pacsId') || '';

  const containerRef = useRef<HTMLDivElement>(null);
  const { isInitialized, error: cornerstoneError } = useCornerstone();

  const [activeTool, setActiveTool] = useState<ViewerTool>('WindowLevel');
  const [viewerState, setViewerState] = useState<ViewerState>({
    currentImageIndex: 0,
    totalImages: 0,
    selectedSeriesUid: null,
    windowWidth: null,
    windowCenter: null,
  });

  const [renderingEngine, setRenderingEngine] = useState<cornerstone.RenderingEngine | null>(null);
  const [viewport, setViewport] = useState<cornerstone.Types.IStackViewport | null>(null);

  // Fetch series for the study
  const { data: seriesData, isLoading: isLoadingSeries } = useQuery({
    queryKey: ['series', pacsId, studyInstanceUid],
    queryFn: () => apiClient.querySeries(pacsId, studyInstanceUid || ''),
    enabled: !!pacsId && !!studyInstanceUid,
  });

  // Fetch instances for selected series
  const { data: instancesData, isLoading: isLoadingInstances } = useQuery({
    queryKey: ['instances', pacsId, studyInstanceUid, viewerState.selectedSeriesUid],
    queryFn: () =>
      apiClient.queryInstances(pacsId, studyInstanceUid || '', viewerState.selectedSeriesUid || ''),
    enabled: !!pacsId && !!studyInstanceUid && !!viewerState.selectedSeriesUid,
  });

  // Initialize rendering engine and viewport
  useEffect(() => {
    if (!isInitialized || !containerRef.current) return;

    const initViewer = async () => {
      try {
        // Create rendering engine
        const engine = new cornerstone.RenderingEngine(RENDERING_ENGINE_ID);
        setRenderingEngine(engine);

        // Create stack viewport
        const viewportInput = {
          viewportId: VIEWPORT_ID,
          type: cornerstone.Enums.ViewportType.STACK,
          element: containerRef.current!,
          defaultOptions: {
            background: [0, 0, 0] as cornerstone.Types.Point3,
          },
        };

        engine.enableElement(viewportInput);
        const vp = engine.getViewport(VIEWPORT_ID) as cornerstone.Types.IStackViewport;
        setViewport(vp);

        // Setup tool group
        let toolGroup = cornerstoneTools.ToolGroupManager.getToolGroup(TOOL_GROUP_ID);
        if (!toolGroup) {
          toolGroup = cornerstoneTools.ToolGroupManager.createToolGroup(TOOL_GROUP_ID);
        }

        if (toolGroup) {
          toolGroup.addViewport(VIEWPORT_ID, RENDERING_ENGINE_ID);

          // Add tools to the group
          toolGroup.addTool(cornerstoneTools.WindowLevelTool.toolName);
          toolGroup.addTool(cornerstoneTools.PanTool.toolName);
          toolGroup.addTool(cornerstoneTools.ZoomTool.toolName);
          toolGroup.addTool(cornerstoneTools.StackScrollTool.toolName);

          // Set Window/Level as active for left mouse button
          toolGroup.setToolActive(cornerstoneTools.WindowLevelTool.toolName, {
            bindings: [{ mouseButton: cornerstoneTools.Enums.MouseBindings.Primary }],
          });

          // Set scroll tool as enabled (it will respond to wheel events automatically)
          toolGroup.setToolEnabled(cornerstoneTools.StackScrollTool.toolName);
        }
      } catch (err) {
        console.error('Failed to initialize viewer:', err);
      }
    };

    initViewer();

    return () => {
      // Cleanup
      try {
        cornerstoneTools.ToolGroupManager.destroyToolGroup(TOOL_GROUP_ID);
        renderingEngine?.destroy();
      } catch (err) {
        console.warn('Cleanup warning:', err);
      }
    };
  }, [isInitialized]);

  // Auto-select first series when data is loaded
  useEffect(() => {
    if (seriesData?.series?.length > 0 && !viewerState.selectedSeriesUid) {
      const firstSeries = seriesData.series[0];
      setViewerState((prev) => ({
        ...prev,
        selectedSeriesUid: firstSeries.SeriesInstanceUID,
      }));
    }
  }, [seriesData]);

  // Load images when instances are available
  useEffect(() => {
    if (!viewport || !instancesData?.instances?.length) return;

    const loadImages = async () => {
      try {
        // Build image IDs using WADO-RS URL pattern
        const baseUrl = '/api/wado';
        const imageIds = instancesData.instances.map(
          (instance: Instance) =>
            `wadors:${baseUrl}/studies/${studyInstanceUid}/series/${viewerState.selectedSeriesUid}/instances/${instance.SOPInstanceUID}/frames/1`
        );

        // Sort by instance number if available
        const sortedImageIds = [...imageIds];

        await viewport.setStack(sortedImageIds, 0);
        viewport.render();

        setViewerState((prev) => ({
          ...prev,
          totalImages: sortedImageIds.length,
          currentImageIndex: 0,
        }));
      } catch (err) {
        console.error('Failed to load images:', err);
      }
    };

    loadImages();
  }, [viewport, instancesData, studyInstanceUid, viewerState.selectedSeriesUid]);

  // Handle tool change
  const handleToolChange = useCallback(
    (tool: ViewerTool) => {
      const toolGroup = cornerstoneTools.ToolGroupManager.getToolGroup(TOOL_GROUP_ID);
      if (!toolGroup) return;

      // Map tool names
      const toolNameMap: Record<ViewerTool, string> = {
        WindowLevel: cornerstoneTools.WindowLevelTool.toolName,
        Pan: cornerstoneTools.PanTool.toolName,
        Zoom: cornerstoneTools.ZoomTool.toolName,
        StackScroll: cornerstoneTools.StackScrollTool.toolName,
      };

      const currentToolName = toolNameMap[activeTool];
      const newToolName = toolNameMap[tool];

      // Deactivate current tool
      toolGroup.setToolPassive(currentToolName);

      // Activate new tool for primary mouse button
      toolGroup.setToolActive(newToolName, {
        bindings: [{ mouseButton: cornerstoneTools.Enums.MouseBindings.Primary }],
      });

      setActiveTool(tool);
    },
    [activeTool]
  );

  // Navigate to specific image (index is 0-based)
  const handleNavigateImage = useCallback(
    (index: number) => {
      if (!viewport || !viewerState.totalImages) return;

      const clampedIndex = Math.max(0, Math.min(index, viewerState.totalImages - 1));
      viewport.setImageIdIndex(clampedIndex);

      setViewerState((prev) => ({
        ...prev,
        currentImageIndex: clampedIndex,
      }));
    },
    [viewport, viewerState.totalImages]
  );

  // Reset viewport
  const handleReset = useCallback(() => {
    if (!viewport) return;
    viewport.resetCamera();
    viewport.render();
  }, [viewport]);

  // Apply window/level preset
  const handlePresetChange = useCallback(
    (preset: WindowLevelPreset) => {
      if (!viewport) return;

      const properties = viewport.getProperties();
      viewport.setProperties({
        ...properties,
        voiRange: {
          lower: preset.windowCenter - preset.windowWidth / 2,
          upper: preset.windowCenter + preset.windowWidth / 2,
        },
      });
      viewport.render();

      setViewerState((prev) => ({
        ...prev,
        windowWidth: preset.windowWidth,
        windowCenter: preset.windowCenter,
      }));
    },
    [viewport]
  );

  // Select series
  const handleSelectSeries = (seriesUid: string) => {
    setViewerState((prev) => ({
      ...prev,
      selectedSeriesUid: seriesUid,
      currentImageIndex: 0,
    }));
  };

  // Error state
  if (cornerstoneError) {
    return (
      <div className="h-full flex items-center justify-center bg-gray-900 text-red-500">
        <div className="text-center">
          <h3 className="text-lg font-semibold">Failed to initialize viewer</h3>
          <p className="text-sm mt-2">{cornerstoneError.message}</p>
        </div>
      </div>
    );
  }

  // Loading state
  if (!isInitialized) {
    return (
      <div className="h-full flex items-center justify-center bg-gray-900 text-white">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p>Initializing DICOM Viewer...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col bg-gray-900">
      {/* Toolbar */}
      <Toolbar
        activeTool={activeTool}
        onToolChange={handleToolChange}
        currentImage={viewerState.currentImageIndex + 1}
        totalImages={viewerState.totalImages}
        onNavigate={handleNavigateImage}
        onReset={handleReset}
        onPresetChange={handlePresetChange}
      />

      {/* Viewport Area */}
      <div className="flex-1 flex overflow-hidden">
        {/* Series Panel */}
        <div className="w-48 bg-gray-800 border-r border-gray-700 overflow-y-auto flex-shrink-0">
          <div className="p-2">
            <h3 className="text-sm font-semibold text-white mb-2">Series</h3>
            {isLoadingSeries ? (
              <div className="text-gray-400 text-xs">Loading series...</div>
            ) : seriesData?.series?.length > 0 ? (
              <div className="space-y-1">
                {seriesData.series.map((series: Series) => (
                  <button
                    key={series.SeriesInstanceUID}
                    onClick={() => handleSelectSeries(series.SeriesInstanceUID)}
                    className={`w-full text-left p-2 rounded text-xs transition-colors ${
                      viewerState.selectedSeriesUid === series.SeriesInstanceUID
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                    }`}
                  >
                    <div className="font-medium truncate">
                      {series.SeriesDescription || `Series ${series.SeriesNumber || '?'}`}
                    </div>
                    <div className="text-gray-400 mt-1">
                      {series.Modality || 'Unknown'} •{' '}
                      {series.NumberOfSeriesRelatedInstances || '?'} images
                    </div>
                  </button>
                ))}
              </div>
            ) : (
              <p className="text-gray-400 text-xs">No series available</p>
            )}
          </div>
        </div>

        {/* Main Viewport */}
        <div className="flex-1 bg-black relative">
          {isLoadingInstances && (
            <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 z-10">
              <div className="text-white text-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-white mx-auto mb-2"></div>
                <p className="text-sm">Loading images...</p>
              </div>
            </div>
          )}

          {!viewerState.selectedSeriesUid && !isLoadingSeries && (
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="text-center text-gray-400">
                <svg
                  className="mx-auto h-16 w-16 mb-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
                <p className="text-lg font-medium">Select a Series</p>
                <p className="text-sm mt-2">Choose a series from the left panel to view images</p>
              </div>
            </div>
          )}

          <div ref={containerRef} className="w-full h-full" style={{ minHeight: '400px' }} />
        </div>
      </div>

      {/* Status Bar */}
      <div className="p-2 text-white text-sm bg-gray-800 border-t border-gray-700 flex justify-between">
        <span>Study: {studyInstanceUid?.slice(0, 30)}...</span>
        <span>
          {viewerState.selectedSeriesUid ? (
            <>
              Image {viewerState.currentImageIndex + 1} of {viewerState.totalImages}
            </>
          ) : (
            'No series selected'
          )}
        </span>
      </div>
    </div>
  );
}
