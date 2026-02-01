# Implementation Roadmap

This document outlines the phased implementation plan for the Java DICOM Viewer application. The plan is designed to deliver incremental value while building towards a comprehensive medical imaging platform.

> **Note**: For detailed module specifications, see [MODULE_SPECIFICATIONS.md](./MODULE_SPECIFICATIONS.md)

> **Phase 1 Details**: For comprehensive Phase 1 implementation guide with code examples, acceptance criteria, and testing strategies, see [PHASE1_IMPLEMENTATION.md](./PHASE1_IMPLEMENTATION.md)

> **Phase 2 Details**: For comprehensive Phase 2 implementation guide with code examples, acceptance criteria, and testing strategies, see [PHASE2_IMPLEMENTATION.md](./PHASE2_IMPLEMENTATION.md)

## Overview

| Phase | Name | Duration | Key Deliverables |
|-------|------|----------|------------------|
| 1 | Foundation | 4-6 weeks | Basic viewer, DICOMweb support |
| 2 | Legacy PACS Support | 3-4 weeks | C-FIND, C-MOVE, C-STORE |
| 3 | Core Viewer Features | 4-6 weeks | Measurements, annotations, MPR basics |
| 4 | Advanced 2D & Workflow | 4-6 weeks | Image processing, reporting, integration |
| 5 | 3D Reconstruction | 6-8 weeks | Volume rendering, MIP/MinIP, advanced MPR |
| 6 | Clinical Modules | 8-10 weeks | Cardiac, neuro, vascular, orthopedic |
| 7 | AI & Premium Features | 6-8 weeks | AI integration, premium add-ons |

---

## Phase 1: Foundation (Weeks 1-6)

### Goals
- Establish project infrastructure
- Implement basic DICOM viewing capabilities
- Add DICOMweb support for modern PACS

### Week 1-2: Project Setup

#### Backend Setup
- [x] Initialize Spring Boot 4.x project with Gradle
- [x] Configure Java 25 with appropriate compiler settings
- [x] Add dcm4che 5.x dependencies
- [x] Set up project structure (controllers, services, repositories)
- [x] Configure application properties
- [x] Set up logging (SLF4J/Logback)
- [x] Add basic health check endpoint

#### Frontend Setup
- [x] Initialize React project with TypeScript
- [x] Install and configure Cornerstone.js
- [x] Set up Tailwind CSS for styling
- [x] Configure build tools (Vite)
- [x] Create basic component structure
- [x] Set up routing (React Router)

#### Development Infrastructure
- [x] Docker Compose for local development
- [x] Set up test PACS (dcm4che-arc or Orthanc)
- [ ] Configure CI/CD pipeline basics

### Week 3-4: Basic DICOM Viewer

#### Image Rendering
- [x] Integrate Cornerstone.js rendering engine
- [x] Implement image loading from local source
- [x] Add window/level adjustment tool
- [x] Implement zoom and pan tools
- [x] Add image scroll for series navigation
- [x] Implement basic toolbar UI

#### Study Browser
- [x] Create study list component
- [x] Implement series thumbnail grid
- [x] Add study metadata display
- [x] Implement study selection

### Week 5-6: DICOMweb Integration

#### WADO-RS Implementation
- [x] Create WADO-RS client service
- [x] Implement instance retrieval
- [x] Add series retrieval support
- [x] Implement thumbnail generation
- [ ] Handle multipart/related responses

#### QIDO-RS Implementation
- [x] Create QIDO-RS client service
- [x] Implement study-level queries
- [x] Add series-level queries
- [x] Implement instance-level queries
- [ ] Add query pagination support

#### API Endpoints
- [x] GET /api/studies - List/search studies
- [x] GET /api/studies/{studyUID}/series - List series
- [x] GET /api/studies/{studyUID}/series/{seriesUID}/instances - List instances
- [x] GET /api/wado - WADO-RS proxy endpoint

### Phase 1 Deliverables
✅ Functional DICOM viewer  
✅ DICOMweb connectivity  
✅ Basic study browser  
✅ Image manipulation tools  

