import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { apiClient } from '../services/api'
import type { Study, PacsConfiguration } from '../types'

export default function StudyBrowser() {
  const navigate = useNavigate()
  const [selectedPacs, setSelectedPacs] = useState<string>('')
  const [filters, setFilters] = useState({
    patientId: '',
    patientName: '',
    studyDate: '',
    modality: '',
    accessionNumber: '',
  })

  // Fetch PACS configurations
  const { data: pacsData } = useQuery({
    queryKey: ['pacs'],
    queryFn: () => apiClient.getPacsConfigurations(),
  })

  // Fetch studies when PACS is selected
  const { data: studiesData, isLoading, refetch } = useQuery({
    queryKey: ['studies', selectedPacs, filters],
    queryFn: () => apiClient.queryStudies(selectedPacs, filters),
    enabled: !!selectedPacs,
  })

  const handleSearch = () => {
    refetch()
  }

  const handleViewStudy = (studyInstanceUid: string) => {
    navigate(`/viewer/${studyInstanceUid}?pacsId=${selectedPacs}`)
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-white mb-6">Study Browser</h1>

      {/* Search Filters */}
      <div className="bg-gray-800 rounded-lg p-4 mb-6 border border-gray-700">
        <div className="grid grid-cols-1 md:grid-cols-6 gap-4">
          {/* PACS Selector */}
          <div>
            <label className="block text-sm text-gray-400 mb-1">PACS</label>
            <select
              className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
              value={selectedPacs}
              onChange={(e) => setSelectedPacs(e.target.value)}
            >
              <option value="">Select PACS...</option>
              {pacsData?.configurations?.map((pacs: PacsConfiguration) => (
                <option key={pacs.id} value={pacs.id}>
                  {pacs.name} ({pacs.pacsType})
                </option>
              ))}
            </select>
          </div>

          {/* Patient ID */}
          <div>
            <label className="block text-sm text-gray-400 mb-1">Patient ID</label>
            <input
              type="text"
              className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
              value={filters.patientId}
              onChange={(e) => setFilters({ ...filters, patientId: e.target.value })}
              placeholder="Patient ID"
            />
          </div>

          {/* Patient Name */}
          <div>
            <label className="block text-sm text-gray-400 mb-1">Patient Name</label>
            <input
              type="text"
              className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
              value={filters.patientName}
              onChange={(e) => setFilters({ ...filters, patientName: e.target.value })}
              placeholder="Patient Name"
            />
          </div>

          {/* Study Date */}
          <div>
            <label className="block text-sm text-gray-400 mb-1">Study Date</label>
            <input
              type="text"
              className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
              value={filters.studyDate}
              onChange={(e) => setFilters({ ...filters, studyDate: e.target.value })}
              placeholder="YYYYMMDD"
            />
          </div>

          {/* Modality */}
          <div>
            <label className="block text-sm text-gray-400 mb-1">Modality</label>
            <select
              className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
              value={filters.modality}
              onChange={(e) => setFilters({ ...filters, modality: e.target.value })}
            >
              <option value="">All</option>
              <option value="CT">CT</option>
              <option value="MR">MR</option>
              <option value="XR">XR</option>
              <option value="US">US</option>
              <option value="CR">CR</option>
            </select>
          </div>

          {/* Search Button */}
          <div className="flex items-end">
            <button
              className="w-full bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50"
              onClick={handleSearch}
              disabled={!selectedPacs}
            >
              Search
            </button>
          </div>
        </div>
      </div>

      {/* Studies Table */}
      <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-700">
            <tr>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Patient Name</th>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Patient ID</th>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Study Date</th>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Description</th>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Modality</th>
              <th className="px-4 py-3 text-left text-sm text-gray-300">Actions</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  Loading...
                </td>
              </tr>
            ) : studiesData?.studies?.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-gray-400">
                  No studies found. Select a PACS and search.
                </td>
              </tr>
            ) : (
              studiesData?.studies?.map((study: Study) => (
                <tr key={study.StudyInstanceUID} className="border-t border-gray-700 hover:bg-gray-700">
                  <td className="px-4 py-3 text-white">{study.PatientName || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.PatientID || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.StudyDate || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.StudyDescription || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.ModalitiesInStudy || study.Modality || 'N/A'}</td>
                  <td className="px-4 py-3">
                    <button
                      className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700"
                      onClick={() => handleViewStudy(study.StudyInstanceUID)}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
