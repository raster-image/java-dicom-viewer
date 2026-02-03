import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../services/api';
import type { Study, PacsConfiguration } from '../types';

export default function StudyBrowser() {
  const navigate = useNavigate();
  const [selectedPacs, setSelectedPacs] = useState<string>('');
  const [filters, setFilters] = useState({
    patientId: '',
    patientName: '',
    studyDate: '',
    modality: '',
    accessionNumber: '',
  });

  // Fetch PACS configurations
  const {
    data: pacsData,
    isLoading: isPacsLoading,
    error: pacsError,
  } = useQuery({
    queryKey: ['pacs'],
    queryFn: () => apiClient.getPacsConfigurations(),
  });

  // Auto-select first active PACS if none selected
  useEffect(() => {
    if (pacsData?.configurations && !selectedPacs) {
      // Try to find default PACS first, otherwise use first active one
      const defaultPacs = pacsData.configurations.find((p: PacsConfiguration) => p.isActive);
      if (defaultPacs) {
        setSelectedPacs(defaultPacs.id);
      } else if (pacsData.configurations.length > 0) {
        setSelectedPacs(pacsData.configurations[0].id);
      }
    }
  }, [pacsData, selectedPacs]);

  // Fetch studies when PACS is selected
  const {
    data: studiesData,
    isLoading,
    refetch,
  } = useQuery({
    queryKey: ['studies', selectedPacs, filters],
    queryFn: () => apiClient.queryStudies(selectedPacs, filters),
    enabled: !!selectedPacs,
  });

  const handleSearch = () => {
    refetch();
  };

  const handleViewStudy = (studyInstanceUid: string) => {
    navigate(`/viewer/${studyInstanceUid}?pacsId=${selectedPacs}`);
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-white mb-6">Study Browser</h1>

      {/* No PACS Configured Warning */}
      {!isPacsLoading &&
        !pacsError &&
        (!pacsData?.configurations || pacsData.configurations.length === 0) && (
          <div className="bg-yellow-900/50 border border-yellow-700 rounded-lg p-4 mb-6">
            <div className="flex items-start gap-3">
              <svg
                className="h-6 w-6 text-yellow-400 mt-0.5"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z"
                  clipRule="evenodd"
                />
              </svg>
              <div>
                <h3 className="text-yellow-300 font-semibold">No PACS Configured</h3>
                <p className="text-yellow-200 text-sm mt-1">
                  You need to configure at least one PACS connection before you can browse studies.
                  Go to Settings to add a PACS configuration.
                </p>
                <button
                  onClick={() => navigate('/settings')}
                  className="mt-3 px-4 py-2 bg-yellow-600 text-white rounded hover:bg-yellow-700 text-sm"
                >
                  Go to Settings
                </button>
              </div>
            </div>
          </div>
        )}

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
              disabled={isPacsLoading}
            >
              <option value="">
                {isPacsLoading
                  ? 'Loading PACS...'
                  : pacsError
                    ? 'Error loading PACS'
                    : 'Select PACS...'}
              </option>
              {pacsData?.configurations?.map((pacs: PacsConfiguration) => (
                <option key={pacs.id} value={pacs.id}>
                  {pacs.name} ({pacs.pacsType})
                </option>
              ))}
            </select>
            {pacsError && (
              <p className="text-red-400 text-xs mt-1">Failed to load PACS configurations</p>
            )}
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
                <tr
                  key={study.StudyInstanceUID}
                  className="border-t border-gray-700 hover:bg-gray-700"
                >
                  <td className="px-4 py-3 text-white">{study.PatientName || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.PatientID || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.StudyDate || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">{study.StudyDescription || 'N/A'}</td>
                  <td className="px-4 py-3 text-gray-300">
                    {study.ModalitiesInStudy || study.Modality || 'N/A'}
                  </td>
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
  );
}
