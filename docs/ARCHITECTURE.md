# Architecture Overview

This document describes the technical architecture of the Java DICOM Viewer application.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Frontend (React)                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   Viewer     │  │   Worklist   │  │   Patient    │  │   Settings   │    │
│  │  Component   │  │  Component   │  │  Component   │  │  Component   │    │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘    │
│                          │                                                   │
│  ┌───────────────────────┴───────────────────────────────────────────┐     │
│  │                    Cornerstone.js Rendering Engine                 │     │
│  └───────────────────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Backend (Spring Boot 4.x)                            │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        REST API Layer                                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │DICOMweb  │  │ Study    │  │ Patient  │  │ Worklist │            │   │
│  │  │Controller│  │Controller│  │Controller│  │Controller│            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                      │                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       Service Layer                                  │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │DICOMweb  │  │ DICOM    │  │  Query   │  │ Storage  │            │   │
│  │  │ Service  │  │ Network  │  │ Service  │  │ Service  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                      │                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     dcm4che 5.x Integration                          │   │
│  │  ┌──────────────────────────────────────────────────────────────┐   │   │
│  │  │                Network Application Entity (AE)                │   │   │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         │   │   │
│  │  │  │ C-ECHO  │  │ C-FIND  │  │ C-MOVE  │  │ C-STORE │         │   │   │
│  │  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘         │   │   │
│  │  └──────────────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┴─────────────────┐
                    ▼                                   ▼
┌──────────────────────────────┐    ┌──────────────────────────────────────┐
│      Modern PACS             │    │         Legacy PACS                   │
│      (DICOMweb)              │    │    (Traditional DICOM)                │
│  ┌────────────────────────┐  │    │  ┌──────────────────────────────┐   │
│  │ WADO-RS │ QIDO-RS │    │  │    │  │ C-FIND │ C-MOVE │ C-STORE │   │   │
│  │ STOW-RS │ UPS-RS  │    │  │    │  └──────────────────────────────┘   │
│  └────────────────────────┘  │    └──────────────────────────────────────┘
└──────────────────────────────┘
```

## Component Details

### Frontend Architecture

#### Technology Stack
- **React 18+** with TypeScript for type-safe development
- **Cornerstone.js** for DICOM image rendering
- **React Query** for data fetching and caching
- **Zustand** for state management
- **Tailwind CSS** for styling

#### Key Components

1. **Viewer Component**
   - DICOM image display using Cornerstone.js
   - Window/Level adjustment
   - Zoom, pan, scroll tools
   - Measurement tools (distance, angle, ROI)
   - Annotation support

2. **Worklist Component**
   - Display study worklist
   - Filter and search capabilities
   - Study selection and navigation

3. **Patient Browser**
   - Patient demographics display
   - Study history navigation
   - Series/Instance hierarchy

4. **Settings Panel**
   - PACS connection configuration
   - User preferences
   - Display settings

### Backend Architecture

#### Technology Stack
- **Java 25** with modern language features
- **Spring Boot 4.x** for application framework
- **dcm4che 5.x** for DICOM operations
- **H2/PostgreSQL** for database
- **Spring Security** for authentication

#### Service Layers

1. **DICOMweb Services**
   - WADO-RS: Retrieve DICOM objects
   - QIDO-RS: Query DICOM objects
   - STOW-RS: Store DICOM objects

2. **Network Services (dcm4che)**
   - C-ECHO: Verify connectivity
   - C-FIND: Query studies/series/instances
   - C-MOVE: Retrieve studies from remote PACS
   - C-STORE: Receive/store DICOM objects

3. **Storage Service**
   - Local DICOM file storage
   - Metadata indexing
   - Cache management

4. **Query Service**
   - Unified query interface for both DICOMweb and traditional DICOM
   - Query result caching
   - Pagination support

## Data Flow

### DICOMweb Query Flow
```
1. Frontend sends REST request to backend
2. Backend checks if target PACS supports DICOMweb
3. If yes: Forward QIDO-RS query to PACS
4. Backend transforms response to internal format
5. Frontend receives and displays results
```

### Legacy PACS Query Flow
```
1. Frontend sends REST request to backend
2. Backend checks if target PACS is legacy
3. If yes: Create DICOM C-FIND association
4. Execute C-FIND query via dcm4che
5. Parse DICOM response to internal format
6. Frontend receives and displays results
```

### Image Retrieval Flow
```
1. User selects study/series in frontend
2. Frontend requests images via REST API
3. Backend determines PACS type:
   a. DICOMweb PACS: Use WADO-RS to retrieve
   b. Legacy PACS: Use C-MOVE to retrieve
