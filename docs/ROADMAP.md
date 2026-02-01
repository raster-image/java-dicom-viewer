# Implementation Roadmap

This document outlines the phased implementation plan for the Java DICOM Viewer application. The plan is designed to deliver incremental value while building towards a comprehensive medical imaging platform.

## Overview

| Phase | Name | Duration | Key Deliverables |
|-------|------|----------|------------------|
| 1 | Foundation | 4-6 weeks | Basic viewer, DICOMweb support |
| 2 | Legacy PACS Support | 3-4 weeks | C-FIND, C-MOVE, C-STORE |
| 3 | Practical Features | 4-6 weeks | Worklist, reporting, user management |
| 4 | Advanced Features | 4-6 weeks | Advanced tools, integration, optimization |

---

## Phase 1: Foundation (Weeks 1-6)

### Goals
- Establish project infrastructure
- Implement basic DICOM viewing capabilities
- Add DICOMweb support for modern PACS

### Week 1-2: Project Setup

#### Backend Setup
- [ ] Initialize Spring Boot 4.x project with Maven
- [ ] Configure Java 25 with appropriate compiler settings
- [ ] Add dcm4che 5.x dependencies
- [ ] Set up project structure (controllers, services, repositories)
- [ ] Configure application properties
- [ ] Set up logging (SLF4J/Logback)
- [ ] Add basic health check endpoint

#### Frontend Setup
- [ ] Initialize React project with TypeScript
- [ ] Install and configure Cornerstone.js
- [ ] Set up Tailwind CSS for styling
- [ ] Configure build tools (Vite)
- [ ] Create basic component structure
- [ ] Set up routing (React Router)

#### Development Infrastructure
- [ ] Docker Compose for local development
- [ ] Set up test PACS (dcm4che-arc or Orthanc)
- [ ] Configure CI/CD pipeline basics

### Week 3-4: Basic DICOM Viewer

#### Image Rendering
- [ ] Integrate Cornerstone.js rendering engine
- [ ] Implement image loading from local source
- [ ] Add window/level adjustment tool
- [ ] Implement zoom and pan tools
- [ ] Add image scroll for series navigation
- [ ] Implement basic toolbar UI

#### Study Browser
- [ ] Create study list component
- [ ] Implement series thumbnail grid
- [ ] Add study metadata display
- [ ] Implement study selection

### Week 5-6: DICOMweb Integration

#### WADO-RS Implementation
- [ ] Create WADO-RS client service
- [ ] Implement instance retrieval
- [ ] Add series retrieval support
- [ ] Implement thumbnail generation
- [ ] Handle multipart/related responses

#### QIDO-RS Implementation
- [ ] Create QIDO-RS client service
- [ ] Implement study-level queries
- [ ] Add series-level queries
- [ ] Implement instance-level queries
- [ ] Add query pagination support

#### API Endpoints
- [ ] GET /api/studies - List/search studies
- [ ] GET /api/studies/{studyUID}/series - List series
- [ ] GET /api/studies/{studyUID}/series/{seriesUID}/instances - List instances
- [ ] GET /api/wado - WADO-RS proxy endpoint

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
- [ ] Create AE configuration model
- [ ] Implement AE management service
- [ ] Add PACS configuration UI
- [ ] Store AE settings in database

#### C-ECHO Implementation
- [ ] Implement C-ECHO SCU using dcm4che
- [ ] Create connectivity test endpoint
- [ ] Add connection status UI
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

- [ ] Implement Patient Root C-FIND
- [ ] Implement Study Root C-FIND
- [ ] Add query at study level
- [ ] Add query at series level
- [ ] Add query at instance level
- [ ] Handle query result pagination
- [ ] Implement query caching

#### Unified Query Service
- [ ] Abstract query interface for both protocols
- [ ] PACS type detection and routing
- [ ] Query result normalization
- [ ] Error handling and fallbacks

### Week 9-10: Retrieval Operations (C-MOVE/C-STORE)

#### C-MOVE SCU Implementation
- [ ] Implement C-MOVE request handling
- [ ] Configure local AE for receiving
- [ ] Handle C-MOVE sub-operations
- [ ] Implement retrieval progress tracking

#### C-STORE SCP Implementation
- [ ] Create C-STORE SCP service
- [ ] Implement instance reception handler
- [ ] Add local storage management
- [ ] Implement metadata extraction
- [ ] Store received instances in cache

#### Local Storage
- [ ] Design storage directory structure
- [ ] Implement DICOM file writing
- [ ] Create metadata database entries
- [ ] Add storage cleanup/management

### Phase 2 Deliverables
✅ C-ECHO for connectivity testing  
✅ C-FIND for querying legacy PACS  
✅ C-MOVE/C-STORE for image retrieval  
✅ Unified query interface  

---

## Phase 3: Practical Application Features (Weeks 11-16)

### Goals
- Transform viewer into practical clinical application
- Add worklist management
- Implement user authentication
- Add reporting capabilities

### Week 11-12: User Management

#### Authentication
- [ ] Implement Spring Security configuration
- [ ] Add JWT token generation/validation
- [ ] Create login/logout endpoints
- [ ] Implement login UI
- [ ] Add session management

#### Authorization
- [ ] Define role hierarchy (Admin, Radiologist, Technologist)
- [ ] Implement role-based access control
- [ ] Add endpoint security
- [ ] Create user management UI

#### User Administration
- [ ] User CRUD operations
- [ ] Password management
- [ ] Role assignment
- [ ] User preferences storage

### Week 13-14: Worklist Management

