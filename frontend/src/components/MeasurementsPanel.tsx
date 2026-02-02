import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import type { Measurement } from '../types';

interface MeasurementsPanelProps {
  studyInstanceUid: string;
  currentSopInstanceUid?: string;
  currentModality?: string;
}

export function MeasurementsPanel({ 
  studyInstanceUid, 
  currentSopInstanceUid,
  currentModality
}: MeasurementsPanelProps) {
  const [expandedMeasurement, setExpandedMeasurement] = useState<string | null>(null);
  const queryClient = useQueryClient();

  // Fetch measurements for the study
  const { data: measurementsData, isLoading } = useQuery({
    queryKey: ['measurements', studyInstanceUid],
    queryFn: () => apiClient.getMeasurementsByStudy(studyInstanceUid),
    enabled: !!studyInstanceUid,
  });

  // Delete measurement mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => apiClient.deleteMeasurement(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['measurements', studyInstanceUid] });
    },
  });

  // Toggle visibility mutation
  const toggleVisibilityMutation = useMutation({
    mutationFn: (id: string) => apiClient.toggleMeasurementVisibility(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['measurements', studyInstanceUid] });
    },
  });

  const measurements = (measurementsData?.measurements as Measurement[]) || [];

  if (isLoading) {
    return (
      <div style={{ padding: '16px', color: '#888' }}>
        Loading measurements...
      </div>
    );
  }

  if (measurements.length === 0) {
    return (
      <div style={{ padding: '16px', color: '#888' }}>
        No measurements available
      </div>
    );
  }

  const formatValue = (measurement: Measurement): string => {
    if (measurement.value === undefined) return 'N/A';
    return `${measurement.value.toFixed(2)} ${measurement.unit || ''}`;
  };

  const formatStatValue = (value: number, statType: 'value' | 'area', modality?: string): string => {
    // Only show HU (Hounsfield Units) for CT images
    const unit = statType === 'value' && modality === 'CT' ? ' HU' : '';
    return `${value.toFixed(2)}${unit}`;
  };

  const formatAreaValue = (value: number): string => {
    return `${value.toFixed(2)} mm²`;
  };

  const getToolIcon = (toolName: string): string => {
    const icons: Record<string, string> = {
      Length: '📏',
      Angle: '📐',
      CobbAngle: '📐',
      RectangleROI: '▭',
      EllipticalROI: '⬭',
      Probe: '🎯',
    };
    return icons[toolName] || '📊';
  };

  return (
    <div style={{ padding: '8px', maxHeight: '400px', overflowY: 'auto' }}>
      <div style={{ 
        fontSize: '14px', 
        fontWeight: 'bold', 
        marginBottom: '12px',
        color: '#fff'
      }}>
        Measurements ({measurements.length})
      </div>
      
      {measurements.map((measurement) => {
        const isExpanded = expandedMeasurement === measurement.id;
        const isOnCurrentImage = currentSopInstanceUid === measurement.sopInstanceUid;

        return (
          <div
            key={measurement.id}
            style={{
              marginBottom: '8px',
              padding: '10px',
              backgroundColor: isOnCurrentImage ? '#2a4a66' : '#1e2936',
              borderRadius: '6px',
              border: isOnCurrentImage ? '1px solid #4a7a9f' : '1px solid #2a3f54',
              cursor: 'pointer',
            }}
          >
            <div
              onClick={() => setExpandedMeasurement(isExpanded ? null : measurement.id)}
              style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '13px', color: '#fff', marginBottom: '4px' }}>
                  {getToolIcon(measurement.toolName)} {measurement.toolName}
                </div>
                <div style={{ fontSize: '12px', color: '#b8c7db' }}>
                  {formatValue(measurement)}
                </div>
              </div>
              <div style={{ fontSize: '18px', color: '#888' }}>
                {isExpanded ? '▼' : '▶'}
              </div>
            </div>

            {isExpanded && (
              <div style={{ marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #2a3f54' }}>
                {/* ROI Statistics */}
                {measurement.roiStats && (
                  <div style={{ marginBottom: '12px' }}>
                    <div style={{ fontSize: '11px', fontWeight: 'bold', color: '#888', marginBottom: '6px' }}>
                      ROI Statistics:
                    </div>
                    <div style={{ fontSize: '11px', color: '#b8c7db' }}>
                      <div>Mean: {formatStatValue(measurement.roiStats.mean, 'value', currentModality)}</div>
                      <div>Std Dev: {formatStatValue(measurement.roiStats.stdDev, 'value', currentModality)}</div>
                      <div>Min: {formatStatValue(measurement.roiStats.min, 'value', currentModality)}</div>
                      <div>Max: {formatStatValue(measurement.roiStats.max, 'value', currentModality)}</div>
                      {measurement.roiStats.area && (
                        <div>Area: {formatAreaValue(measurement.roiStats.area)}</div>
                      )}
                    </div>
                  </div>
                )}

                {/* Action Buttons */}
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleVisibilityMutation.mutate(measurement.id);
                    }}
                    style={{
                      flex: 1,
                      padding: '6px 12px',
                      fontSize: '11px',
                      backgroundColor: measurement.visible ? '#4a7a9f' : '#555',
                      color: '#fff',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    {measurement.visible ? '👁 Visible' : '🚫 Hidden'}
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      if (window.confirm('Delete this measurement?')) {
                        deleteMutation.mutate(measurement.id);
                      }
                    }}
                    style={{
                      flex: 1,
                      padding: '6px 12px',
                      fontSize: '11px',
                      backgroundColor: '#aa3333',
                      color: '#fff',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    🗑 Delete
                  </button>
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