4. Images stored locally (if not already cached)
5. Backend serves images to frontend
6. Cornerstone.js renders images
```

## Database Schema

### Core Entities

```sql
-- PACS Configuration
CREATE TABLE pacs_configuration (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    ae_title VARCHAR(16) NOT NULL,
    pacs_type VARCHAR(20) NOT NULL, -- 'DICOMWEB' or 'LEGACY'
    wado_rs_url VARCHAR(500),
    qido_rs_url VARCHAR(500),
    stow_rs_url VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Local Study Cache
CREATE TABLE study_cache (
    study_instance_uid VARCHAR(64) PRIMARY KEY,
    patient_id VARCHAR(64),
    patient_name VARCHAR(255),
    study_date DATE,
    study_description VARCHAR(255),
    modality VARCHAR(16),
    accession_number VARCHAR(16),
    source_pacs_id UUID,
    cached_at TIMESTAMP,
    FOREIGN KEY (source_pacs_id) REFERENCES pacs_configuration(id)
);

-- Audit Log
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    user_id VARCHAR(64),
    action VARCHAR(50),
    resource_type VARCHAR(50),
    resource_id VARCHAR(64),
    details JSONB,
    timestamp TIMESTAMP,
    ip_address VARCHAR(45)
);
```

## Security Considerations

### Authentication
- OAuth 2.0 / OpenID Connect support
- LDAP/Active Directory integration
- JWT token-based session management

### Authorization
- Role-based access control (RBAC)
- Study-level access restrictions
- Audit logging for all access

### Data Protection
- TLS 1.3 for all communications
- DICOM TLS for network operations
- Encryption at rest for stored images

## Scalability

### Horizontal Scaling
- Stateless backend design
- Redis for session caching
- Load balancer support

### Performance Optimization
- Image caching at multiple levels
- Lazy loading for large studies
- WebSocket for real-time updates

## Integration Points

### External Systems
- HL7/FHIR integration for clinical data
- Reporting system integration
- RIS/HIS connectivity

---

## Viewer Architecture

### Frontend Viewer Components

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DICOM Viewer Application                               │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                         Viewer Container                                 │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │   │
│  │  │   Toolbar   │  │  Viewport   │  │   Series    │  │   Study     │    │   │
│  │  │  Component  │  │  Component  │  │   Panel     │  │   Info      │    │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                        Tool Management Layer                             │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │   │
│  │  │ WL/WW    │  │ Zoom/Pan │  │ Scroll   │  │ Measure  │  │ Annotate │ │   │
│  │  │ Tool     │  │ Tool     │  │ Tool     │  │ Tool     │  │ Tool     │ │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                     Cornerstone.js / Cornerstone3D                       │   │
│  │  ┌───────────────────────────────────────────────────────────────────┐  │   │
│  │  │                    Rendering Engine                                │  │   │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐             │  │   │
│  │  │  │ 2D View │  │ MPR     │  │ Volume  │  │ MIP     │             │  │   │
│  │  │  │ Render  │  │ Render  │  │ Render  │  │ Render  │             │  │   │
│  │  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘             │  │   │
│  │  └───────────────────────────────────────────────────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐   │
│  │                        Image Loader Layer                                │   │
│  │  ┌──────────────────────┐  ┌──────────────────────┐                    │   │
│  │  │   WADO Image Loader  │  │   File Image Loader  │                    │   │
│  │  └──────────────────────┘  └──────────────────────┘                    │   │
│  └─────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Tool Architecture

```typescript
// Tool Interface Definition
interface ViewerTool {
  id: string;
  name: string;
  icon: string;
  category: ToolCategory;
  mode: 'active' | 'passive' | 'enabled' | 'disabled';
  
  // Lifecycle methods
  activate(): void;
  deactivate(): void;
  
  // Event handlers
  onMouseDown?(event: ViewerMouseEvent): void;
  onMouseMove?(event: ViewerMouseEvent): void;
  onMouseUp?(event: ViewerMouseEvent): void;
  onMouseWheel?(event: ViewerWheelEvent): void;
  onTouchStart?(event: ViewerTouchEvent): void;
  onKeyDown?(event: ViewerKeyEvent): void;
  
  // Rendering
  render?(context: CanvasRenderingContext2D): void;
}