#### Modality Worklist (MWL)
- [ ] Implement MWL C-FIND SCU
- [ ] Create worklist data model
- [ ] Add worklist query service
- [ ] Implement worklist UI component

#### Study Worklist
- [ ] Create worklist view with filters
- [ ] Add status tracking (New, In Progress, Complete)
- [ ] Implement priority indicators
- [ ] Add worklist refresh mechanism

#### Worklist Features
- [ ] Search and filter functionality
- [ ] Sort by multiple criteria
- [ ] Date range filtering
- [ ] Modality filtering
- [ ] Status filtering

### Week 14-15: Basic Reporting

#### Report Data Model
- [ ] Create report entity
- [ ] Add report status workflow
- [ ] Implement report versioning
- [ ] Associate reports with studies

#### Reporting UI
- [ ] Create report editor component
- [ ] Add template support
- [ ] Implement auto-save
- [ ] Add report preview

#### Report Operations
- [ ] Create/update reports
- [ ] Report status transitions
- [ ] Report finalization
- [ ] Export to PDF

### Week 15-16: Audit & Compliance

#### Audit Logging
- [ ] Design audit log schema
- [ ] Implement audit interceptors
- [ ] Log study access events
- [ ] Log user actions
- [ ] Create audit log viewer

#### Compliance Features
- [ ] Patient data anonymization tools
- [ ] Export audit reports
- [ ] Session timeout management
- [ ] Failed login tracking

### Phase 3 Deliverables
✅ User authentication and authorization  
✅ Role-based access control  
✅ Worklist management  
✅ Basic reporting functionality  
✅ Audit logging  

---

## Phase 4: Advanced Features (Weeks 17-22)

### Goals
- Add advanced viewing tools
- Implement system integrations
- Optimize performance
- Production hardening

### Week 17-18: Advanced Viewer Tools

#### Measurement Tools
- [ ] Distance measurement
- [ ] Angle measurement
- [ ] Area/ROI measurement
- [ ] Hounsfield unit display (CT)
- [ ] Measurement persistence

#### Annotation Tools
- [ ] Text annotations
- [ ] Arrow markers
- [ ] Ellipse/rectangle tools
- [ ] Annotation saving/loading

#### Multi-planar Reconstruction (MPR)
- [ ] Implement axial/sagittal/coronal views
- [ ] Crosshair synchronization
- [ ] 3D cursor
- [ ] MPR view layouts

### Week 19-20: Integration & Interoperability

#### HL7/FHIR Integration
- [ ] HL7 v2.x message parsing
- [ ] FHIR ImagingStudy resource
- [ ] Patient demographics sync
- [ ] Order information integration

#### STOW-RS Implementation
- [ ] Create STOW-RS endpoint
- [ ] Handle multipart uploads
- [ ] Implement instance creation
- [ ] Add upload progress UI

#### External System Integration
- [ ] RIS integration (receive orders)
- [ ] Report delivery (HL7 ORU)
- [ ] Image routing configuration

### Week 20-21: Performance Optimization

#### Backend Optimization
- [ ] Connection pooling tuning
- [ ] Query result caching
- [ ] Image caching layer
- [ ] Async operation handling

#### Frontend Optimization
- [ ] Image prefetching
- [ ] Lazy loading optimization
- [ ] Virtual scrolling for large lists
- [ ] WebGL rendering optimization

#### Infrastructure
- [ ] Redis caching integration
- [ ] CDN for static assets
- [ ] Database query optimization
- [ ] Load testing and benchmarking

### Week 21-22: Production Readiness

#### Security Hardening
- [ ] Security audit
- [ ] Penetration testing fixes
- [ ] HTTPS configuration
- [ ] Security headers

#### Deployment
- [ ] Kubernetes manifests
- [ ] Helm charts
- [ ] Environment configurations
- [ ] Backup procedures

#### Documentation
- [ ] API documentation (OpenAPI)
- [ ] User manual
- [ ] Administrator guide
- [ ] Deployment guide

### Phase 4 Deliverables
✅ Advanced measurement tools  
✅ Annotation support  
✅ HL7/FHIR integration  
✅ Performance optimizations  
✅ Production-ready deployment  

---

## Success Metrics

### Phase 1
- [ ] Can load and display DICOM images
- [ ] Can query DICOMweb PACS
- [ ] Basic viewing tools functional

### Phase 2
- [ ] Can connect to legacy PACS
- [ ] C-FIND returns correct results
- [ ] C-MOVE retrieves images successfully

### Phase 3
- [ ] Users can log in and have appropriate access
- [ ] Worklist displays correctly
- [ ] Basic reports can be created

### Phase 4
- [ ] Measurements accurate to 1mm
- [ ] System handles 100+ concurrent users
- [ ] All security requirements met

---

## Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| dcm4che 5.x API changes | High | Pin specific version, isolate in service layer |
| Browser compatibility | Medium | Test early, use polyfills |
| PACS compatibility issues | High | Test with multiple PACS vendors early |
| Performance with large studies | High | Implement streaming, pagination early |

---

## Dependencies

### External Dependencies
- Test PACS server (Orthanc, dcm4chee-arc)
- Sample DICOM datasets
- Test users and credentials

### Internal Dependencies
- Phase 2 requires Phase 1 completion
- Phase 3 can start after Phase 1 (parallel with Phase 2)
- Phase 4 requires Phase 2 and 3 completion

---

## Next Steps

1. **Immediate**: Set up development environment
2. **Week 1**: Initialize Spring Boot and React projects
3. **Week 2**: Basic project structure and build verification
4. **Ongoing**: Weekly progress reviews and adjustments
