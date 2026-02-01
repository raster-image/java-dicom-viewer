import React from 'react';

export type ViewerTool =
  | 'WindowLevel'
  | 'Pan'
  | 'Zoom'
  | 'StackScroll'
  // Phase 3: Measurement tools
  | 'Length'
  | 'Angle'
  | 'CobbAngle'
  | 'RectangleROI'
  | 'EllipticalROI'
  | 'Probe'
  // Phase 3: Annotation tools
  | 'ArrowAnnotate'
  | 'TextMarker';

export interface WindowLevelPreset {
  name: string;
  windowWidth: number;
  windowCenter: number;
}

export const WINDOW_LEVEL_PRESETS: Record<string, WindowLevelPreset> = {
  'ct-abdomen': { name: 'CT Abdomen', windowWidth: 400, windowCenter: 50 },
  'ct-lung': { name: 'CT Lung', windowWidth: 1500, windowCenter: -600 },
  'ct-bone': { name: 'CT Bone', windowWidth: 2000, windowCenter: 500 },
  'ct-brain': { name: 'CT Brain', windowWidth: 80, windowCenter: 40 },
  'ct-soft-tissue': { name: 'CT Soft Tissue', windowWidth: 350, windowCenter: 50 },
  'mr-t1': { name: 'MR T1', windowWidth: 500, windowCenter: 250 },
  'mr-t2': { name: 'MR T2', windowWidth: 400, windowCenter: 200 },
  'mr-flair': { name: 'MR FLAIR', windowWidth: 1200, windowCenter: 600 },
};

interface ToolbarProps {
  activeTool: ViewerTool;
  onToolChange: (tool: ViewerTool) => void;
  currentImage: number; // 1-based display index
  totalImages: number;
  onNavigate: (index: number) => void; // Expects 0-based index
  onReset: () => void;
  onPresetChange?: (preset: WindowLevelPreset) => void;
  isKeyImage?: boolean;
  onToggleKeyImage?: () => void;
  measurementCount?: number;
  annotationCount?: number;
  onClearMeasurements?: () => void;
  showAnnotations?: boolean;
  onToggleAnnotations?: () => void;
}

interface ToolButton {
  id: ViewerTool;
  label: string;
  icon: string;
  shortcut: string;
  description: string;
  category: 'navigation' | 'measurement' | 'annotation';
}

const TOOLS: ToolButton[] = [
  // Navigation tools
  {
    id: 'WindowLevel',
    label: 'W/L',
    icon: '◐',
    shortcut: 'W',
    description: 'Adjust window/level (brightness/contrast)',
    category: 'navigation',
  },
  {
    id: 'Pan',
    label: 'Pan',
    icon: '✥',
    shortcut: 'P',
    description: 'Pan the image',
    category: 'navigation',
  },
  {
    id: 'Zoom',
    label: 'Zoom',
    icon: '🔍',
    shortcut: 'Z',
    description: 'Zoom in/out',
    category: 'navigation',
  },
  {
    id: 'StackScroll',
    label: 'Scroll',
    icon: '↕',
    shortcut: 'S',
    description: 'Scroll through images',
    category: 'navigation',
  },
  // Phase 3: Measurement tools
  {
    id: 'Length',
    label: 'Length',
    icon: '📏',
    shortcut: 'L',
    description: 'Measure distance between two points',
    category: 'measurement',
  },
  {
    id: 'Angle',
    label: 'Angle',
    icon: '∠',
    shortcut: 'A',
    description: 'Measure angle between three points',
    category: 'measurement',
  },
  {
    id: 'CobbAngle',
    label: 'Cobb',
    icon: '⌒',
    shortcut: 'C',
    description: "Cobb's angle for spinal measurements",
    category: 'measurement',
  },
  {
    id: 'RectangleROI',
    label: 'Rect',
    icon: '▭',
    shortcut: 'R',
    description: 'Rectangle ROI with statistics',
    category: 'measurement',
  },
  {
    id: 'EllipticalROI',
    label: 'Ellipse',
    icon: '⬭',
    shortcut: 'E',
    description: 'Ellipse ROI with statistics',
    category: 'measurement',
  },
  {
    id: 'Probe',
    label: 'Probe',
    icon: '✚',
    shortcut: 'B',
    description: 'Probe pixel values (HU for CT)',
    category: 'measurement',
  },
  // Phase 3: Annotation tools
  {
    id: 'ArrowAnnotate',
    label: 'Arrow',
    icon: '➤',
    shortcut: 'J',
    description: 'Add arrow annotation',
    category: 'annotation',
  },
  {
    id: 'TextMarker',
    label: 'Text',
    icon: 'T',
    shortcut: 'T',
    description: 'Add text marker',
    category: 'annotation',
  },
];

