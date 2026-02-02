import React, { useState, useEffect } from 'react';
import { pacsService } from '../services/pacsService';
import type { ApplicationEntity, ConnectionStatus } from '../types/pacs';

interface EchoResult {
  aeId: number;
  aeTitle: string;
  hostname: string;
  port: number;
  status: ConnectionStatus;
  success: boolean;
  responseTimeMs?: number;
  message?: string;
  timestamp: string;
}

interface PACSExplorerProps {
  onClose?: () => void;
}

export const PACSExplorer: React.FC<PACSExplorerProps> = ({ onClose }) => {
  const [aeList, setAeList] = useState<ApplicationEntity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [echoResults, setEchoResults] = useState<Map<number, EchoResult>>(new Map());
  const [testingIds, setTestingIds] = useState<Set<number>>(new Set());
  const [isTestingAll, setIsTestingAll] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [autoRefresh, setAutoRefresh] = useState(false);
  const [refreshInterval, setRefreshInterval] = useState(30);
  const [filterStatus, setFilterStatus] = useState<'all' | 'success' | 'failed' | 'unknown'>('all');

  // Load AEs on mount
  useEffect(() => {
    loadAEs();
  }, []);

  // Auto-refresh functionality
  useEffect(() => {
    let intervalId: NodeJS.Timeout | null = null;

    if (autoRefresh && refreshInterval > 0) {
      intervalId = setInterval(() => {
        testAllConnections();
      }, refreshInterval * 1000);
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [autoRefresh, refreshInterval]);

  const loadAEs = async () => {
    try {
      setIsLoading(true);
      const data = await pacsService.getAllAEs();
      setAeList(data);
      setError(null);

      // Initialize echo results from stored status
      const initialResults = new Map<number, EchoResult>();
      data.forEach((ae) => {
        if (ae.lastEchoStatus) {
          initialResults.set(ae.id, {
            aeId: ae.id,
            aeTitle: ae.aeTitle,
            hostname: ae.hostname,
            port: ae.port,
            status: ae.lastEchoStatus,
            success: ae.lastEchoStatus === 'SUCCESS',
            timestamp: ae.lastEchoTime || new Date().toISOString(),
          });
        }
      });
      setEchoResults(initialResults);
    } catch (err) {
      setError('Failed to load PACS servers');
    } finally {
      setIsLoading(false);
    }
  };

  const testConnection = async (ae: ApplicationEntity) => {
    try {
      setTestingIds((prev) => new Set(prev).add(ae.id));
      const startTime = Date.now();
      const result = await pacsService.testConnection(ae.id);
      const responseTime = Date.now() - startTime;

      const echoResult: EchoResult = {
        aeId: ae.id,
        aeTitle: ae.aeTitle,
        hostname: ae.hostname,
        port: ae.port,
        status: result.status as ConnectionStatus,
        success: result.success,
        responseTimeMs: responseTime,
        message: result.success ? 'Connection successful' : 'Connection failed',
        timestamp: new Date().toISOString(),
      };

      setEchoResults((prev) => new Map(prev).set(ae.id, echoResult));

      // Update the AE in the list with new status
      setAeList((prev) =>
        prev.map((item) =>
          item.id === ae.id
            ? { ...item, lastEchoStatus: result.status as ConnectionStatus, lastEchoTime: echoResult.timestamp }
            : item
        )
      );
    } catch (err: any) {
      const echoResult: EchoResult = {
        aeId: ae.id,
        aeTitle: ae.aeTitle,
        hostname: ae.hostname,
        port: ae.port,
        status: 'FAILED',
        success: false,
        message: err.message || 'Connection test failed',
        timestamp: new Date().toISOString(),
      };
      setEchoResults((prev) => new Map(prev).set(ae.id, echoResult));
    } finally {
      setTestingIds((prev) => {
        const next = new Set(prev);
        next.delete(ae.id);
        return next;
      });
    }
  };

  const testAllConnections = async () => {
    const enabledAEs = aeList.filter((ae) => ae.enabled);
    if (enabledAEs.length === 0) {
      setError('No enabled PACS servers to test');
      return;
    }

    setIsTestingAll(true);
    setError(null);

    // Test all connections sequentially to avoid overwhelming the network
    for (const ae of enabledAEs) {
      await testConnection(ae);
    }

    setIsTestingAll(false);
  };

  const getStatusBadge = (status?: ConnectionStatus, isLoading = false) => {
    if (isLoading) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-900 text-blue-300">
          <svg className="animate-spin -ml-1 mr-1 h-3 w-3" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            />
          </svg>
          Testing...
        </span>
      );
    }

    switch (status) {
      case 'SUCCESS':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-900 text-green-300">
            <svg className="mr-1 h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clipRule="evenodd"
              />
            </svg>
            Connected
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-900 text-red-300">
            <svg className="mr-1 h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            Failed
          </span>
        );
      case 'TIMEOUT':
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-yellow-900 text-yellow-300">
            <svg className="mr-1 h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
                clipRule="evenodd"
              />
            </svg>
            Timeout
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-700 text-gray-300">
            <svg className="mr-1 h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z"
                clipRule="evenodd"
              />
            </svg>
            Unknown
          </span>
        );
    }
  };

  const getAETypeIcon = (aeType: string) => {
    switch (aeType) {
      case 'LOCAL':
        return '🏠';
      case 'REMOTE_DICOMWEB':
        return '🌐';
      case 'REMOTE_LEGACY':
        return '📡';
      default:
        return '📦';
    }
  };

  const formatTimestamp = (timestamp?: string) => {
    if (!timestamp) return 'Never';
    const date = new Date(timestamp);
    return date.toLocaleString();
  };

  const filteredAEs = aeList.filter((ae) => {
    if (filterStatus === 'all') return true;
    const result = echoResults.get(ae.id);
    if (filterStatus === 'success') return result?.status === 'SUCCESS';
    if (filterStatus === 'failed') return result?.status === 'FAILED' || result?.status === 'TIMEOUT';
    if (filterStatus === 'unknown') return !result?.status || result?.status === 'UNKNOWN';
    return true;
  });

  const stats = {
    total: aeList.length,
    enabled: aeList.filter((ae) => ae.enabled).length,
    connected: Array.from(echoResults.values()).filter((r) => r.status === 'SUCCESS').length,
    failed: Array.from(echoResults.values()).filter((r) => r.status === 'FAILED' || r.status === 'TIMEOUT').length,
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-400">Loading PACS servers...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 p-6">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">PACS Explorer</h1>
          <p className="text-gray-400 mt-1">Browse and test connectivity to PACS servers using DICOM C-ECHO</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600 transition-colors"
          >
            Close
          </button>
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
          <div className="text-3xl font-bold text-white">{stats.total}</div>
          <div className="text-gray-400 text-sm">Total Servers</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
          <div className="text-3xl font-bold text-blue-400">{stats.enabled}</div>
          <div className="text-gray-400 text-sm">Enabled</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
          <div className="text-3xl font-bold text-green-400">{stats.connected}</div>
          <div className="text-gray-400 text-sm">Connected</div>
        </div>
        <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
          <div className="text-3xl font-bold text-red-400">{stats.failed}</div>
          <div className="text-gray-400 text-sm">Failed</div>
        </div>
      </div>

      {/* Controls */}
      <div className="bg-gray-800 rounded-lg p-4 border border-gray-700 mb-6">
        <div className="flex flex-wrap items-center gap-4">
          <button
            onClick={testAllConnections}
            disabled={isTestingAll || aeList.length === 0}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 transition-colors"
          >
            {isTestingAll ? (
              <>
                <svg className="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                Testing All...
              </>
            ) : (
              <>
                <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M13 10V3L4 14h7v7l9-11h-7z"
                  />
                </svg>
                Test All Connections
              </>
            )}
          </button>

          <button
            onClick={loadAEs}
            className="px-4 py-2 bg-gray-700 text-white rounded hover:bg-gray-600 flex items-center gap-2 transition-colors"
          >
            <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
            Refresh List
          </button>

          <div className="flex items-center gap-2 ml-auto">
            <label className="text-gray-400 text-sm">Auto-refresh:</label>
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(e) => setAutoRefresh(e.target.checked)}
              className="rounded bg-gray-700 border-gray-600 text-blue-600 focus:ring-blue-500"
            />
            {autoRefresh && (
              <select
                value={refreshInterval}
                onChange={(e) => setRefreshInterval(Number(e.target.value))}
                className="bg-gray-700 text-white rounded px-2 py-1 text-sm border border-gray-600"
              >
                <option value={15}>15s</option>
                <option value={30}>30s</option>
                <option value={60}>1m</option>
                <option value={300}>5m</option>
              </select>
            )}
          </div>
        </div>

        {/* Filter */}
        <div className="flex items-center gap-2 mt-4 pt-4 border-t border-gray-700">
          <label className="text-gray-400 text-sm">Filter:</label>
          <div className="flex gap-2">
            {(['all', 'success', 'failed', 'unknown'] as const).map((status) => (
              <button
                key={status}
                onClick={() => setFilterStatus(status)}
                className={`px-3 py-1 rounded text-sm capitalize transition-colors ${
                  filterStatus === status
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
                }`}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Error message */}
      {error && (
        <div className="mb-6 p-4 bg-red-900/50 border border-red-700 rounded-lg flex items-center justify-between">
          <span className="text-red-300">{error}</span>
          <button onClick={() => setError(null)} className="text-red-400 hover:text-red-300">
            <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </div>
      )}

      {/* PACS Server List */}
      {filteredAEs.length === 0 ? (
        <div className="bg-gray-800 rounded-lg p-12 border border-gray-700 text-center">
          <svg className="mx-auto h-12 w-12 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2m-2-4h.01M17 16h.01"
            />
          </svg>
          <h3 className="mt-4 text-lg font-medium text-white">No PACS servers found</h3>
          <p className="mt-2 text-gray-400">
            {aeList.length === 0
              ? 'Configure your PACS connections in the Settings page.'
              : 'No servers match the current filter.'}
          </p>
          {aeList.length === 0 && (
            <a
              href="/settings"
              className="mt-4 inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
            >
              Configure PACS
            </a>
          )}
        </div>
      ) : (
        <div className="grid gap-4">
          {filteredAEs.map((ae) => {
            const result = echoResults.get(ae.id);
            const isTesting = testingIds.has(ae.id);

            return (
              <div
                key={ae.id}
                className={`bg-gray-800 rounded-lg border transition-all ${
                  !ae.enabled
                    ? 'border-gray-700 opacity-60'
                    : result?.status === 'SUCCESS'
                    ? 'border-green-700'
                    : result?.status === 'FAILED' || result?.status === 'TIMEOUT'
                    ? 'border-red-700'
                    : 'border-gray-700'
                }`}
              >
                <div className="p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">{getAETypeIcon(ae.aeType)}</span>
                      <div>
                        <div className="flex items-center gap-2">
                          <h3 className="text-lg font-semibold text-white">{ae.aeTitle}</h3>
                          {ae.defaultAE && (
                            <span className="px-2 py-0.5 bg-yellow-900 text-yellow-300 text-xs rounded-full">
                              Default
                            </span>
                          )}
                          {!ae.enabled && (
                            <span className="px-2 py-0.5 bg-gray-700 text-gray-400 text-xs rounded-full">
                              Disabled
                            </span>
                          )}
                        </div>
                        <p className="text-gray-400 text-sm mt-1">
                          {ae.hostname}:{ae.port}
                        </p>
                        {ae.description && <p className="text-gray-500 text-sm mt-1">{ae.description}</p>}
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      {getStatusBadge(result?.status || ae.lastEchoStatus, isTesting)}
                      <button
                        onClick={() => testConnection(ae)}
                        disabled={isTesting || !ae.enabled}
                        className="px-3 py-1.5 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                      >
                        {isTesting ? 'Testing...' : 'Test'}
                      </button>
                    </div>
                  </div>

                  {/* Details */}
                  <div className="mt-4 grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <span className="text-gray-500">Type:</span>
                      <span className="ml-2 text-gray-300">{ae.aeType.replace('_', ' ')}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Query Level:</span>
                      <span className="ml-2 text-gray-300">{ae.queryRetrieveLevel}</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Timeout:</span>
                      <span className="ml-2 text-gray-300">{ae.connectionTimeout}s</span>
                    </div>
                    <div>
                      <span className="text-gray-500">Last Test:</span>
                      <span className="ml-2 text-gray-300">
                        {formatTimestamp(result?.timestamp || ae.lastEchoTime)}
                      </span>
                    </div>
                  </div>

                  {/* Echo Result Details */}
                  {result && (
                    <div
                      className={`mt-4 p-3 rounded ${
                        result.success ? 'bg-green-900/30' : 'bg-red-900/30'
                      }`}
                    >
                      <div className="flex items-center justify-between text-sm">
                        <div className="flex items-center gap-4">
                          <span className={result.success ? 'text-green-300' : 'text-red-300'}>
                            {result.message}
                          </span>
                          {result.responseTimeMs !== undefined && (
                            <span className="text-gray-400">
                              Response time: <span className="text-white">{result.responseTimeMs}ms</span>
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Legend */}
      <div className="mt-8 bg-gray-800 rounded-lg p-4 border border-gray-700">
        <h3 className="text-lg font-semibold text-white mb-3">Legend</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div className="flex items-center gap-2">
            <span className="text-2xl">🏠</span>
            <span className="text-gray-300">Local PACS</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-2xl">📡</span>
            <span className="text-gray-300">Remote Legacy PACS</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-2xl">🌐</span>
            <span className="text-gray-300">Remote DICOMweb</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-2xl">📦</span>
            <span className="text-gray-300">Other</span>
          </div>
        </div>
        <div className="mt-4 pt-4 border-t border-gray-700">
          <p className="text-gray-400 text-sm">
            <strong className="text-white">C-ECHO</strong> (Verification SOP Class) is used to test connectivity to DICOM Application Entities.
            A successful C-ECHO indicates that the remote PACS is reachable and responding to DICOM network requests.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PACSExplorer;
