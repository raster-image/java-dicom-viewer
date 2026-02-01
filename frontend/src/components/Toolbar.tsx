import React from 'react'

export type ViewerTool = 'WindowLevel' | 'Pan' | 'Zoom' | 'StackScroll'

interface ToolbarProps {
  activeTool: ViewerTool
  onToolChange: (tool: ViewerTool) => void
  currentImage: number
  totalImages: number
  onNavigate: (index: number) => void
  onReset: () => void
}

interface ToolButton {
  id: ViewerTool
  label: string
  icon: string
  shortcut: string
  description: string
}

const TOOLS: ToolButton[] = [
  { 
    id: 'WindowLevel', 
    label: 'W/L', 
    icon: '◐', 
    shortcut: 'W',
    description: 'Adjust window/level (brightness/contrast)'
  },
  { 
    id: 'Pan', 
    label: 'Pan', 
    icon: '✥', 
    shortcut: 'P',
    description: 'Pan the image'
  },
  { 
    id: 'Zoom', 
    label: 'Zoom', 
    icon: '🔍', 
    shortcut: 'Z',
    description: 'Zoom in/out'
  },
  { 
    id: 'StackScroll', 
    label: 'Scroll', 
    icon: '↕', 
    shortcut: 'S',
    description: 'Scroll through images'
  },
]

export const Toolbar: React.FC<ToolbarProps> = ({
  activeTool,
  onToolChange,
  currentImage,
  totalImages,
  onNavigate,
  onReset,
}) => {
  return (
    <div className="flex items-center justify-between p-2 bg-gray-800 border-b border-gray-700">
      {/* Tool buttons */}
      <div className="flex gap-1">
        {TOOLS.map((tool) => (
          <button
            key={tool.id}
            onClick={() => onToolChange(tool.id)}
            className={`
              px-3 py-2 rounded text-sm font-medium transition-colors
              ${activeTool === tool.id
                ? 'bg-blue-600 text-white'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }
            `}
            title={`${tool.description} (${tool.shortcut})`}
          >
            <span className="mr-1">{tool.icon}</span>
            {tool.label}
          </button>
        ))}
        
        {/* Reset button */}
        <button
          onClick={onReset}
          className="px-3 py-2 rounded text-sm font-medium bg-gray-700 text-gray-300 hover:bg-gray-600 transition-colors ml-2"
          title="Reset view"
        >
          ↻ Reset
        </button>
      </div>

      {/* Navigation */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => onNavigate(currentImage - 2)}
          disabled={currentImage <= 1}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600 transition-colors"
          title="Previous image"
        >
          ◀
        </button>
        <span className="text-white text-sm min-w-[100px] text-center">
          {totalImages > 0 ? `${currentImage} / ${totalImages}` : 'No images'}
        </span>
        <button
          onClick={() => onNavigate(currentImage)}
          disabled={currentImage >= totalImages}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600 transition-colors"
          title="Next image"
        >
          ▶
        </button>
      </div>

      {/* Window/Level presets */}
      <div className="flex gap-1">
        <select
          className="px-2 py-1 bg-gray-700 text-white rounded text-sm border border-gray-600"
          onChange={(e) => {
            // Preset will be handled by parent component
            console.log('Apply preset:', e.target.value)
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
  )
}

export default Toolbar
