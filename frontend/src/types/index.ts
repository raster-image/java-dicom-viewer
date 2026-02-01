export interface PacsConfiguration {
  id: string
  name: string
  host: string
  port: number
  aeTitle: string
  pacsType: 'LEGACY' | 'DICOMWEB'
  wadoRsUrl?: string
  qidoRsUrl?: string
  stowRsUrl?: string
  isActive: boolean
  createdAt?: string
  updatedAt?: string
}

export interface Study {
  StudyInstanceUID: string
  PatientID?: string
  PatientName?: string
  PatientBirthDate?: string
  PatientSex?: string
  StudyDate?: string
  StudyTime?: string
  StudyDescription?: string
  AccessionNumber?: string
  ModalitiesInStudy?: string
  Modality?: string
  NumberOfStudyRelatedSeries?: number
  NumberOfStudyRelatedInstances?: number
}

export interface Series {
  SeriesInstanceUID: string
  SeriesNumber?: number
  SeriesDescription?: string
  Modality?: string
  NumberOfSeriesRelatedInstances?: number
  BodyPartExamined?: string
}

export interface Instance {
  SOPInstanceUID: string
  SOPClassUID?: string
  InstanceNumber?: number
  Rows?: number
  Columns?: number
}

export interface StudyQueryFilters {
  patientId?: string
  patientName?: string
  studyDate?: string
  modality?: string
  accessionNumber?: string
}

export interface TestConnectionResult {
  success: boolean
  responseTime?: number
  message: string
}

export interface MoveResult {
  success: boolean
  completedSuboperations: number
  failedSuboperations: number
  warningSuboperations: number
  errorMessage?: string
}