export const Toolbar: React.FC<ToolbarProps> = ({
  activeTool,
  onToolChange,
  currentImage,
  totalImages,
  onNavigate,
  onReset,
  onPresetChange,
  isKeyImage,
  onToggleKeyImage,
  measurementCount,
  onClearMeasurements,
  showAnnotations = true,
  onToggleAnnotations,
}) => {
  const handlePresetChange = (presetKey: string) => {
    const preset = WINDOW_LEVEL_PRESETS[presetKey];
    if (preset && onPresetChange) {
      onPresetChange(preset);
    }
  };

  const navigationTools = TOOLS.filter((t) => t.category === 'navigation');
  const measurementTools = TOOLS.filter((t) => t.category === 'measurement');
  const annotationTools = TOOLS.filter((t) => t.category === 'annotation');

  const renderToolButton = (tool: ToolButton) => (
    <button
      key={tool.id}
      onClick={() => onToolChange(tool.id)}
      className={`
        px-2 py-1.5 rounded text-xs font-medium transition-colors
        ${
          activeTool === tool.id
            ? 'bg-blue-600 text-white'
            : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
        }
      `}
      title={`${tool.description} (${tool.shortcut})`}
    >
      <span className="mr-1">{tool.icon}</span>
      {tool.label}
    </button>
  );

  return (
    <div className="flex items-center justify-between p-2 bg-gray-800 border-b border-gray-700 flex-wrap gap-2">
      {/* Navigation tools */}
      <div className="flex gap-1 items-center">
        <span className="text-gray-400 text-xs mr-1">Nav:</span>
        {navigationTools.map(renderToolButton)}

        {/* Reset button */}
        <button
          onClick={onReset}
          className="px-2 py-1.5 rounded text-xs font-medium bg-gray-700 text-gray-300 hover:bg-gray-600 transition-colors ml-1"
          title="Reset view"
        >
          ↻
        </button>
      </div>

      {/* Measurement tools */}
      <div className="flex gap-1 items-center border-l border-gray-600 pl-2">
        <span className="text-gray-400 text-xs mr-1">Measure:</span>
        {measurementTools.map(renderToolButton)}
        {measurementCount !== undefined && measurementCount > 0 && (
          <span className="ml-1 px-1.5 py-0.5 bg-blue-900 text-blue-200 text-xs rounded">
            {measurementCount}
          </span>
        )}
        {onClearMeasurements && (
          <button
            onClick={onClearMeasurements}
            className="px-1.5 py-1 rounded text-xs bg-red-900 text-red-200 hover:bg-red-800 transition-colors ml-1"
            title="Clear all measurements"
          >
            ✕
          </button>
        )}
      </div>

      {/* Annotation tools */}
      <div className="flex gap-1 items-center border-l border-gray-600 pl-2">
        <span className="text-gray-400 text-xs mr-1">Annotate:</span>
        {annotationTools.map(renderToolButton)}
        {onToggleAnnotations && (
          <button
            onClick={onToggleAnnotations}
            className={`px-1.5 py-1 rounded text-xs transition-colors ml-1 ${
              showAnnotations
                ? 'bg-green-900 text-green-200 hover:bg-green-800'
                : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
            }`}
            title={showAnnotations ? 'Hide annotations' : 'Show annotations'}
          >
            👁
          </button>
        )}
      </div>

      {/* Key Image toggle */}
      {onToggleKeyImage && (
        <div className="flex items-center border-l border-gray-600 pl-2">
          <button
            onClick={onToggleKeyImage}
            className={`px-2 py-1.5 rounded text-xs font-medium transition-colors ${
              isKeyImage
                ? 'bg-yellow-600 text-white'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            }`}
            title={isKeyImage ? 'Unmark as key image' : 'Mark as key image'}
          >
            ⭐ Key
          </button>
        </div>
      )}

      {/* Navigation - currentImage is 1-based, onNavigate expects 0-based index */}
      <div className="flex items-center gap-2 border-l border-gray-600 pl-2">
        <button
          onClick={() => onNavigate(currentImage - 2)}
          disabled={currentImage <= 1}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600 transition-colors text-xs"
          title="Previous image"
        >
          ◀
        </button>
        <span className="text-white text-xs min-w-[80px] text-center">
          {totalImages > 0 ? `${currentImage} / ${totalImages}` : 'No images'}
        </span>
        <button
          onClick={() => onNavigate(currentImage)}
          disabled={currentImage >= totalImages}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600 transition-colors text-xs"
          title="Next image"
        >
          ▶
        </button>
      </div>

      {/* Window/Level presets */}
      <div className="flex gap-1">
        <select
          className="px-2 py-1 bg-gray-700 text-white rounded text-xs border border-gray-600"
          onChange={(e) => {
            if (e.target.value) {
              handlePresetChange(e.target.value);
            }
          }}
          defaultValue=""
        >
          <option value="">W/L Presets</option>
          <optgroup label="CT Presets">
            <option value="ct-abdomen">CT Abdomen (W:400 L:50)</option>
            <option value="ct-lung">CT Lung (W:1500 L:-600)</option>
            <option value="ct-bone">CT Bone (W:2000 L:500)</option>
            <option value="ct-brain">CT Brain (W:80 L:40)</option>
            <option value="ct-soft-tissue">CT Soft Tissue (W:350 L:50)</option>
          </optgroup>
          <optgroup label="MR Presets">
            <option value="mr-t1">MR T1 (W:500 L:250)</option>
            <option value="mr-t2">MR T2 (W:400 L:200)</option>
            <option value="mr-flair">MR FLAIR (W:1200 L:600)</option>
          </optgroup>
        </select>
      </div>
    </div>
  );
};

export default Toolbar;