type ToolCategory = 
  | 'manipulation'   // WL, Zoom, Pan, Rotate
  | 'measurement'    // Length, Angle, ROI
  | 'annotation'     // Text, Arrow, Shape
  | 'navigation'     // Scroll, Cine
  | 'segmentation'   // ROI, Brush
  | '3d';            // MPR, Volume
```

### Measurement Data Model

```typescript
interface Measurement {
  id: string;
  type: MeasurementType;
  studyInstanceUID: string;
  seriesInstanceUID: string;
  sopInstanceUID: string;
  frameNumber?: number;
  
  // Geometry
  points: Point2D[];
  handles: MeasurementHandle[];
  
  // Result
  value: number;
  unit: string;
  text?: string;
  
  // Metadata
  createdAt: Date;
  createdBy: string;
  modifiedAt?: Date;
  isVisible: boolean;
  color: string;
}

type MeasurementType = 
  | 'length'
  | 'angle'
  | 'cobbs_angle'
  | 'rectangle_roi'
  | 'ellipse_roi'
  | 'polygon_roi'
  | 'freehand_roi'
  | 'pixel_probe';

interface ROIMeasurement extends Measurement {
  stats: {
    mean: number;
    stdDev: number;
    min: number;
    max: number;
    area: number;
    perimeter: number;
    pixelCount: number;
  };
}
```

### Viewport State Management

```typescript
interface ViewportState {
  id: string;
  
  // Image Reference
  studyInstanceUID: string;
  seriesInstanceUID: string;
  imageIdIndex: number;
  
  // Display Settings
  windowCenter: number;
  windowWidth: number;
  invert: boolean;
  
  // Transform
  scale: number;
  translation: { x: number; y: number };
  rotation: number;
  flipHorizontal: boolean;
  flipVertical: boolean;
  
  // Interpolation
  interpolationType: 'nearest' | 'linear';
  
  // Overlay
  showPatientInfo: boolean;
  showAnnotations: boolean;
  showMeasurements: boolean;
}

interface MPRViewportState extends ViewportState {
  plane: 'axial' | 'sagittal' | 'coronal' | 'oblique';
  slabThickness: number;
  slabMode: 'average' | 'mip' | 'minip';
  crosshairPosition: Point3D;
}

interface VolumeViewportState extends ViewportState {
  camera: {
    position: Point3D;
    focalPoint: Point3D;
    viewUp: [number, number, number];
    viewAngle: number;
    parallelScale: number;
  };
  transferFunction: TransferFunction;
  clippingPlanes: ClippingPlane[];
}
```

### 3D Rendering Pipeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        3D Rendering Pipeline                             │
│                                                                          │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │   Volume     │    │   Transfer   │    │   Shader    │              │
│  │   Data       │───►│   Function   │───►│   Program   │              │
│  │   (3D)       │    │   Mapping    │    │   (WebGL)   │              │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│         │                   │                   │                       │
│         ▼                   ▼                   ▼                       │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐              │
│  │   Texture    │    │   Ray        │    │   Frame     │              │
│  │   Upload     │───►│   Marching   │───►│   Buffer    │              │
│  │   (GPU)      │    │   Algorithm  │    │   Output    │              │
│  └──────────────┘    └──────────────┘    └──────────────┘              │
│                                                 │                       │
│                                                 ▼                       │
│                                          ┌──────────────┐              │
│                                          │   Display    │              │
│                                          │   (Canvas)   │              │
│                                          └──────────────┘              │
└─────────────────────────────────────────────────────────────────────────┘
```

### MPR Reconstruction Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      MPR Reconstruction System                           │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │                      Volume Data Store                            │  │
│  │  ┌────────────────────────────────────────────────────────────┐  │  │
│  │  │  3D Voxel Array (Float32/Int16)                            │  │  │
│  │  │  Dimensions: Width x Height x Depth                         │  │  │
│  │  │  Spacing: [sx, sy, sz] mm                                   │  │  │
│  │  │  Origin: [ox, oy, oz] mm                                    │  │  │
│  │  └────────────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                   │                                     │
│                    ┌──────────────┼──────────────┐                     │
│                    ▼              ▼              ▼                      │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐       │
│  │   Axial Plane    │ │  Sagittal Plane  │ │  Coronal Plane   │       │
│  │   Extraction     │ │  Extraction      │ │  Extraction      │       │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘       │
│           │                    │                    │                  │
│           ▼                    ▼                    ▼                  │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐       │
│  │   Interpolation  │ │  Interpolation   │ │  Interpolation   │       │
│  │   (Trilinear)    │ │  (Trilinear)     │ │  (Trilinear)     │       │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘       │
│           │                    │                    │                  │
│           ▼                    ▼                    ▼                  │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐       │
│  │   2D Image       │ │   2D Image       │ │   2D Image       │       │
│  │   Rendering      │ │   Rendering      │ │   Rendering      │       │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘       │
└─────────────────────────────────────────────────────────────────────────┘
```

### Clinical Module Integration

```typescript
// Clinical Module Interface
interface ClinicalModule {
  id: string;
  name: string;
  description: string;
  category: ModuleCategory;
  
