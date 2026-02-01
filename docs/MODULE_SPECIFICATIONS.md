# DICOM Viewer Module Specifications

This document provides detailed specifications for all viewer modules in the Java DICOM Viewer application. It serves as a comprehensive guide for development, testing, and feature planning.

## Table of Contents

1. [Core DICOM Viewer Modules (2D)](#1-core-dicom-viewer-modules-2d)
2. [Advanced Reconstruction Modules (3D)](#2-advanced-reconstruction-modules-3d)
3. [Specialized Clinical 3D Modules](#3-specialized-clinical-3d-modules)
4. [Advanced Post-Processing & AI-Ready Features](#4-advanced-post-processing--ai-ready-features)
5. [Reporting & Workflow Integration](#5-reporting--workflow-integration)
6. [DICOM & Interoperability Standards](#6-dicom--interoperability-standards)
7. [Optional & Premium 3D Add-Ons](#7-optional--premium-3d-add-ons)

---

## 1. Core DICOM Viewer Modules (2D)

### 1.A Image Viewing & Navigation

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Multi-planar image viewing | Axial, Sagittal, Coronal plane support | High | 1 |
| Stack scrolling | Mouse wheel, keyboard arrows, touch gestures | High | 1 |
| Window Level (WL) / Window Width (WW) | Adjustable presets and custom values | High | 1 |
| Zoom | Mouse wheel, pinch, toolbar buttons | High | 1 |
| Pan | Click-drag, touch gestures | High | 1 |
| Rotate | 90° increments, free rotation | Medium | 2 |
| Flip | Horizontal and vertical flipping | Medium | 2 |
| Cine loop playback | Adjustable speed (1-60 fps), loop modes | Medium | 2 |
| Image inversion | Grayscale inversion toggle | Medium | 1 |
| Grayscale mapping | LUT presets, custom mapping | Medium | 2 |
| Series synchronization | Link/unlink series scrolling | High | 2 |
| Reference lines | Cross-reference lines between viewports | Medium | 3 |
| Multi-monitor support | Extend viewer across multiple displays | Low | 4 |

#### Technical Implementation

```typescript
// Cornerstone.js Tool Configuration
interface ViewerToolConfig {
  windowLevel: {
    presets: WindowLevelPreset[];
    defaultPreset: string;
  };
  zoom: {
    minScale: number;
    maxScale: number;
    zoomSpeed: number;
  };
  pan: {
    enabled: boolean;
  };
  scroll: {
    scrollMethod: 'mouse' | 'keyboard' | 'touch';
    loop: boolean;
  };
  cine: {
    defaultFps: number;
    maxFps: number;
  };
}

interface WindowLevelPreset {
  name: string;
  windowWidth: number;
  windowCenter: number;
  modality?: string; // CT, MR, etc.
}
```

#### Default Window/Level Presets

| Preset Name | Window Width | Window Center | Modality |
|-------------|--------------|---------------|----------|
| CT Abdomen | 400 | 50 | CT |
| CT Lung | 1500 | -600 | CT |
| CT Bone | 2000 | 500 | CT |
| CT Brain | 80 | 40 | CT |
| CT Soft Tissue | 350 | 50 | CT |
| MR T1 | 500 | 250 | MR |
| MR T2 | 400 | 200 | MR |
| MR FLAIR | 1200 | 600 | MR |

---

### 1.B Measurement & Annotation Tools

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Linear measurement | Distance between two points | High | 2 |
| Angle measurement | Angle between three points | High | 2 |
| ROI measurements | Rectangle, ellipse, polygon | High | 2 |
| Area measurement | Calculate region area in mm² | High | 2 |
| Perimeter measurement | Calculate region perimeter | Medium | 2 |
| Pixel value (HU) | Display Hounsfield units for CT | High | 2 |
| Freehand ROI | Draw custom regions | Medium | 3 |
| Text annotations | Add text labels | Medium | 2 |
| Arrow annotations | Directional markers | Medium | 2 |
| Graphical annotations | Lines, shapes, markers | Medium | 2 |
| Cobb's Angle | Spinal curvature measurement | Medium | 3 |
| Distance calibration | Calibrate measurements | Low | 4 |
| Lesion tracking | Track lesions across studies | Medium | 4 |
| Measurement comparison | Compare measurements across studies | Medium | 4 |

#### Measurement Tool Specifications

```typescript
interface MeasurementTool {
  id: string;
  type: 'length' | 'angle' | 'roi' | 'area' | 'pixel';
  points: Point3D[];
  value?: number;
  unit: string;
  studyInstanceUID: string;
  seriesInstanceUID: string;
  sopInstanceUID: string;
  imageId: string;
  createdAt: Date;
  createdBy: string;
  annotation?: string;
}

interface Point3D {
  x: number;
  y: number;
  z?: number;
}

interface ROIMeasurement extends MeasurementTool {
  type: 'roi';
  shape: 'rectangle' | 'ellipse' | 'polygon' | 'freehand';
  stats: {
    mean: number;
    stdDev: number;
    min: number;
    max: number;
    area: number;
    perimeter: number;
  };
}
```

---

### 1.C Image Processing

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Sharpening | Edge enhancement filter | Medium | 3 |
| Smoothing | Noise reduction filter | Medium | 3 |
| Edge enhancement | Sobel, Laplacian filters | Medium | 3 |
| Noise reduction | Median, Gaussian filters | Medium | 3 |
| Magnification | Digital zoom with interpolation | Medium | 2 |
| Interpolation | Bilinear, bicubic options | Medium | 2 |
| Image fusion | Overlay multiple series | Low | 4 |
| Key image marking | Flag important images | High | 2 |

#### Image Processing Filters

```typescript
interface ImageFilter {
  name: string;
  type: 'convolution' | 'morphological' | 'intensity';
  kernel?: number[][];
  parameters?: Record<string, number>;
}

const FILTERS: ImageFilter[] = [
  {
    name: 'Sharpen',
    type: 'convolution',
    kernel: [
      [0, -1, 0],
      [-1, 5, -1],
      [0, -1, 0]
    ]
  },
  {
    name: 'Smooth',
    type: 'convolution',
    kernel: [
      [1/9, 1/9, 1/9],
      [1/9, 1/9, 1/9],
      [1/9, 1/9, 1/9]
    ]
  },
  {
    name: 'Edge Detect (Sobel)',
    type: 'convolution',
    kernel: [
      [-1, 0, 1],
      [-2, 0, 2],
      [-1, 0, 1]
    ]
  }
];
```

---

## 2. Advanced Reconstruction Modules (3D)

### 2.A Multi-Planar Reconstruction (MPR)

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Orthogonal MPR | Axial/Sagittal/Coronal views | High | 3 |
| Oblique MPR | Arbitrary plane angles | Medium | 3 |
| Curved MPR (CPR) | Curved plane along vessels | Medium | 4 |
| Slab thickness control | Variable thickness slabs | Medium | 3 |
| MinIP mode | Minimum intensity projection | Medium | 3 |
| MaxIP mode | Maximum intensity projection | Medium | 3 |
| Synchronized crosshair | Linked navigation across views | High | 3 |

#### MPR Technical Specifications

```typescript
interface MPRConfiguration {
  viewports: MPRViewport[];
  synchronization: {
    enabled: boolean;
    mode: 'crosshair' | 'scroll' | 'both';
  };
  slab: {
    thickness: number; // in mm
    mode: 'average' | 'mip' | 'minip';
  };
}

interface MPRViewport {
  id: string;
  plane: 'axial' | 'sagittal' | 'coronal' | 'oblique';
  oblique?: {
    rotationX: number;
    rotationY: number;
    rotationZ: number;
  };
  curved?: {
    path: Point3D[];
  };
}
```

---

### 2.B Volume Rendering (VR)

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| True 3D volume rendering | GPU-accelerated rendering | High | 4 |
| Opacity mapping | Transfer function editor | High | 4 |
| Color mapping | Tissue-specific coloring | High | 4 |
| Preset tissue views | Bone, soft tissue, vessels | Medium | 4 |
| Interactive rotation | Real-time 3D manipulation | High | 4 |
| Zoom | Scale volume view | High | 4 |
| Crop | Region of interest cropping | Medium | 4 |
| Real-time performance | 30+ fps rendering | High | 4 |

#### Volume Rendering Presets

```typescript
interface VolumePreset {
  name: string;
  description: string;
  transferFunction: TransferFunction;
  lighting: LightingConfig;
}

interface TransferFunction {
  colorPoints: ColorPoint[];
  opacityPoints: OpacityPoint[];
}

interface ColorPoint {
  value: number; // HU value
  color: [number, number, number]; // RGB
}

interface OpacityPoint {
  value: number; // HU value
  opacity: number; // 0-1
}

const VOLUME_PRESETS: VolumePreset[] = [
  {
    name: 'CT Bone',
    description: 'Bone visualization',
    transferFunction: {
      colorPoints: [
        { value: 200, color: [255, 255, 255] },
        { value: 1000, color: [255, 255, 200] }
      ],
      opacityPoints: [
        { value: 200, opacity: 0 },
        { value: 400, opacity: 0.5 },
        { value: 1000, opacity: 1.0 }
      ]
    },
    lighting: {
      ambient: 0.2,
      diffuse: 0.8,
      specular: 0.4
    }
  },
  {
    name: 'CT Soft Tissue',
    description: 'Soft tissue visualization',
    transferFunction: {
      colorPoints: [
        { value: -100, color: [100, 50, 50] },
        { value: 100, color: [200, 150, 150] }
      ],
      opacityPoints: [
        { value: -100, opacity: 0 },
        { value: 50, opacity: 0.3 },
        { value: 100, opacity: 0.6 }
      ]
    },
    lighting: {
      ambient: 0.3,
      diffuse: 0.7,
      specular: 0.2
    }
  },
  {
    name: 'CT Vessels (CTA)',
    description: 'Vascular visualization',
    transferFunction: {
      colorPoints: [
        { value: 100, color: [200, 50, 50] },
        { value: 400, color: [255, 100, 100] }
      ],
      opacityPoints: [
        { value: 100, opacity: 0 },
        { value: 200, opacity: 0.5 },
        { value: 400, opacity: 0.9 }
      ]
    },
    lighting: {
      ambient: 0.2,
      diffuse: 0.8,
      specular: 0.5
    }
  }
];
```

---

### 2.C Maximum / Minimum Intensity Projection

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| MIP rendering | Maximum intensity projection | High | 3 |
| MinIP rendering | Minimum intensity projection | Medium | 3 |
| Sliding slab | Variable slab position | Medium | 3 |
| Thick slab modes | Configurable thickness | Medium | 3 |
| Rotation | Rotate projection angle | Medium | 3 |

#### MIP/MinIP Configuration

```typescript
interface ProjectionConfig {
  mode: 'mip' | 'minip' | 'average';
  slabThickness: number; // in mm
  slabPosition: number; // percentage through volume
  rotationAngle: number; // degrees
  windowLevel: {
    width: number;
    center: number;
  };
}
```

---

## 3. Specialized Clinical 3D Modules

### 3.A Cardiac Imaging

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Cardiac MPR | Specialized cardiac planes | Medium | 5 |
| Coronary visualization | CTA visualization | Medium | 5 |
| Cardiac phase selection | Multi-phase navigation | Medium | 5 |
| Vessel analysis | Coronary analysis tools | Low | 5 |
| Calcium scoring | Agatston scoring (optional) | Low | 5 |

#### Cardiac Imaging Specifications

```typescript
interface CardiacModule {
  phases: CardiacPhase[];
  coronaryTrees: CoronaryTree[];
  calciumScore?: number;
}

interface CardiacPhase {
  phaseNumber: number;
  phasePercentage: number;
  seriesInstanceUID: string;
}

interface CoronaryTree {
  vessel: 'LAD' | 'LCX' | 'RCA' | 'LM';
  segments: VesselSegment[];
  stenosis?: StenosisMeasurement[];
}

interface VesselSegment {
  id: string;
  name: string;
  centerline: Point3D[];
  diameter: number[];
}
```

---

### 3.B Neuro Imaging

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Brain perfusion analysis | CBF, CBV, MTT maps | Low | 5 |
| 3D brain reconstruction | Surface/volume rendering | Medium | 4 |
| DWI visualization | Diffusion-weighted imaging | Medium | 4 |
| Tractography | Fiber tracking (advanced) | Low | 6 |

---

### 3.C Orthopedic & Trauma

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| 3D bone reconstruction | Bone surface rendering | Medium | 4 |
| Implant measurement | Pre-operative planning | Low | 5 |
| Fracture visualization | Fracture analysis | Medium | 4 |
| Limb length discrepancy | Length measurements | Low | 5 |

---

### 3.D Vascular Imaging

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Vessel segmentation | Automatic/semi-automatic | Medium | 4 |
| Centerline extraction | Vessel path tracing | Medium | 4 |
| Stenosis measurement | Diameter reduction calc | Medium | 4 |
| Aneurysm analysis | Size and volume measurement | Low | 5 |
| Automated vessel tracking | AI-assisted tracking | Low | 5 |

#### Vascular Analysis Specifications

```typescript
interface VascularAnalysis {
  vessels: Vessel[];
  stenoses: Stenosis[];
  aneurysms: Aneurysm[];
}

interface Vessel {
  id: string;
  name: string;
  type: 'artery' | 'vein';
  centerline: Point3D[];
  crossSections: CrossSection[];
}

interface CrossSection {
  position: Point3D;
  area: number;
  diameter: {
    min: number;
    max: number;
  };
  normal: [number, number, number];
}

interface Stenosis {
  vesselId: string;
  location: Point3D;
  percentReduction: number;
  referenceArea: number;
  minimalArea: number;
  length: number;
}

interface Aneurysm {
  id: string;
  vesselId: string;
  location: Point3D;
  maxDiameter: number;
  volume: number;
  neckDiameter: number;
}
```

---

## 4. Advanced Post-Processing & AI-Ready Features

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Automated organ segmentation | AI-based segmentation | Medium | 5 |
| Lesion detection | Automated finding | Low | 5 |
| Lesion quantification | Size/volume measurement | Medium | 5 |
| Tumor volume calculation | 3D volume estimation | Medium | 4 |
| Growth trend analysis | Longitudinal comparison | Low | 5 |
| AI plugin integration | Open API for AI models | High | 4 |
| DICOM SR support | Structured reporting | High | 3 |

### AI Integration Architecture

```typescript
interface AIPlugin {
  id: string;
  name: string;
  version: string;
  vendor: string;
  inputType: 'series' | 'study' | 'instance';
  outputType: 'segmentation' | 'detection' | 'measurement' | 'report';
  modalities: string[]; // CT, MR, etc.
  bodyParts: string[];
  endpoint: string;
  authentication?: AuthConfig;
}

interface AIResult {
  pluginId: string;
  studyInstanceUID: string;
  seriesInstanceUID?: string;
  timestamp: Date;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  findings: AIFinding[];
  segmentations?: Segmentation[];
  measurements?: Measurement[];
  structuredReport?: DicomSR;
}

interface AIFinding {
  id: string;
  type: string;
  location: Point3D;
  boundingBox?: BoundingBox;
  confidence: number;
  description: string;
  recommendations?: string[];
}

interface Segmentation {
  id: string;
  label: string;
  labelMap: number[][][]; // 3D array
  volume: number;
  color: [number, number, number];
}
```

---

## 5. Reporting & Workflow Integration

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| Structured reporting | Template-based reports | High | 3 |
| Measurement auto-population | Insert measurements into reports | High | 3 |
| Voice dictation | Speech-to-text integration | Low | 5 |
| RIS/HIS integration | HL7/FHIR connectivity | Medium | 4 |
| Study comparison | Prior vs current side-by-side | High | 3 |
| Key image attachment | Include images in reports | Medium | 3 |

### Reporting System Architecture

```typescript
interface Report {
  id: string;
  studyInstanceUID: string;
  templateId: string;
  status: 'draft' | 'preliminary' | 'final' | 'amended';
  sections: ReportSection[];
  measurements: ReportMeasurement[];
  keyImages: KeyImage[];
  createdAt: Date;
  createdBy: string;
  signedAt?: Date;
  signedBy?: string;
}

interface ReportSection {
  id: string;
  name: string;
  content: string;
  findings: Finding[];
}

interface ReportMeasurement {
  toolId: string;
  value: number;
  unit: string;
  label: string;
  autoPopulated: boolean;
}

interface KeyImage {
  sopInstanceUID: string;
  seriesInstanceUID: string;
  imageIndex: number;
  annotation?: string;
  windowLevel?: { width: number; center: number };
}

interface ReportTemplate {
  id: string;
  name: string;
  modality: string[];
  bodyPart: string[];
  sections: TemplateSection[];
  defaultFindings: string[];
}
```

---

## 6. DICOM & Interoperability Standards

| Feature | Description | Priority | Phase |
|---------|-------------|----------|-------|
| C-STORE | Store DICOM objects | High | 2 |
| C-FIND | Query DICOM objects | High | 2 |
| C-MOVE | Retrieve DICOM objects | High | 2 |
| C-GET | Alternative retrieval | Medium | 3 |
| WADO-RS | Web retrieve | High | 1 |
| QIDO-RS | Web query | High | 1 |
| STOW-RS | Web store | Medium | 3 |
| DICOM SR | Structured reports | High | 3 |
| Multi-vendor support | Broad compatibility | High | 2 |
| HTML5 web viewer | Browser-based viewing | High | 1 |
| DICOMweb support | Modern web standards | High | 1 |

### DICOM Conformance Statement

```yaml
Implementation Class UID: 1.2.840.10008.5.1.4.1.1.xxx
Implementation Version Name: JAVA-DICOM-VIEWER-1.0

SOP Classes Supported:
  Storage:
    - CT Image Storage: 1.2.840.10008.5.1.4.1.1.2
    - MR Image Storage: 1.2.840.10008.5.1.4.1.1.4
    - CR Image Storage: 1.2.840.10008.5.1.4.1.1.1
    - DX Image Storage: 1.2.840.10008.5.1.4.1.1.1.1
    - US Image Storage: 1.2.840.10008.5.1.4.1.1.6.1
    - XA Image Storage: 1.2.840.10008.5.1.4.1.1.12.1
    - Secondary Capture: 1.2.840.10008.5.1.4.1.1.7
    
  Query/Retrieve:
    - Patient Root Q/R - FIND: 1.2.840.10008.5.1.4.1.2.1.1
    - Patient Root Q/R - MOVE: 1.2.840.10008.5.1.4.1.2.1.2
    - Study Root Q/R - FIND: 1.2.840.10008.5.1.4.1.2.2.1
    - Study Root Q/R - MOVE: 1.2.840.10008.5.1.4.1.2.2.2
    
  Verification:
    - Verification SOP Class: 1.2.840.10008.1.1

Transfer Syntaxes Supported:
  - Implicit VR Little Endian: 1.2.840.10008.1.2
  - Explicit VR Little Endian: 1.2.840.10008.1.2.1
  - Explicit VR Big Endian: 1.2.840.10008.1.2.2
  - JPEG Baseline: 1.2.840.10008.1.2.4.50
  - JPEG Lossless: 1.2.840.10008.1.2.4.70
  - JPEG 2000 Lossless: 1.2.840.10008.1.2.4.90
  - JPEG 2000 Lossy: 1.2.840.10008.1.2.4.91
```

---

## 7. Optional & Premium 3D Add-Ons

These modules are considered premium features and may require additional licensing.

### 7.A Premium Clinical Modules

| Module | Description | Category | Phase |
|--------|-------------|----------|-------|
| Advanced Cardiac CT | Comprehensive cardiac analysis | Cardiac | 6 |
| Coronary CTA Analysis | Automated coronary evaluation | Cardiac | 6 |
| CT Colonography | Virtual colonoscopy | Oncology | 6 |
| Lung Nodule Analysis | AI-assisted lung screening | Oncology | 6 |
| Liver Analysis | Segment volumetry, lesion tracking | Oncology | 6 |
| Brain Perfusion CT | Stroke assessment tools | Neuro | 6 |
| CT Fractional Flow Reserve | FFR-CT analysis | Cardiac | 6 |
| Dental/CBCT Module | Dental imaging tools | Dental | 6 |
| Mammography Module | Breast imaging workstation | Breast | 6 |
| PET-CT Fusion | Nuclear medicine fusion | Nuclear | 6 |

### 7.B Add-On Module Specifications

```typescript
interface PremiumModule {
  id: string;
  name: string;
  description: string;
  category: ModuleCategory;
  version: string;
  dependencies: string[];
  license: LicenseInfo;
  features: ModuleFeature[];
}

type ModuleCategory = 
  | 'cardiac'
  | 'neuro'
  | 'oncology'
  | 'vascular'
  | 'orthopedic'
  | 'dental'
  | 'breast'
  | 'nuclear';

interface LicenseInfo {
  type: 'perpetual' | 'subscription' | 'per-study';
  expirationDate?: Date;
  maxStudies?: number;
  maxUsers?: number;
}

interface ModuleFeature {
  id: string;
  name: string;
  description: string;
  enabled: boolean;
}
```

### 7.C Module Integration Points

```typescript
interface ModuleIntegration {
  // Entry points for premium modules
  registerModule(module: PremiumModule): void;
  
  // Tool panel integration
  getModuleTools(moduleId: string): ViewerTool[];
  
  // Menu integration
  getModuleMenuItems(moduleId: string): MenuItem[];
  
  // Workflow integration
  getModuleWorkflows(moduleId: string): Workflow[];
  
  // AI integration
  getModuleAIPlugins(moduleId: string): AIPlugin[];
}
```

---

## Implementation Priority Matrix

| Priority | Phase | Modules |
|----------|-------|---------|
| Critical | 1-2 | Basic viewing, WL/WW, Zoom/Pan, Stack scroll, DICOMweb, C-FIND/C-MOVE |
| High | 2-3 | Measurements, Annotations, Key images, MPR (basic), DICOM SR |
| Medium | 3-4 | Volume rendering, MIP/MinIP, AI plugin API, RIS integration |
| Low | 4-5 | Clinical 3D modules, Voice dictation, Advanced AI |
| Optional | 5-6 | Premium add-ons, Specialized clinical modules |

---

## Performance Requirements

| Metric | Requirement |
|--------|-------------|
| Image load time | < 1 second for single image |
| Series load time | < 5 seconds for 500 images |
| WL/WW response | < 50ms |
| Scroll response | < 16ms (60 fps) |
| MPR response | < 100ms |
| Volume render | > 30 fps |
| Memory usage | < 4GB for typical workload |
| Concurrent series | 4+ in viewer |

---

## Browser Compatibility

| Browser | Version | Support Level |
|---------|---------|---------------|
| Chrome | 90+ | Full |
| Firefox | 88+ | Full |
| Safari | 14+ | Full |
| Edge | 90+ | Full |
| iOS Safari | 14+ | Partial (2D only) |
| Android Chrome | 90+ | Partial (2D only) |

---

## Security Requirements

| Requirement | Implementation |
|-------------|----------------|
| Data encryption | TLS 1.3 for all communications |
| Authentication | JWT tokens, OAuth 2.0 support |
| Authorization | Role-based access control (RBAC) |
| Audit logging | All PHI access logged |
| Session management | Configurable timeout, secure cookies |
| HIPAA compliance | Full compliance required |
| GDPR compliance | Patient consent management |

---

## Next Steps

1. Review and approve module specifications
2. Prioritize modules for each development phase
3. Create detailed technical designs for Phase 1-2 modules
4. Estimate development effort for each module
5. Plan integration testing strategy
6. Define acceptance criteria for each module
