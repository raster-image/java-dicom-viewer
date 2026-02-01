/**
 * Application Entity configuration
 */
export interface ApplicationEntity {
  id: number;
  aeTitle: string;
  hostname: string;
  port: number;
  aeType: AEType;
  description?: string;
  dicomWebUrl?: string;
  queryRetrieveLevel: QueryRetrieveLevel;
  defaultAE: boolean;
  enabled: boolean;
  connectionTimeout: number;
  responseTimeout: number;
  maxAssociations: number;
  lastEchoStatus?: ConnectionStatus;
  lastEchoTime?: string;
  createdAt: string;
  updatedAt: string;
}

export type AEType = 'LOCAL' | 'REMOTE_LEGACY' | 'REMOTE_DICOMWEB';
export type QueryRetrieveLevel = 'PATIENT' | 'STUDY' | 'SERIES' | 'IMAGE';
export type ConnectionStatus = 'UNKNOWN' | 'SUCCESS' | 'FAILED' | 'TIMEOUT';

/**
 * Create/Update AE request
 */
export interface AERequest {
  aeTitle: string;
  hostname: string;
  port: number;
  aeType: AEType;
  description?: string;
  dicomWebUrl?: string;
  queryRetrieveLevel?: QueryRetrieveLevel;
  defaultAE?: boolean;
  enabled?: boolean;
  connectionTimeout?: number;
  responseTimeout?: number;
  maxAssociations?: number;
}

/**
 * Connection test result
 */
export interface ConnectionTestResult {
  aeId: number;
  status: ConnectionStatus;
  success: boolean;
}

/**
 * Retrieval progress model
 */
export interface RetrievalProgress {
  retrievalId: string;
  status: RetrievalStatus;
  total: number;
  completed: number;
  failed: number;
  warnings: number;
  errorMessage?: string;
  startTime: string;
  endTime?: string;
  percentComplete: number;
  remaining: number;
}

export type RetrievalStatus =
  | 'PENDING'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'COMPLETED_WITH_ERRORS'
  | 'FAILED'
  | 'CANCELLED';
