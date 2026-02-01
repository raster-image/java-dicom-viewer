import { useQuery } from '@tanstack/react-query'
import { apiClient } from '../services/api'

export default function Dashboard() {
  const { data: health } = useQuery({
    queryKey: ['health'],
    queryFn: () => apiClient.getHealth(),
  })

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-white mb-6">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* System Status Card */}
        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <h2 className="text-lg font-semibold text-white mb-4">System Status</h2>
          <div className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-400">API Status:</span>
              <span className={health?.status === 'UP' ? 'text-green-400' : 'text-red-400'}>
                {health?.status || 'Unknown'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">Version:</span>
              <span className="text-white">{health?.version || 'N/A'}</span>
            </div>
          </div>
        </div>

        {/* Quick Actions Card */}
        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <h2 className="text-lg font-semibold text-white mb-4">Quick Actions</h2>
          <div className="space-y-2">
            <a
              href="/studies"
              className="block w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 text-center"
            >
              Browse Studies
            </a>
            <a
              href="/settings"
              className="block w-full bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-500 text-center"
            >
              Configure PACS
            </a>
          </div>
        </div>

        {/* Features Card */}
        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <h2 className="text-lg font-semibold text-white mb-4">Features</h2>
          <ul className="text-gray-300 space-y-1 text-sm">
            <li>✓ DICOMweb (WADO-RS, QIDO-RS)</li>
            <li>✓ Legacy PACS (C-FIND, C-MOVE)</li>
            <li>✓ Multi-PACS Support</li>
            <li>✓ Cornerstone.js Viewer</li>
            <li>✓ Measurement Tools</li>
          </ul>
        </div>
      </div>

      {/* Information Section */}
      <div className="mt-8 bg-gray-800 rounded-lg p-6 border border-gray-700">
        <h2 className="text-lg font-semibold text-white mb-4">Getting Started</h2>
        <div className="text-gray-300 space-y-4">
          <p>
            Welcome to the DICOM Viewer application. This application supports both modern 
            DICOMweb PACS systems and legacy PACS systems using traditional DICOM protocols.
          </p>
          <ol className="list-decimal list-inside space-y-2">
            <li>Configure your PACS connections in the Settings page</li>
            <li>Test connectivity using the connection test feature</li>
            <li>Browse studies from the Study Browser</li>
            <li>Open studies in the viewer for image display and measurements</li>
          </ol>
        </div>
      </div>
    </div>
  )
}
