export interface PacsConfiguration {
  id: string;
  name: string;
  host: string;
  port: number;
  aeTitle: string;
  pacsType: 'LEGACY' | 'DICOMWEB';
  wadoRsUrl?: string;
  qidoRsUrl?: string;
  stowRsUrl?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Study {
  StudyInstanceUID: string;
  PatientID?: string;
  PatientName?: string;
  PatientBirthDate?: string;
  PatientSex?: string;
  StudyDate?: string;
  StudyTime?: string;
  StudyDescription?: string;
  AccessionNumber?: string;
  ModalitiesInStudy?: string;
  Modality?: string;
  NumberOfStudyRelatedSeries?: number;
  NumberOfStudyRelatedInstances?: number;
}

export interface Series {
  SeriesInstanceUID: string;
  SeriesNumber?: number;
  SeriesDescription?: string;
  Modality?: string;
  NumberOfSeriesRelatedInstances?: number;
  BodyPartExamined?: string;
}

export interface Instance {
  SOPInstanceUID: string;
  SOPClassUID?: string;
  InstanceNumber?: number;
  Rows?: number;
  Columns?: number;
}

export interface StudyQueryFilters {
  patientId?: string;
  patientName?: string;
  studyDate?: string;
  modality?: string;
  accessionNumber?: string;
}

export interface TestConnectionResult {
  success: boolean;
  responseTime?: number;
  message: string;
}

export interface MoveResult {
  success: boolean;
  completedSuboperations: number;
  failedSuboperations: number;
  warningSuboperations: number;
  errorMessage?: string;
}

// Phase 3: Measurement Types
export type MeasurementType =
  | 'LENGTH'
  | 'ANGLE'
  | 'COBB_ANGLE'
  | 'RECTANGLE_ROI'
  | 'ELLIPSE_ROI'
  | 'POLYGON_ROI'
  | 'FREEHAND_ROI'
  | 'BIDIRECTIONAL'
  | 'PROBE';

export interface Point2D {
  x: number;
  y: number;
}

export interface Point3D extends Point2D {
  z?: number;
}

export interface ROIStats {
  mean: number;
  stdDev: number;
  min: number;
  max: number;
  area: number;
  perimeter?: number;
  pixelCount?: number;
}

export interface Measurement {
  id: string;
  studyInstanceUid: string;
  seriesInstanceUid: string;
  sopInstanceUid: string;
  imageId?: string;
  frameIndex?: number;
  measurementType: MeasurementType;
  toolName: string;
  label?: string;
  value?: number;
  unit?: string;
  points: Point3D[];
  roiStats?: ROIStats;
  color?: string;
  visible: boolean;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Phase 3: Annotation Types
export type AnnotationType =
  | 'TEXT'
  | 'ARROW'
  | 'MARKER'
  | 'LINE'
  | 'RECTANGLE'
  | 'ELLIPSE'
  | 'POLYLINE';

export interface AnnotationStyle {
  lineWidth?: number;
  lineDash?: number[];
  shadow?: boolean;
  textBox?: {
    background: boolean;
    link: boolean;
  };
}

export interface Annotation {
  id: string;
  studyInstanceUid: string;
  seriesInstanceUid: string;
  sopInstanceUid: string;
  imageId?: string;
  frameIndex?: number;
  annotationType: AnnotationType;
  toolName: string;
  text?: string;
  points: Point3D[];
  style?: AnnotationStyle;
  color?: string;
  fontSize?: number;
  visible: boolean;
  locked: boolean;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Phase 3: Key Image Types
export interface KeyImage {
  id: string;
  studyInstanceUid: string;
  seriesInstanceUid: string;
  sopInstanceUid: string;
  imageId?: string;
  frameIndex?: number;
  instanceNumber?: number;
  description?: string;
  category?: string;
  windowWidth?: number;
  windowCenter?: number;
  thumbnailPath?: string;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface KeyImageToggleResult {
  action: 'added' | 'removed';
  isKeyImage: boolean;
  keyImage?: KeyImage;
}
