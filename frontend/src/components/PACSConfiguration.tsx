import React, { useState } from 'react';
import { pacsService } from '../services/pacsService';
import type { ApplicationEntity, AERequest, AEType, ConnectionStatus } from '../types/pacs';

interface PACSConfigurationProps {
  onClose?: () => void;
}

export const PACSConfiguration: React.FC<PACSConfigurationProps> = ({ onClose }) => {
  const [aeList, setAeList] = useState<ApplicationEntity[]>([]);
  const [editingAE, setEditingAE] = useState<ApplicationEntity | null>(null);
  const [isCreating, setIsCreating] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [testingId, setTestingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Fetch all AEs on mount
  React.useEffect(() => {
    loadAEs();
  }, []);

  const loadAEs = async () => {
    try {
      setIsLoading(true);
      const data = await pacsService.getAllAEs();
      setAeList(data);
      setError(null);
    } catch (err) {
      setError('Failed to load PACS configuration');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = async (data: AERequest) => {
    try {
      setIsSaving(true);
      await pacsService.createAE(data);
      await loadAEs();
      setIsCreating(false);
    } catch (err) {
      setError('Failed to create PACS configuration');
    } finally {
      setIsSaving(false);
    }
  };

  const handleUpdate = async (id: number, data: AERequest) => {
    try {
      setIsSaving(true);
      await pacsService.updateAE(id, data);
      await loadAEs();
      setEditingAE(null);
    } catch (err) {
      setError('Failed to update PACS configuration');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this PACS configuration?')) {
      return;
    }
    try {
      await pacsService.deleteAE(id);
      await loadAEs();
    } catch (err) {
      setError('Failed to delete PACS configuration');
    }
  };

  const handleTest = async (id: number) => {
    try {
      setTestingId(id);
      await pacsService.testConnection(id);
      await loadAEs();
    } catch (err) {
      setError('Connection test failed');
    } finally {
      setTestingId(null);
    }
  };

  const getStatusColor = (status?: ConnectionStatus): string => {
    switch (status) {
      case 'SUCCESS':
        return 'bg-green-100 text-green-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'TIMEOUT':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status?: ConnectionStatus): string => {
    switch (status) {
      case 'SUCCESS':
        return '✓';
      case 'FAILED':
        return '✗';
      case 'TIMEOUT':
        return '⏱';
      default:
        return '?';
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 bg-gray-50 border-b flex justify-between items-center">
        <h2 className="text-xl font-semibold text-gray-900">PACS Configuration</h2>
        <div className="flex gap-2">
          <button
            onClick={() => setIsCreating(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Add PACS
          </button>
          {onClose && (
            <button
              onClick={onClose}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
            >
              Close
            </button>
          )}
        </div>
      </div>

      {/* Error message */}
      {error && (
        <div className="p-4 bg-red-100 text-red-700">
          {error}
          <button onClick={() => setError(null)} className="ml-2 text-red-900">✕</button>
        </div>
      )}

      {/* AE List */}
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">AE Title</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Host</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Port</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Type</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Status</th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-700">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {aeList.map((ae) => (
              <tr key={ae.id} className={!ae.enabled ? 'opacity-50' : ''}>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <span className="font-medium">{ae.aeTitle}</span>
                    {ae.defaultAE && (
                      <span className="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded">
                        Default
                      </span>
                    )}
                  </div>
                  {ae.description && (
                    <div className="text-sm text-gray-500">{ae.description}</div>
                  )}
                </td>
                <td className="px-4 py-3 text-sm">{ae.hostname}</td>
                <td className="px-4 py-3 text-sm">{ae.port}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 text-xs rounded ${
                    ae.aeType === 'LOCAL' ? 'bg-purple-100 text-purple-700' :
                    ae.aeType === 'REMOTE_LEGACY' ? 'bg-orange-100 text-orange-700' :
                    'bg-green-100 text-green-700'
                  }`}>
                    {ae.aeType}
                  </span>
                </td>
                <td className="px-4 py-3">
                  {ae.aeType !== 'LOCAL' && (
                    <span className={`inline-flex items-center px-2 py-1 text-xs rounded ${getStatusColor(ae.lastEchoStatus)}`}>
                      <span className="mr-1">{getStatusIcon(ae.lastEchoStatus)}</span>
                      {ae.lastEchoStatus || 'Unknown'}
                    </span>
                  )}
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {ae.aeType !== 'LOCAL' && (
                      <button
                        onClick={() => handleTest(ae.id)}
                        disabled={testingId === ae.id}
                        className="px-2 py-1 text-sm bg-green-100 text-green-700 rounded hover:bg-green-200"
                        title="Test Connection"
                      >
                        {testingId === ae.id ? '...' : 'Test'}
                      </button>
                    )}
                    <button
                      onClick={() => setEditingAE(ae)}
                      className="px-2 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200"
                    >
                      Edit
                    </button>
                    {ae.aeType !== 'LOCAL' && (
                      <button
                        onClick={() => handleDelete(ae.id)}
                        className="px-2 py-1 text-sm bg-red-100 text-red-700 rounded hover:bg-red-200"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create/Edit Modal */}
      {(isCreating || editingAE) && (
        <AEFormModal
          ae={editingAE}
          onSave={(data) => {
            if (editingAE) {
              handleUpdate(editingAE.id, data);
            } else {
              handleCreate(data);
            }
          }}
          onCancel={() => {
            setIsCreating(false);
            setEditingAE(null);
          }}
          isLoading={isSaving}
        />
      )}
    </div>
  );
};

// AE Form Modal Component
interface AEFormModalProps {
  ae: ApplicationEntity | null;
  onSave: (data: AERequest) => void;
  onCancel: () => void;
  isLoading: boolean;
}

const AEFormModal: React.FC<AEFormModalProps> = ({ ae, onSave, onCancel, isLoading }) => {
  const [formData, setFormData] = useState<AERequest>({
    aeTitle: ae?.aeTitle || '',
    hostname: ae?.hostname || '',
    port: ae?.port || 11112,
    aeType: ae?.aeType || 'REMOTE_LEGACY',
    description: ae?.description || '',
    dicomWebUrl: ae?.dicomWebUrl || '',
    queryRetrieveLevel: ae?.queryRetrieveLevel || 'STUDY',
    defaultAE: ae?.defaultAE || false,
    enabled: ae?.enabled ?? true,
    connectionTimeout: ae?.connectionTimeout || 30000,
    responseTimeout: ae?.responseTimeout || 60000,
    maxAssociations: ae?.maxAssociations || 10,
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  const updateField = <K extends keyof AERequest>(field: K, value: AERequest[K]) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="px-6 py-4 border-b">
          <h3 className="text-lg font-semibold">
            {ae ? 'Edit PACS Configuration' : 'Add New PACS'}
          </h3>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* AE Title */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              AE Title *
            </label>
            <input
              type="text"
              required
              maxLength={16}
              value={formData.aeTitle}
              onChange={(e) => updateField('aeTitle', e.target.value.toUpperCase())}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., PACS_SERVER"
            />
          </div>

          {/* Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type *
            </label>
            <select
              value={formData.aeType}
              onChange={(e) => updateField('aeType', e.target.value as AEType)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              disabled={ae?.aeType === 'LOCAL'}
            >
              <option value="REMOTE_LEGACY">Legacy DICOM</option>
              <option value="REMOTE_DICOMWEB">DICOMweb</option>
              {ae?.aeType === 'LOCAL' && <option value="LOCAL">Local</option>}
            </select>
          </div>

          {/* Hostname */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Hostname *
            </label>
            <input
              type="text"
              required
              value={formData.hostname}
              onChange={(e) => updateField('hostname', e.target.value)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., pacs.hospital.org"
            />
          </div>

          {/* Port */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Port *
            </label>
            <input
              type="number"
              required
              min={1}
              max={65535}
              value={formData.port}
              onChange={(e) => updateField('port', parseInt(e.target.value))}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* DICOMweb URL (only for DICOMweb type) */}
          {formData.aeType === 'REMOTE_DICOMWEB' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                DICOMweb URL
              </label>
              <input
                type="url"
                value={formData.dicomWebUrl}
                onChange={(e) => updateField('dicomWebUrl', e.target.value)}
                className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., https://pacs.hospital.org/dicom-web"
              />
            </div>
          )}

          {/* Description */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <input
              type="text"
              value={formData.description}
              onChange={(e) => updateField('description', e.target.value)}
              className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Main Hospital PACS"
            />
          </div>

          {/* Advanced Settings */}
          <details className="border rounded p-3">
            <summary className="cursor-pointer text-sm font-medium text-gray-700">
              Advanced Settings
            </summary>
            <div className="mt-3 space-y-3">
              {/* Connection Timeout */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Connection Timeout (ms)
                </label>
                <input
                  type="number"
                  min={1000}
                  value={formData.connectionTimeout}
                  onChange={(e) => updateField('connectionTimeout', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Response Timeout */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Response Timeout (ms)
                </label>
                <input
                  type="number"
                  min={1000}
                  value={formData.responseTimeout}
                  onChange={(e) => updateField('responseTimeout', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* Max Associations */}
              <div>
                <label className="block text-sm text-gray-600 mb-1">
                  Max Associations
                </label>
                <input
                  type="number"
                  min={1}
                  max={50}
                  value={formData.maxAssociations}
                  onChange={(e) => updateField('maxAssociations', parseInt(e.target.value))}
                  className="w-full px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </details>

          {/* Checkboxes */}
          <div className="flex gap-6">
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={formData.enabled}
                onChange={(e) => updateField('enabled', e.target.checked)}
                className="rounded"
              />
              <span className="text-sm text-gray-700">Enabled</span>
            </label>
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={formData.defaultAE}
                onChange={(e) => updateField('defaultAE', e.target.checked)}
                className="rounded"
              />
              <span className="text-sm text-gray-700">Default PACS</span>
            </label>
          </div>

          {/* Actions */}
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 text-gray-700 bg-gray-100 rounded hover:bg-gray-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {isLoading ? 'Saving...' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PACSConfiguration;