---

## Phase 2: Legacy PACS Support (Weeks 7-10)

### Goals
- Implement traditional DICOM network operations
- Enable connectivity to older PACS systems
- Provide unified query interface

### Week 7-8: DICOM Network Foundation

#### Application Entity Configuration
- [x] Create AE configuration model
- [x] Implement AE management service
- [x] Add PACS configuration UI
- [x] Store AE settings in database

#### C-ECHO Implementation
- [x] Implement C-ECHO SCU using dcm4che
- [x] Create connectivity test endpoint
- [x] Add connection status UI
- [ ] Implement connection pooling

### Week 8-9: Query Operations (C-FIND)

#### C-FIND SCU Implementation
```java
// Key classes to implement:
- DicomQueryService
- CfindScu
- QueryAttributeBuilder
- QueryResultParser
```

- [x] Implement Patient Root C-FIND
- [x] Implement Study Root C-FIND
- [x] Add query at study level
- [x] Add query at series level
- [x] Add query at instance level
- [ ] Handle query result pagination
- [ ] Implement query caching

#### Unified Query Service
- [x] Abstract query interface for both protocols
- [x] PACS type detection and routing
- [x] Query result normalization
- [x] Error handling and fallbacks

### Week 9-10: Retrieval Operations (C-MOVE/C-STORE)

#### C-MOVE SCU Implementation
- [x] Implement C-MOVE request handling
- [x] Configure local AE for receiving
- [x] Handle C-MOVE sub-operations
- [x] Implement retrieval progress tracking

#### C-STORE SCP Implementation
- [x] Create C-STORE SCP service
- [x] Implement instance reception handler
- [x] Add local storage management
- [x] Implement metadata extraction
- [x] Store received instances in cache

#### Local Storage
- [x] Design storage directory structure
- [x] Implement DICOM file writing
- [x] Create metadata database entries
- [x] Add storage cleanup/management

### Phase 2 Deliverables
✅ C-ECHO for connectivity testing  
✅ C-FIND for querying legacy PACS  
✅ C-MOVE/C-STORE for image retrieval  
✅ Unified query interface  

---

## Phase 3: Core Viewer Features (Weeks 11-16)

### Goals
- Implement essential measurement and annotation tools
- Add basic MPR capabilities
- Enable key image marking and study comparison

### Week 11-12: Measurement Tools

#### Linear Measurements
- [ ] Distance measurement tool (two points)
- [ ] Measurement value display with units
- [ ] Measurement persistence to backend
- [ ] Measurement editing and deletion

#### Angle Measurements
- [ ] Cobb's angle tool (three points)
- [ ] Standard angle measurement
- [ ] Angle overlay display

#### ROI Measurements
- [ ] Rectangle ROI tool
- [ ] Ellipse ROI tool
- [ ] Polygon ROI tool (multi-point)
- [ ] Freehand ROI tool
- [ ] Statistics display (mean, std dev, min, max, area)
- [ ] Hounsfield unit display for CT

### Week 12-13: Annotation Tools

#### Text Annotations
- [ ] Text label tool
- [ ] Font size and color options
- [ ] Text positioning and editing

#### Graphical Annotations
- [ ] Arrow annotation tool
- [ ] Line annotation tool
- [ ] Marker/point annotation
- [ ] Shape annotations (rectangle, ellipse)

#### Annotation Management
- [ ] Save annotations to backend
- [ ] Load annotations for study
- [ ] Annotation visibility toggle
- [ ] Annotation layer management

### Week 13-14: Key Image & Comparison

#### Key Image Marking
- [ ] Mark image as key image
- [ ] Key image indicator display
- [ ] Key image gallery view
- [ ] Key image export

#### Study Comparison
- [ ] Prior study loading
- [ ] Side-by-side comparison view
- [ ] Synchronized scrolling option
- [ ] Measurement comparison
- [ ] Lesion tracking across studies

### Week 14-16: Basic MPR

