import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../services/api';
import type { Annotation } from '../types';

interface AnnotationsPanelProps {
  studyInstanceUid: string;
  currentSopInstanceUid?: string;
}

export function AnnotationsPanel({ studyInstanceUid, currentSopInstanceUid }: AnnotationsPanelProps) {
  const [expandedAnnotation, setExpandedAnnotation] = useState<string | null>(null);
  const queryClient = useQueryClient();

  // Fetch annotations for the study
  const { data: annotationsData, isLoading } = useQuery({
    queryKey: ['annotations', studyInstanceUid],
    queryFn: () => apiClient.getAnnotationsByStudy(studyInstanceUid),
    enabled: !!studyInstanceUid,
  });

  // Delete annotation mutation
  const deleteMutation = useMutation({
    mutationFn: (id: string) => apiClient.deleteAnnotation(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['annotations', studyInstanceUid] });
    },
  });

  // Toggle visibility mutation
  const toggleVisibilityMutation = useMutation({
    mutationFn: (id: string) => apiClient.toggleAnnotationVisibility(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['annotations', studyInstanceUid] });
    },
  });

  // Toggle lock mutation
  const toggleLockMutation = useMutation({
    mutationFn: (id: string) => apiClient.toggleAnnotationLock(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['annotations', studyInstanceUid] });
    },
  });

  const annotations = (annotationsData?.annotations as Annotation[]) || [];

  if (isLoading) {
    return (
      <div style={{ padding: '16px', color: '#888' }}>
        Loading annotations...
      </div>
    );
  }

  if (annotations.length === 0) {
    return (
      <div style={{ padding: '16px', color: '#888' }}>
        No annotations available
      </div>
    );
  }

  const getToolIcon = (toolName: string): string => {
    const icons: Record<string, string> = {
      ArrowAnnotate: '➡️',
      PlanarFreehandROI: '✏️',
    };
    return icons[toolName] || '📝';
  };

  return (
    <div style={{ padding: '8px', maxHeight: '400px', overflowY: 'auto' }}>
      <div style={{ 
        fontSize: '14px', 
        fontWeight: 'bold', 
        marginBottom: '12px',
        color: '#fff'
      }}>
        Annotations ({annotations.length})
      </div>
      
      {annotations.map((annotation) => {
        const isExpanded = expandedAnnotation === annotation.id;
        const isOnCurrentImage = currentSopInstanceUid === annotation.sopInstanceUid;

        return (
          <div
            key={annotation.id}
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
              onClick={() => setExpandedAnnotation(isExpanded ? null : annotation.id)}
              style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
            >
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '13px', color: '#fff', marginBottom: '4px' }}>
                  {getToolIcon(annotation.toolName)} {annotation.toolName}
                </div>
                {annotation.text && (
                  <div style={{ fontSize: '12px', color: '#b8c7db', fontStyle: 'italic' }}>
                    "{annotation.text}"
                  </div>
                )}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                {annotation.locked && (
                  <span style={{ fontSize: '14px' }}>🔒</span>
                )}
                <span style={{ fontSize: '18px', color: '#888' }}>
                  {isExpanded ? '▼' : '▶'}
                </span>
              </div>
            </div>

            {isExpanded && (
              <div style={{ marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #2a3f54' }}>
                {/* Annotation Details */}
                <div style={{ marginBottom: '12px' }}>
                  <div style={{ fontSize: '11px', color: '#b8c7db' }}>
                    <div>Type: {annotation.annotationType}</div>
                    {annotation.color && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        Color: 
                        <div style={{
                          width: '16px',
                          height: '16px',
                          backgroundColor: annotation.color,
                          border: '1px solid #555',
                          borderRadius: '3px'
                        }} />
                      </div>
                    )}
                  </div>
                </div>

                {/* Action Buttons */}
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleVisibilityMutation.mutate(annotation.id);
                    }}
                    style={{
                      flex: '1 0 45%',
                      padding: '6px 12px',
                      fontSize: '11px',
                      backgroundColor: annotation.visible ? '#4a7a9f' : '#555',
                      color: '#fff',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    {annotation.visible ? '👁 Visible' : '🚫 Hidden'}
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleLockMutation.mutate(annotation.id);
                    }}
                    style={{
                      flex: '1 0 45%',
                      padding: '6px 12px',
                      fontSize: '11px',
                      backgroundColor: annotation.locked ? '#7a6a4f' : '#555',
                      color: '#fff',
                      border: 'none',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    {annotation.locked ? '🔒 Locked' : '🔓 Unlocked'}
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      if (window.confirm('Delete this annotation?')) {
                        deleteMutation.mutate(annotation.id);
                      }
                    }}
                    style={{
                      flex: '1 0 100%',
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
