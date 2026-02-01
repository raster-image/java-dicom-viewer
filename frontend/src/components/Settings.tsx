import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import type { PacsConfiguration } from '../types';

export default function Settings() {
  const queryClient = useQueryClient();
  const [editingPacs, setEditingPacs] = useState<PacsConfiguration | null>(null);
  const [testingPacs, setTestingPacs] = useState<string | null>(null);
  const [testResult, setTestResult] = useState<{
    id: string;
    success: boolean;
    message: string;
  } | null>(null);

  // Fetch PACS configurations
  const { data: pacsData, isLoading } = useQuery({
    queryKey: ['pacs'],
    queryFn: () => apiClient.getPacsConfigurations(),
  });

  // Create/Update mutation
  const saveMutation = useMutation({
    mutationFn: (pacs: PacsConfiguration) =>
      pacs.id ? apiClient.updatePacs(pacs.id, pacs) : apiClient.createPacs(pacs),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pacs'] });
      setEditingPacs(null);
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => apiClient.deletePacs(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pacs'] });
    },
  });

  // Test connection
  const testConnection = async (id: string) => {
    setTestingPacs(id);
    setTestResult(null);
    try {
      const result = await apiClient.testPacsConnection(id);
      setTestResult({ id, ...result });
    } catch (error) {
      setTestResult({ id, success: false, message: 'Test failed' });
    }
    setTestingPacs(null);
  };

  const handleSave = () => {
    if (editingPacs) {
      saveMutation.mutate(editingPacs);
    }
  };

  const handleAddNew = () => {
    setEditingPacs({
      id: '',
      name: '',
      host: '',
      port: 104,
      aeTitle: '',
      pacsType: 'LEGACY',
      wadoRsUrl: '',
      qidoRsUrl: '',
      stowRsUrl: '',
      isActive: true,
    });
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-white mb-6">Settings</h1>

      {/* PACS Configuration Section */}
      <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-lg font-semibold text-white">PACS Configurations</h2>
          <button
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            onClick={handleAddNew}
          >
            Add PACS
          </button>
        </div>

        {isLoading ? (
          <p className="text-gray-400">Loading...</p>
        ) : (
          <div className="space-y-4">
            {pacsData?.configurations?.map((pacs: PacsConfiguration) => (
              <div
                key={pacs.id}
                className="bg-gray-700 rounded-lg p-4 flex items-center justify-between"
              >
                <div>
                  <h3 className="text-white font-medium">{pacs.name}</h3>
                  <p className="text-gray-400 text-sm">
                    {pacs.host}:{pacs.port} | AE: {pacs.aeTitle} | Type: {pacs.pacsType}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {testResult?.id === pacs.id && (
                    <span className={testResult.success ? 'text-green-400' : 'text-red-400'}>
                      {testResult.success ? '✓ Connected' : '✗ Failed'}
                    </span>
                  )}
                  <button
                    className="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-500"
                    onClick={() => testConnection(pacs.id)}
                    disabled={testingPacs === pacs.id}
                  >
                    {testingPacs === pacs.id ? 'Testing...' : 'Test'}
                  </button>
                  <button
                    className="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-500"
                    onClick={() => setEditingPacs(pacs)}
                  >
                    Edit
                  </button>
                  <button
                    className="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700"
                    onClick={() => deleteMutation.mutate(pacs.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}

            {(!pacsData?.configurations || pacsData.configurations.length === 0) && (
              <p className="text-gray-400">No PACS configured. Click "Add PACS" to get started.</p>
            )}
          </div>
        )}
      </div>

      {/* Edit Modal */}
      {editingPacs && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 w-full max-w-lg border border-gray-700">
            <h2 className="text-lg font-semibold text-white mb-4">
              {editingPacs.id ? 'Edit PACS' : 'Add PACS'}
            </h2>

            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-1">Name</label>
                <input
                  type="text"
                  className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                  value={editingPacs.name}
                  onChange={(e) => setEditingPacs({ ...editingPacs, name: e.target.value })}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Host</label>
                  <input
                    type="text"
                    className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                    value={editingPacs.host}
                    onChange={(e) => setEditingPacs({ ...editingPacs, host: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Port</label>
                  <input
                    type="number"
                    className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                    value={editingPacs.port}
                    onChange={(e) =>
                      setEditingPacs({ ...editingPacs, port: parseInt(e.target.value) })
                    }
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-1">AE Title</label>
                  <input
                    type="text"
                    className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                    value={editingPacs.aeTitle}
                    onChange={(e) => setEditingPacs({ ...editingPacs, aeTitle: e.target.value })}
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-1">Type</label>
                  <select
                    className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                    value={editingPacs.pacsType}
                    onChange={(e) =>
                      setEditingPacs({
                        ...editingPacs,
                        pacsType: e.target.value as 'LEGACY' | 'DICOMWEB',
                      })
                    }
                  >
                    <option value="LEGACY">Legacy (C-FIND/C-MOVE)</option>
                    <option value="DICOMWEB">DICOMweb (WADO-RS/QIDO-RS)</option>
                  </select>
                </div>
              </div>

              {editingPacs.pacsType === 'DICOMWEB' && (
                <>
                  <div>
                    <label className="block text-sm text-gray-400 mb-1">WADO-RS URL</label>
                    <input
                      type="text"
                      className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                      value={editingPacs.wadoRsUrl}
                      onChange={(e) =>
                        setEditingPacs({ ...editingPacs, wadoRsUrl: e.target.value })
                      }
                      placeholder="https://pacs.example.com/wado-rs"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-400 mb-1">QIDO-RS URL</label>
                    <input
                      type="text"
                      className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                      value={editingPacs.qidoRsUrl}
                      onChange={(e) =>
                        setEditingPacs({ ...editingPacs, qidoRsUrl: e.target.value })
                      }
                      placeholder="https://pacs.example.com/qido-rs"
                    />
                  </div>
                  <div>
                    <label className="block text-sm text-gray-400 mb-1">STOW-RS URL</label>
                    <input
                      type="text"
                      className="w-full bg-gray-700 text-white rounded px-3 py-2 border border-gray-600"
                      value={editingPacs.stowRsUrl}
                      onChange={(e) =>
                        setEditingPacs({ ...editingPacs, stowRsUrl: e.target.value })
                      }
                      placeholder="https://pacs.example.com/stow-rs"
                    />
                  </div>
                </>
              )}
            </div>

            <div className="flex justify-end gap-2 mt-6">
              <button
                className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-500"
                onClick={() => setEditingPacs(null)}
              >
                Cancel
              </button>
              <button
                className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                onClick={handleSave}
              >
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
