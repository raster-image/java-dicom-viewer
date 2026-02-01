import axios from 'axios'
import type { PacsConfiguration, Study, StudyQueryFilters } from '../types'

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const apiClient = {
  // Health
  getHealth: async () => {
    const response = await api.get('/health')
    return response.data
  },

  // PACS Configuration
  getPacsConfigurations: async () => {
    const response = await api.get('/pacs')
    return response.data
  },

  getPacs: async (id: string) => {
    const response = await api.get(`/pacs/${id}`)
    return response.data
  },

  createPacs: async (pacs: Omit<PacsConfiguration, 'id'>) => {
    const response = await api.post('/pacs', pacs)
    return response.data
  },

  updatePacs: async (id: string, pacs: PacsConfiguration) => {
    const response = await api.put(`/pacs/${id}`, pacs)
    return response.data
  },

  deletePacs: async (id: string) => {
    await api.delete(`/pacs/${id}`)
  },

  testPacsConnection: async (id: string) => {
    const response = await api.post(`/pacs/${id}/test`)
    return response.data
  },

  // Studies
  queryStudies: async (pacsId: string, filters: StudyQueryFilters) => {
    const params = new URLSearchParams({ pacsId })
    if (filters.patientId) params.append('patientId', filters.patientId)
    if (filters.patientName) params.append('patientName', filters.patientName)
    if (filters.studyDate) params.append('studyDate', filters.studyDate)
    if (filters.modality) params.append('modality', filters.modality)
    if (filters.accessionNumber) params.append('accessionNumber', filters.accessionNumber)

    const response = await api.get(`/studies?${params.toString()}`)
    return response.data
  },

  querySeries: async (pacsId: string, studyInstanceUid: string) => {
    const response = await api.get(`/studies/${studyInstanceUid}/series?pacsId=${pacsId}`)
    return response.data
  },

  queryInstances: async (pacsId: string, studyInstanceUid: string, seriesInstanceUid: string) => {
    const response = await api.get(
      `/studies/${studyInstanceUid}/series/${seriesInstanceUid}/instances?pacsId=${pacsId}`
    )
    return response.data
  },

  retrieveStudy: async (pacsId: string, studyInstanceUid: string, destinationAe: string) => {
    const response = await api.post(
      `/studies/${studyInstanceUid}/retrieve?pacsId=${pacsId}&destinationAe=${destinationAe}`
    )
    return response.data
  },
}
