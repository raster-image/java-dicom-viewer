import axios from 'axios';
import type {
  PacsConfiguration,
  StudyQueryFilters,
  Measurement,
  Annotation,
  KeyImage,
  KeyImageToggleResult,
} from '../types';

export const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const apiClient = {
  // Health
  getHealth: async () => {
    const response = await api.get('/actuator/health');
    return response.data;
  },

  // PACS Configuration
  getPacsConfigurations: async () => {
    const response = await api.get('/pacs');
    return response.data;
  },

  getPacs: async (id: string) => {
    const response = await api.get(`/pacs/${id}`);
    return response.data;
  },

  createPacs: async (pacs: Omit<PacsConfiguration, 'id'>) => {
    const response = await api.post('/pacs', pacs);
    return response.data;
  },

  updatePacs: async (id: string, pacs: PacsConfiguration) => {
    const response = await api.put(`/pacs/${id}`, pacs);
    return response.data;
  },

  deletePacs: async (id: string) => {
    await api.delete(`/pacs/${id}`);
  },

  testPacsConnection: async (id: string) => {
    const response = await api.post(`/pacs/${id}/test`);
    return response.data;
  },

  // Studies
  queryStudies: async (pacsId: string, filters: StudyQueryFilters) => {
    const params = new URLSearchParams({ pacsId });
    if (filters.patientId) params.append('patientId', filters.patientId);
    if (filters.patientName) params.append('patientName', filters.patientName);
    if (filters.studyDate) params.append('studyDate', filters.studyDate);
    if (filters.modality) params.append('modality', filters.modality);
    if (filters.accessionNumber) params.append('accessionNumber', filters.accessionNumber);

    const response = await api.get(`/studies?${params.toString()}`);
    return response.data;
  },

  querySeries: async (pacsId: string, studyInstanceUid: string) => {
    const response = await api.get(`/studies/${studyInstanceUid}/series?pacsId=${pacsId}`);
    return response.data;
  },

  queryInstances: async (pacsId: string, studyInstanceUid: string, seriesInstanceUid: string) => {
    const response = await api.get(
      `/studies/${studyInstanceUid}/series/${seriesInstanceUid}/instances?pacsId=${pacsId}`
    );
    return response.data;
  },

  retrieveStudy: async (pacsId: string, studyInstanceUid: string, destinationAe: string) => {
    const response = await api.post(
      `/studies/${studyInstanceUid}/retrieve?pacsId=${pacsId}&destinationAe=${destinationAe}`
    );
    return response.data;
  },

  // Phase 3: Measurements API
  createMeasurement: async (measurement: Omit<Measurement, 'id' | 'createdAt' | 'updatedAt'>) => {
    const payload = {
      ...measurement,
      pointsJson: JSON.stringify(measurement.points),
      roiStatsJson: measurement.roiStats ? JSON.stringify(measurement.roiStats) : null,
    };
    const response = await api.post('/measurements', payload);
    return response.data as Measurement;
  },

  getMeasurement: async (id: string) => {
    const response = await api.get(`/measurements/${id}`);
    return response.data as Measurement;
  },

  updateMeasurement: async (id: string, measurement: Partial<Measurement>) => {
    const payload = {
      ...measurement,
      pointsJson: measurement.points ? JSON.stringify(measurement.points) : undefined,
      roiStatsJson: measurement.roiStats ? JSON.stringify(measurement.roiStats) : undefined,
    };
    const response = await api.put(`/measurements/${id}`, payload);
    return response.data as Measurement;
  },

  deleteMeasurement: async (id: string) => {
    await api.delete(`/measurements/${id}`);
  },

  getMeasurementsByStudy: async (studyInstanceUid: string, visibleOnly = false) => {
    const response = await api.get(
      `/measurements/study/${studyInstanceUid}?visibleOnly=${visibleOnly}`
    );
    return response.data as {
      studyInstanceUid: string;
      count: number;
      measurements: Measurement[];
    };
  },

  getMeasurementsBySeries: async (seriesInstanceUid: string) => {
    const response = await api.get(`/measurements/series/${seriesInstanceUid}`);
    return response.data as {
      seriesInstanceUid: string;
      count: number;
      measurements: Measurement[];
    };
  },

  getMeasurementsByInstance: async (sopInstanceUid: string, frameIndex?: number) => {
    const params = frameIndex !== undefined ? `?frameIndex=${frameIndex}` : '';
    const response = await api.get(`/measurements/instance/${sopInstanceUid}${params}`);
    return response.data as { sopInstanceUid: string; count: number; measurements: Measurement[] };
  },

  toggleMeasurementVisibility: async (id: string) => {
    const response = await api.post(`/measurements/${id}/toggle-visibility`);
    return response.data as Measurement;
  },

  // Phase 3: Annotations API
  createAnnotation: async (annotation: Omit<Annotation, 'id' | 'createdAt' | 'updatedAt'>) => {
    const payload = {
      ...annotation,
      pointsJson: JSON.stringify(annotation.points),
      styleJson: annotation.style ? JSON.stringify(annotation.style) : null,
    };
    const response = await api.post('/annotations', payload);
    return response.data as Annotation;
  },

  getAnnotation: async (id: string) => {
    const response = await api.get(`/annotations/${id}`);
    return response.data as Annotation;
  },

  updateAnnotation: async (id: string, annotation: Partial<Annotation>) => {
    const payload = {
      ...annotation,
      pointsJson: annotation.points ? JSON.stringify(annotation.points) : undefined,
      styleJson: annotation.style ? JSON.stringify(annotation.style) : undefined,
    };
    const response = await api.put(`/annotations/${id}`, payload);
    return response.data as Annotation;
  },

  deleteAnnotation: async (id: string) => {
    await api.delete(`/annotations/${id}`);
  },

  getAnnotationsByStudy: async (studyInstanceUid: string, visibleOnly = false) => {
    const response = await api.get(
      `/annotations/study/${studyInstanceUid}?visibleOnly=${visibleOnly}`
    );
    return response.data as { studyInstanceUid: string; count: number; annotations: Annotation[] };
  },

  getAnnotationsBySeries: async (seriesInstanceUid: string) => {
    const response = await api.get(`/annotations/series/${seriesInstanceUid}`);
    return response.data as { seriesInstanceUid: string; count: number; annotations: Annotation[] };
  },

  getAnnotationsByInstance: async (sopInstanceUid: string, frameIndex?: number) => {
    const params = frameIndex !== undefined ? `?frameIndex=${frameIndex}` : '';
    const response = await api.get(`/annotations/instance/${sopInstanceUid}${params}`);
    return response.data as { sopInstanceUid: string; count: number; annotations: Annotation[] };
  },

  toggleAnnotationVisibility: async (id: string) => {
    const response = await api.post(`/annotations/${id}/toggle-visibility`);
    return response.data as Annotation;
  },

  toggleAnnotationLock: async (id: string) => {
    const response = await api.post(`/annotations/${id}/toggle-lock`);
    return response.data as Annotation;
  },

  // Phase 3: Key Images API
  createKeyImage: async (keyImage: Omit<KeyImage, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await api.post('/key-images', keyImage);
    return response.data as KeyImage;
  },

  toggleKeyImage: async (keyImage: Omit<KeyImage, 'id' | 'createdAt' | 'updatedAt'>) => {
    const response = await api.post('/key-images/toggle', keyImage);
    return response.data as KeyImageToggleResult;
  },

  getKeyImage: async (id: string) => {
    const response = await api.get(`/key-images/${id}`);
    return response.data as KeyImage;
  },

  updateKeyImage: async (id: string, keyImage: Partial<KeyImage>) => {
    const response = await api.put(`/key-images/${id}`, keyImage);
    return response.data as KeyImage;
  },

  deleteKeyImage: async (id: string) => {
    await api.delete(`/key-images/${id}`);
  },

  getKeyImagesByStudy: async (studyInstanceUid: string, category?: string) => {
    const params = category ? `?category=${encodeURIComponent(category)}` : '';
    const response = await api.get(`/key-images/study/${studyInstanceUid}${params}`);
    return response.data as { studyInstanceUid: string; count: number; keyImages: KeyImage[] };
  },

  getKeyImagesBySeries: async (seriesInstanceUid: string) => {
    const response = await api.get(`/key-images/series/${seriesInstanceUid}`);
    return response.data as { seriesInstanceUid: string; count: number; keyImages: KeyImage[] };
  },

  checkKeyImage: async (sopInstanceUid: string, frameIndex = 0) => {
    const response = await api.get(
      `/key-images/check?sopInstanceUid=${sopInstanceUid}&frameIndex=${frameIndex}`
    );
    return response.data as { sopInstanceUid: string; frameIndex: number; isKeyImage: boolean };
  },
};