  // Module capabilities
  supportedModalities: string[];
  supportedBodyParts: string[];
  
  // UI Integration
  getToolbarItems(): ToolbarItem[];
  getPanelComponents(): React.ComponentType[];
  getMenuItems(): MenuItem[];
  
  // Data Processing
  processStudy?(study: DicomStudy): Promise<ModuleResult>;
  
  // Visualization
  getOverlays?(): Overlay[];
  getAnnotations?(): Annotation[];
}

type ModuleCategory = 
  | 'cardiac'
  | 'neuro'
  | 'vascular'
  | 'orthopedic'
  | 'oncology'
  | 'general';

// Cardiac Module Example
interface CardiacModule extends ClinicalModule {
  category: 'cardiac';
  
  // Cardiac-specific methods
  detectCardiacPhases(): CardiacPhase[];
  extractCoronaryArteries(): CoronaryTree;
  calculateEjectionFraction(): number;
  measureChamberVolumes(): ChamberVolumes;
}

// Vascular Module Example  
interface VascularModule extends ClinicalModule {
  category: 'vascular';
  
  // Vascular-specific methods
  segmentVessels(seedPoints: Point3D[]): VesselTree;
  extractCenterline(vessel: VesselSegment): Point3D[];
  measureStenosis(location: Point3D): StenosisMeasurement;
  analyzeAneurysm(region: BoundingBox): AneurysmAnalysis;
}
```

### AI Integration Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        AI Integration Framework                          │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      AI Plugin Manager                           │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │   │
│  │  │ Plugin   │  │ Plugin   │  │ Plugin   │  │ Plugin   │        │   │
│  │  │ Registry │  │ Loader   │  │ Executor │  │ Monitor  │        │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                   │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      AI Plugin Interface                         │   │
│  │                                                                  │   │
│  │  // Plugin Definition                                            │   │
│  │  interface AIPlugin {                                            │   │
│  │    id: string;                                                   │   │
│  │    name: string;                                                 │   │
│  │    version: string;                                              │   │
│  │    inputSchema: InputSchema;                                     │   │
│  │    outputSchema: OutputSchema;                                   │   │
│  │                                                                  │   │
│  │    // Execution                                                  │   │
│  │    analyze(input: AIInput): Promise<AIOutput>;                   │   │
│  │                                                                  │   │
│  │    // Progress                                                   │   │
│  │    onProgress?: (progress: number) => void;                      │   │
│  │  }                                                               │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                   │                                     │
│                    ┌──────────────┼──────────────┐                     │
│                    ▼              ▼              ▼                      │
│  ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐       │
│  │   Segmentation   │ │   Detection      │ │   Measurement    │       │
│  │   Plugins        │ │   Plugins        │ │   Plugins        │       │
│  │                  │ │                  │ │                  │       │
│  │  - Organ Seg     │ │  - Nodule Det    │ │  - Auto Measure  │       │
│  │  - Lesion Seg    │ │  - Finding Det   │ │  - Landmark Det  │       │
│  │  - Vessel Seg    │ │  - Anomaly Det   │ │  - Growth Track  │       │
│  └──────────────────┘ └──────────────────┘ └──────────────────┘       │
│                                   │                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                      Result Integration                          │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │   │
│  │  │  Overlay     │  │  Annotation  │  │  Report      │          │   │
│  │  │  Renderer    │  │  Manager     │  │  Generator   │          │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Performance Optimization Strategies

| Area | Strategy | Implementation |
|------|----------|----------------|
| Image Loading | Progressive loading | Load low-res first, then full resolution |
| Image Caching | Multi-level cache | Browser cache → IndexedDB → Server cache |
| Rendering | WebGL acceleration | GPU-based image processing |
| Memory | Streaming volumes | Load only visible slices |
| Network | Prefetching | Anticipate user navigation |
| 3D Rendering | LOD (Level of Detail) | Reduce resolution during interaction |
| MPR | Background generation | Pre-compute reformats |
| Tools | Lazy initialization | Load tools on demand |
