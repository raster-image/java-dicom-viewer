import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './components/Dashboard'
import StudyBrowser from './components/StudyBrowser'
import Viewer from './components/Viewer'
import Settings from './components/Settings'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="studies" element={<StudyBrowser />} />
          <Route path="viewer/:studyInstanceUid" element={<Viewer />} />
          <Route path="settings" element={<Settings />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