#### Orthogonal MPR
- [ ] Generate axial, sagittal, coronal views
- [ ] Real-time reformatting
- [ ] Synchronized crosshair navigation
- [ ] Linked window/level across views

#### MPR Layout
- [ ] 2x2 MPR layout
- [ ] 1x3 MPR layout
- [ ] Customizable layout options
- [ ] Viewport swap/maximize

#### MPR Tools
- [ ] Slab thickness slider (1-20mm)
- [ ] Average intensity projection mode
- [ ] Reference line display
- [ ] Scroll position synchronization

### Phase 3 Deliverables
✅ Complete measurement toolkit
✅ Annotation tools with persistence
✅ Key image marking and gallery
✅ Study comparison view
✅ Basic orthogonal MPR

---

## Phase 4: Advanced 2D Features & Workflow (Weeks 17-22)

### Goals
- Add advanced image processing features
- Implement cine and dynamic series playback
- Integrate reporting and clinical workflow

### Week 17-18: Image Processing & Enhancement

#### Image Processing Filters
- [ ] Sharpen filter implementation
- [ ] Smooth/blur filter
- [ ] Edge enhancement (Sobel, Laplacian)
- [ ] Noise reduction filters
- [ ] Custom convolution kernel support

#### Display Enhancement
- [ ] Magnification with interpolation options
- [ ] Image inversion toggle
- [ ] Grayscale LUT presets
- [ ] Custom LUT creation
- [ ] Rotate (90°, free angle)
- [ ] Flip (horizontal, vertical)

#### Cine & Playback
- [ ] Cine loop playback controls
- [ ] Adjustable frame rate (1-60 fps)
- [ ] Loop mode options (forward, bounce)
- [ ] Frame-by-frame navigation
- [ ] Multi-phase cardiac playback

### Week 18-19: User Management & Security

#### Authentication
- [ ] Spring Security configuration
- [ ] JWT token generation/validation
- [ ] Login/logout endpoints
- [ ] Login UI implementation
- [ ] Session management

#### Authorization
- [ ] Role hierarchy (Admin, Radiologist, Technologist)
- [ ] Role-based access control
- [ ] Endpoint security
- [ ] User management UI

#### Audit & Compliance
- [ ] Audit log schema design
- [ ] Study access logging
- [ ] User action logging
- [ ] Audit log viewer

### Week 19-20: Worklist & Reporting

#### Modality Worklist (MWL)
- [ ] MWL C-FIND SCU implementation
- [ ] Worklist data model
- [ ] Worklist query service
- [ ] Worklist UI component

#### Study Worklist Features
- [ ] Status tracking (New, In Progress, Complete)
- [ ] Priority indicators
- [ ] Search and filter functionality
- [ ] Date range and modality filtering

#### Basic Reporting
- [ ] Report entity model
- [ ] Report status workflow
- [ ] Report editor component
- [ ] Template support
- [ ] PDF export

### Week 20-21: Integration & Interoperability

#### HL7/FHIR Integration
- [ ] HL7 v2.x message parsing
- [ ] FHIR ImagingStudy resource
- [ ] Patient demographics sync
- [ ] Order information integration

#### STOW-RS Implementation
- [ ] STOW-RS endpoint
- [ ] Multipart upload handling
- [ ] Upload progress UI

#### RIS/HIS Integration
- [ ] Order receipt integration
- [ ] Report delivery (HL7 ORU)
- [ ] Image routing configuration

### Week 21-22: Performance & Production

#### Performance Optimization
- [ ] Connection pooling tuning
- [ ] Query result caching
- [ ] Image prefetching
- [ ] WebGL rendering optimization
- [ ] Virtual scrolling for large lists

#### Production Readiness
- [ ] Security audit and hardening
- [ ] HTTPS configuration
- [ ] Kubernetes manifests
- [ ] Helm charts
- [ ] API documentation (OpenAPI)
- [ ] User and admin guides

### Phase 4 Deliverables
✅ Image processing filters
✅ Cine loop playback
✅ User authentication and authorization
✅ Modality worklist integration
✅ Basic reporting functionality
✅ HL7/FHIR integration
✅ Performance optimizations
✅ Production-ready deployment  

