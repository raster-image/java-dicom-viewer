import { Outlet, Link } from 'react-router-dom';

export default function Layout() {
  return (
    <div className="flex h-screen bg-gray-900">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-800 border-r border-gray-700">
        <div className="p-4">
          <h1 className="text-xl font-bold text-white">DICOM Viewer</h1>
        </div>
        <nav className="mt-4">
          <Link to="/" className="block px-4 py-2 text-gray-300 hover:bg-gray-700 hover:text-white">
            Dashboard
          </Link>
          <Link
            to="/studies"
            className="block px-4 py-2 text-gray-300 hover:bg-gray-700 hover:text-white"
          >
            Study Browser
          </Link>
          <Link
            to="/pacs-explorer"
            className="block px-4 py-2 text-gray-300 hover:bg-gray-700 hover:text-white"
          >
            PACS Explorer
          </Link>
          <Link
            to="/settings"
            className="block px-4 py-2 text-gray-300 hover:bg-gray-700 hover:text-white"
          >
            Settings
          </Link>
        </nav>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
