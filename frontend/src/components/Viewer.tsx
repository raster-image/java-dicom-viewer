import { useParams } from 'react-router-dom'

export default function Viewer() {
  const { studyInstanceUid } = useParams()

  return (
    <div className="h-full flex flex-col">
      {/* Toolbar */}
      <div className="bg-gray-800 border-b border-gray-700 p-2 flex items-center gap-2">
        <button className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
          Window/Level
        </button>
        <button className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
          Zoom
        </button>
        <button className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
          Pan
        </button>
        <button className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
          Measure
        </button>
        <button className="bg-gray-700 text-white px-3 py-1 rounded text-sm hover:bg-gray-600">
          Reset
        </button>
        <div className="flex-1" />
        <span className="text-gray-400 text-sm">
          Study: {studyInstanceUid?.slice(0, 20)}...
        </span>
      </div>

      {/* Viewport Area */}
      <div className="flex-1 flex">
        {/* Series Panel */}
        <div className="w-48 bg-gray-800 border-r border-gray-700 overflow-y-auto">
          <div className="p-2">
            <h3 className="text-sm font-semibold text-white mb-2">Series</h3>
            <p className="text-gray-400 text-xs">
              Select a series to view
            </p>
          </div>
        </div>

        {/* Main Viewport */}
        <div className="flex-1 bg-black flex items-center justify-center">
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
            <p className="text-lg font-medium">DICOM Viewer</p>
            <p className="text-sm mt-2">
              Cornerstone.js viewport will be rendered here
            </p>
            <p className="text-xs mt-1">
              Configure PACS and select a study to begin
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