---

## Phase 5: 3D Reconstruction (Weeks 23-30)

### Goals
- Implement advanced 3D visualization capabilities
- Add volume rendering for CT/MR datasets
- Enable multi-planar reconstruction with advanced features

### Week 23-25: Advanced MPR

#### Orthogonal MPR Enhancement
- [ ] Implement high-quality interpolation
- [ ] Add slab thickness controls (1-50mm)
- [ ] Implement MinIP/MaxIP slab modes
- [ ] Add average intensity projection
- [ ] Create synchronized crosshair navigation
- [ ] Implement linked window/level across viewports

#### Oblique MPR
- [ ] Add arbitrary plane rotation
- [ ] Implement angle input controls
- [ ] Add preset oblique planes
- [ ] Create plane manipulation handles
- [ ] Implement real-time oblique updates

#### Curved MPR (CPR)
- [ ] Create vessel centerline editor
- [ ] Implement curved plane generation
- [ ] Add straightened vessel view
- [ ] Implement cross-sectional views along curve
- [ ] Add measurement along curved path

### Week 25-27: Volume Rendering

#### Core Volume Rendering
- [ ] Integrate VTK.js or similar WebGL library
- [ ] Implement ray-casting renderer
- [ ] Add GPU-accelerated rendering
- [ ] Implement transfer function editor
- [ ] Create opacity/color mapping tools
- [ ] Add lighting controls

#### Volume Presets
- [ ] CT Bone preset
- [ ] CT Soft Tissue preset
- [ ] CT Vessels (CTA) preset
- [ ] CT Lung preset
- [ ] MR Brain preset
- [ ] Custom preset creation/saving

#### Volume Manipulation
- [ ] Interactive 3D rotation
- [ ] Zoom and perspective controls
- [ ] Clipping planes (6 directions)
- [ ] Region of interest cropping
- [ ] Volume measurement tools

### Week 27-30: Projection Techniques

#### MIP (Maximum Intensity Projection)
- [ ] Implement MIP rendering
- [ ] Add variable slab thickness
- [ ] Create sliding slab controls
- [ ] Implement rotation for MIP
- [ ] Add cine MIP rotation

#### MinIP (Minimum Intensity Projection)
- [ ] Implement MinIP rendering
- [ ] Configure for airway/lung visualization
- [ ] Add slab thickness controls
- [ ] Implement window/level for MinIP

#### Average Intensity Projection
- [ ] Implement AIP rendering
- [ ] Add configurable averaging parameters
- [ ] Create comparison view modes

### Phase 5 Deliverables
✅ Advanced MPR with oblique and curved reconstruction
✅ Real-time volume rendering with presets
✅ MIP/MinIP/AIP projection techniques
✅ 3D measurement tools
✅ Interactive 3D navigation

---

## Phase 6: Specialized Clinical 3D Modules (Weeks 31-40)

### Goals
- Implement specialized clinical visualization tools
- Add anatomy-specific analysis features
- Enable advanced clinical workflows

### Week 31-33: Vascular Imaging

#### Vessel Segmentation
- [ ] Implement semi-automatic vessel segmentation
- [ ] Add seed point-based region growing
- [ ] Create vessel tree visualization
- [ ] Implement vessel labeling

#### Vessel Analysis
- [ ] Centerline extraction algorithm
- [ ] Cross-sectional area measurement
- [ ] Diameter measurement tools
- [ ] Stenosis calculation (% reduction)
- [ ] Length measurement along vessels

#### Aneurysm Analysis
- [ ] Aneurysm detection assistance
- [ ] Maximum diameter measurement
- [ ] Volume calculation
- [ ] Neck diameter measurement
- [ ] Growth tracking across studies

### Week 33-35: Cardiac Imaging

#### Cardiac MPR
- [ ] Short-axis view generation
- [ ] Long-axis views (2CH, 3CH, 4CH)
- [ ] Automated cardiac plane detection
- [ ] Phase selection for multi-phase CT

