import { api } from './api';
import type { ApplicationEntity, AERequest, ConnectionTestResult } from '../types/pacs';

export const pacsService = {
  /**
   * Get all configured Application Entities
   */
  async getAllAEs(): Promise<ApplicationEntity[]> {
    const response = await api.get<ApplicationEntity[]>('/ae');
    return response.data;
  },

  /**
   * Get a specific Application Entity by ID
   */
  async getAE(id: number): Promise<ApplicationEntity> {
    const response = await api.get<ApplicationEntity>(`/ae/${id}`);
    return response.data;
  },

  /**
   * Create a new Application Entity
   */
  async createAE(ae: AERequest): Promise<ApplicationEntity> {
    const response = await api.post<ApplicationEntity>('/ae', ae);
    return response.data;
  },

  /**
   * Update an existing Application Entity
   */
  async updateAE(id: number, ae: AERequest): Promise<ApplicationEntity> {
    const response = await api.put<ApplicationEntity>(`/ae/${id}`, ae);
    return response.data;
  },

  /**
   * Delete an Application Entity
   */
  async deleteAE(id: number): Promise<void> {
    await api.delete(`/ae/${id}`);
  },

  /**
   * Test connectivity to a remote AE using C-ECHO
   */
  async testConnection(id: number): Promise<ConnectionTestResult> {
    const response = await api.post<ConnectionTestResult>(`/ae/${id}/test`);
    return response.data;
  },

  /**
   * Test connectivity to all enabled remote AEs
   */
  async testAllConnections(): Promise<void> {
    await api.post('/ae/test-all');
  },
};