#### Coronary Visualization
- [ ] Coronary artery tree extraction
- [ ] Curved MPR along coronary arteries
- [ ] Cross-sectional views
- [ ] Stenosis measurement

#### Cardiac Measurements
- [ ] Ejection fraction estimation
- [ ] Chamber volume measurements
- [ ] Wall thickness measurement
- [ ] Optional: Calcium scoring

### Week 35-37: Neuro Imaging

#### Brain Visualization
- [ ] 3D brain surface rendering
- [ ] Brain tissue segmentation (gray/white matter)
- [ ] Ventricle visualization
- [ ] Skull stripping tools

#### Brain Analysis
- [ ] DWI/ADC visualization
- [ ] Perfusion map display (CBF, CBV, MTT)
- [ ] Lesion volume measurement
- [ ] Midline shift measurement
- [ ] Optional: Tractography visualization

### Week 37-40: Orthopedic & Trauma

#### Bone Visualization
- [ ] 3D bone surface rendering
- [ ] Bone segmentation tools
- [ ] Fracture visualization
- [ ] Implant visualization

#### Orthopedic Measurements
- [ ] Bone length measurement
- [ ] Angle measurements
- [ ] Limb length discrepancy
- [ ] Joint angle analysis
- [ ] Implant planning tools

### Phase 6 Deliverables
✅ Vascular analysis with stenosis/aneurysm measurement
✅ Cardiac imaging with coronary visualization
✅ Neuro imaging with perfusion/diffusion support
✅ Orthopedic tools for bone and fracture analysis
✅ Clinical-specific measurement tools

---

## Phase 7: AI Integration & Premium Features (Weeks 41-48)

### Goals
- Create extensible AI integration framework
- Implement premium clinical modules
- Enable advanced automation and analysis

### Week 41-43: AI Plugin Framework

#### AI Integration API
- [ ] Design plugin interface specification
- [ ] Create plugin registration system
- [ ] Implement asynchronous processing queue
- [ ] Add progress tracking and notification
- [ ] Create result visualization framework

#### AI Result Handling
- [ ] Segmentation overlay display
- [ ] Detection bounding box display
- [ ] Finding annotation integration
- [ ] Confidence score display
- [ ] AI-to-report integration

#### Built-in AI Features
- [ ] Automated organ segmentation
- [ ] Lesion detection assistance
- [ ] Measurement auto-detection
- [ ] Smart hanging protocols

### Week 43-45: Advanced Reporting

#### Structured Reporting (DICOM SR)
- [ ] DICOM SR creation/parsing
- [ ] Measurement auto-population
- [ ] Template-based report generation
- [ ] SR to PDF export

#### Clinical Reporting
- [ ] Voice dictation integration API
- [ ] Speech-to-text support
- [ ] Report comparison tools
- [ ] Critical finding alerts

#### Report Workflow
- [ ] Draft/preliminary/final status
- [ ] Addendum support
- [ ] Digital signature
- [ ] Report distribution

### Week 45-48: Premium Add-On Modules

#### Premium Module Framework
- [ ] Module licensing system
- [ ] Feature flag management
- [ ] Module marketplace structure
- [ ] Version management

#### Initial Premium Modules
- [ ] Advanced Cardiac CT Analysis
- [ ] Lung Nodule Analysis
- [ ] Liver Analysis Module
- [ ] Dental/CBCT Module

### Phase 7 Deliverables
✅ Extensible AI plugin framework
✅ Built-in AI-assisted features
✅ DICOM Structured Reporting
✅ Voice dictation integration
✅ Premium module infrastructure
✅ Initial premium clinical modules

---

## Module Feature Matrix

| Module | Phase | Priority | Complexity |
|--------|-------|----------|------------|
| Basic Image Viewing | 1 | Critical | Low |
| Window/Level | 1 | Critical | Low |
| Zoom/Pan/Scroll | 1 | Critical | Low |
| DICOMweb Support | 1 | Critical | Medium |
| Legacy PACS (C-FIND/C-MOVE) | 2 | Critical | High |
| Linear Measurements | 3 | High | Medium |
| Angle Measurements | 3 | High | Medium |
| ROI Measurements | 3 | High | Medium |
| Text Annotations | 3 | High | Low |
| Key Image Marking | 3 | High | Low |
| Basic MPR | 3 | High | High |
| Image Processing Filters | 4 | Medium | Medium |
| Cine Loop | 4 | Medium | Medium |
| HL7/FHIR Integration | 4 | Medium | High |
| Volume Rendering | 5 | High | Very High |
| MIP/MinIP | 5 | High | High |
| Oblique/Curved MPR | 5 | Medium | Very High |
| Vessel Analysis | 6 | Medium | Very High |
| Cardiac Imaging | 6 | Low | Very High |
| Neuro Imaging | 6 | Low | Very High |
| AI Plugin Framework | 7 | High | High |
| Premium Modules | 7 | Low | Variable |

---

## Success Metrics

### Phase 1
- [ ] Can load and display DICOM images
- [ ] Can query DICOMweb PACS
- [ ] Basic viewing tools functional

### Phase 2
- [x] Can connect to legacy PACS
- [x] C-FIND returns correct results
- [x] C-MOVE retrieves images successfully

### Phase 3
- [ ] All measurement tools accurate to 1mm
- [ ] Annotations persist across sessions
- [ ] Basic MPR functional with synchronized views

### Phase 4
- [ ] Image processing filters working
- [ ] Basic reports can be created
- [ ] HL7 messages processed correctly

### Phase 5
- [ ] Volume rendering at 30+ fps
- [ ] MIP/MinIP renders correctly
- [ ] Curved MPR along vessels functional

### Phase 6
- [ ] Stenosis measurements match clinical standards
- [ ] Cardiac planes auto-detected correctly
- [ ] Bone measurements accurate

### Phase 7
- [ ] AI plugins load and execute correctly
- [ ] DICOM SR generated properly
- [ ] Premium modules activate with valid license

---

## Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| dcm4che 5.x API changes | High | Pin specific version, isolate in service layer |
| Browser compatibility | Medium | Test early, use polyfills |
| PACS compatibility issues | High | Test with multiple PACS vendors early |
| Performance with large studies | High | Implement streaming, pagination early |
| WebGL compatibility | High | Graceful degradation, software fallback |
| 3D rendering performance | High | GPU detection, quality presets |
| AI model licensing | Medium | Clear licensing framework, offline support |
| Clinical accuracy | Critical | Validation testing, regulatory compliance |

---

## Dependencies

### External Dependencies
- Test PACS server (Orthanc, dcm4chee-arc)
- Sample DICOM datasets
- Test users and credentials
- WebGL 2.0 capable browsers
- VTK.js or similar for 3D rendering
- AI model servers (optional)

### Internal Dependencies
- Phase 2 requires Phase 1 completion
- Phase 3 requires Phase 1 completion
- Phase 4 requires Phase 3 completion
- Phase 5 requires Phase 3 completion
- Phase 6 requires Phase 5 completion
- Phase 7 requires Phases 4-6 completion

---

## Technology Stack for Advanced Features

### 3D Rendering
- **VTK.js** - Volume rendering, isosurface extraction
- **Cornerstone3D** - Medical imaging toolkit
- **Three.js** - 3D geometry and mesh rendering
- **WebGL 2.0** - GPU-accelerated rendering

### AI Integration
- **ONNX Runtime** - Client-side AI inference (optional)
- **REST API** - Server-side AI communication
- **DICOM SR** - Structured reporting format

### Performance
- **Web Workers** - Background processing
- **SharedArrayBuffer** - Memory sharing
- **IndexedDB** - Client-side caching
- **WebAssembly** - Performance-critical operations

---

## Next Steps

1. **Immediate**: Set up development environment
2. **Week 1**: Initialize Spring Boot and React projects
3. **Week 2**: Basic project structure and build verification
4. **Ongoing**: Weekly progress reviews and adjustments
