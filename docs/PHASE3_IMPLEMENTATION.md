# Phase 3: Core Viewer Features - Detailed Implementation Guide

This document provides detailed technical specifications, step-by-step implementation tasks, acceptance criteria, and code examples for Phase 3 (Core Viewer Features) of the Java DICOM Viewer project.

> **Duration**: 4-6 weeks (Weeks 11-16)  
> **Focus**: Measurement tools, annotations, key images, and basic MPR  
> **Prerequisites**: Phase 1 and Phase 2 completion, Cornerstone.js tools

---

## Table of Contents

1. [Overview](#overview)
2. [Week 11-12: Measurement Tools](#week-11-12-measurement-tools)
3. [Week 12-13: Annotation Tools](#week-12-13-annotation-tools)
4. [Week 13-14: Key Image & Comparison](#week-13-14-key-image--comparison)
5. [Week 14-16: Basic MPR](#week-14-16-basic-mpr)
6. [Acceptance Criteria](#acceptance-criteria)
7. [Testing Strategy](#testing-strategy)
8. [Deliverables Checklist](#deliverables-checklist)

---

## Overview

### Phase 3 Goals

1. **Implement essential measurement tools** - Enable linear, angle, and ROI measurements with persistence
2. **Add annotation capabilities** - Text labels, arrows, markers for clinical documentation
3. **Enable key image marking** - Allow users to flag important images in a study
4. **Basic MPR capabilities** - Orthogonal multiplanar reconstruction views

### Success Metrics

| Metric | Target |
|--------|--------|
| Measurement accuracy | Within 1mm of actual values |
| Annotation persistence | 100% reliable save/load |
| Key image toggle latency | < 100ms |
| MPR render time | < 500ms for 256x256x256 volume |
| Test coverage | > 75% |

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Phase 3 Architecture                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────┐    ┌─────────────────────┐    ┌──────────────────┐ │
│  │   Frontend Tools    │    │   Backend Services  │    │    Database      │ │
│  │                     │    │                     │    │                  │ │
│  │ - LengthTool        │◀──▶│ - MeasurementService│◀──▶│ - measurements   │ │
│  │ - AngleTool         │    │ - AnnotationService │    │ - annotations    │ │
│  │ - ROI Tools         │    │ - KeyImageService   │    │ - key_images     │ │
│  │ - ArrowAnnotate     │    │                     │    │                  │ │
│  │ - TextMarker        │    │                     │    │                  │ │
│  └─────────────────────┘    └─────────────────────┘    └──────────────────┘ │
│                                      │                          │           │
│                                      ▼                          ▼           │
│                            ┌─────────────────────┐    ┌──────────────────┐ │
│                            │   REST Controllers  │    │  Entity Models   │ │
│                            │                     │    │                  │ │
│                            │ - /api/measurements │    │ - Measurement    │ │
│                            │ - /api/annotations  │    │ - Annotation     │ │
│                            │ - /api/key-images   │    │ - KeyImage       │ │
│                            └─────────────────────┘    └──────────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Week 11-12: Measurement Tools

### 1. Linear Measurements

#### 1.1 Distance Measurement Tool

**Tasks:**
- [x] Integrate Cornerstone LengthTool
- [x] Display measurement value with units (mm)
- [x] Implement measurement persistence to backend
- [ ] Support measurement editing and deletion

**Frontend Implementation:**

```typescript
// Cornerstone tool registration
import { LengthTool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(LengthTool);
toolGroup.addTool(LengthTool.toolName);
toolGroup.setToolActive(LengthTool.toolName, {
  bindings: [{ mouseButton: MouseBindings.Primary }],
});
```

**API Endpoint:**

```
POST /api/measurements
Content-Type: application/json

{
  "studyInstanceUid": "1.2.3.4.5",
  "seriesInstanceUid": "1.2.3.4.5.6",
  "sopInstanceUid": "1.2.3.4.5.6.7",
  "measurementType": "LENGTH",
  "toolName": "Length",
  "value": 45.5,
  "unit": "mm",
  "pointsJson": "[{\"x\":100,\"y\":100},{\"x\":200,\"y\":200}]",
  "visible": true
}
```

### 2. Angle Measurements

#### 2.1 Standard Angle Tool

**Tasks:**
- [x] Integrate Cornerstone AngleTool
- [x] Display angle in degrees
- [ ] Support angle editing

```typescript
import { AngleTool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(AngleTool);
toolGroup.addTool(AngleTool.toolName);
```

#### 2.2 Cobb's Angle Tool

**Tasks:**
- [x] Integrate Cornerstone CobbAngleTool
- [ ] Display spinal curvature measurement

```typescript
import { CobbAngleTool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(CobbAngleTool);
toolGroup.addTool(CobbAngleTool.toolName);
```

### 3. ROI Measurements

#### 3.1 Rectangle ROI

**Tasks:**
- [x] Integrate RectangleROITool
- [ ] Display statistics (mean, std dev, min, max, area)
- [ ] Display Hounsfield units for CT

```typescript
import { RectangleROITool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(RectangleROITool);
```

#### 3.2 Elliptical ROI

**Tasks:**
- [x] Integrate EllipticalROITool
- [ ] Display ROI statistics

```typescript
import { EllipticalROITool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(EllipticalROITool);
```

#### 3.3 Probe Tool (Pixel Values)

**Tasks:**
- [x] Integrate ProbeTool
- [ ] Display HU values for CT images

```typescript
import { ProbeTool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(ProbeTool);
```

---

## Week 12-13: Annotation Tools

### 1. Arrow Annotations

**Tasks:**
- [x] Integrate ArrowAnnotateTool
- [ ] Support custom arrow styles
- [ ] Persist annotations to backend

```typescript
import { ArrowAnnotateTool } from '@cornerstonejs/tools';

cornerstoneTools.addTool(ArrowAnnotateTool);
```

### 2. Text Markers

**Tasks:**
- [x] Basic text annotation support (using PlanarFreehandROITool as fallback)
- [ ] Custom font size and color
- [ ] Text positioning and editing

### 3. Annotation Management

**API Endpoints:**

```
POST /api/annotations
GET /api/annotations/study/{studyInstanceUid}
GET /api/annotations/instance/{sopInstanceUid}
PUT /api/annotations/{id}
DELETE /api/annotations/{id}
POST /api/annotations/{id}/toggle-visibility
POST /api/annotations/{id}/toggle-lock
```

---

## Week 13-14: Key Image & Comparison

### 1. Key Image Marking

**Tasks:**
- [x] Implement key image toggle in toolbar
- [x] Create backend API for key image management
- [x] Display key image indicator on viewport
- [x] Show key images in side panel

**API Endpoints:**

```
POST /api/key-images/toggle
GET /api/key-images/study/{studyInstanceUid}
GET /api/key-images/check?sopInstanceUid={uid}&frameIndex={index}
DELETE /api/key-images/{id}
```

### 2. Study Comparison (Future)

**Tasks:**
- [ ] Prior study loading
- [ ] Side-by-side comparison view
- [ ] Synchronized scrolling option

---

## Week 14-16: Basic MPR (Future Implementation)

### 1. Orthogonal MPR

**Tasks:**
- [ ] Generate axial, sagittal, coronal views
- [ ] Real-time reformatting
- [ ] Synchronized crosshair navigation
- [ ] Linked window/level across views

### 2. MPR Layout

**Tasks:**
- [ ] 2x2 MPR layout
- [ ] 1x3 MPR layout
- [ ] Viewport swap/maximize

---

## Acceptance Criteria

### Measurement Tools
- [x] Length tool measures distances with ±1mm accuracy
- [x] Angle tool measures angles with ±0.5° accuracy
- [x] ROI tools display area in mm²
- [ ] All measurements persist to backend
- [ ] Measurements load correctly when reopening study

### Annotation Tools
- [x] Arrow annotations can be created
- [ ] Text annotations can be created and edited
- [ ] Annotations persist to backend
- [ ] Annotation visibility can be toggled

### Key Image Marking
- [x] Images can be marked/unmarked as key images
- [x] Key image status persists
- [x] Key image indicator displays on viewport
- [x] Key images listed in side panel

---

## Testing Strategy

### Unit Tests

```java
// Backend unit tests
@Test
void testCreateMeasurement() {
    Measurement measurement = new Measurement();
    measurement.setStudyInstanceUid("1.2.3.4.5");
    measurement.setMeasurementType(MeasurementType.LENGTH);
    measurement.setValue(45.5);
    measurement.setUnit("mm");
    
    Measurement saved = measurementService.create(measurement);
    
    assertNotNull(saved.getId());
    assertEquals("1.2.3.4.5", saved.getStudyInstanceUid());
}

@Test
void testToggleKeyImage() {
    KeyImage keyImage = new KeyImage();
    keyImage.setStudyInstanceUid("1.2.3.4.5");
    keyImage.setSopInstanceUid("1.2.3.4.5.6.7");
    
    // First toggle - creates key image
    KeyImage result = keyImageService.toggleKeyImage(keyImage);
    assertNotNull(result);
    
    // Second toggle - removes key image
    KeyImage removed = keyImageService.toggleKeyImage(keyImage);
    assertNull(removed);
}
```

### Integration Tests

```typescript
// Frontend integration tests
describe('Measurement Tools', () => {
  it('should create length measurement', async () => {
    // Activate length tool
    setActiveTool('Length');
    
    // Simulate measurement creation
    const measurement = await createMeasurement({
      startPoint: { x: 100, y: 100 },
      endPoint: { x: 200, y: 200 }
    });
    
    expect(measurement.value).toBeGreaterThan(0);
    expect(measurement.unit).toBe('mm');
  });
  
  it('should toggle key image', async () => {
    const result = await apiClient.toggleKeyImage({
      studyInstanceUid: '1.2.3.4.5',
      seriesInstanceUid: '1.2.3.4.5.6',
      sopInstanceUid: '1.2.3.4.5.6.7'
    });
    
    expect(result.action).toBe('added');
    expect(result.isKeyImage).toBe(true);
  });
});
```

---

## Deliverables Checklist

### Backend

- [x] Measurement entity and repository
- [x] Annotation entity and repository
- [x] KeyImage entity and repository
- [x] MeasurementService with CRUD operations
- [x] AnnotationService with CRUD operations
- [x] KeyImageService with toggle functionality
- [x] MeasurementController REST endpoints
- [x] AnnotationController REST endpoints
- [x] KeyImageController REST endpoints
- [x] Unit tests for services
- [x] Integration tests for controllers

### Frontend

- [x] Cornerstone measurement tools registered
- [x] Cornerstone annotation tools registered
- [x] Updated Toolbar with Phase 3 tools
- [x] Updated Viewer with tool handling
- [x] Key image toggle functionality
- [x] Key image indicator display
- [x] Key images panel in sidebar
- [x] API client extended with Phase 3 endpoints
- [x] TypeScript types for measurements and annotations
- [x] Measurement persistence to backend
- [x] Annotation persistence to backend

### Documentation

- [x] PHASE3_IMPLEMENTATION.md created
- [x] API documentation updated
- [x] User guide for new features

---

## Database Schema

### Measurements Table

```sql
CREATE TABLE measurements (
    id UUID PRIMARY KEY,
    study_instance_uid VARCHAR(255) NOT NULL,
    series_instance_uid VARCHAR(255) NOT NULL,
    sop_instance_uid VARCHAR(255) NOT NULL,
    image_id VARCHAR(255),
    frame_index INTEGER,
    measurement_type VARCHAR(50) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    label VARCHAR(255),
    value DOUBLE PRECISION,
    unit VARCHAR(50),
    points_json TEXT NOT NULL,
    roi_stats_json TEXT,
    color VARCHAR(50),
    visible BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_measurement_study ON measurements(study_instance_uid);
CREATE INDEX idx_measurement_series ON measurements(series_instance_uid);
CREATE INDEX idx_measurement_sop ON measurements(sop_instance_uid);
```

### Annotations Table

```sql
CREATE TABLE annotations (
    id UUID PRIMARY KEY,
    study_instance_uid VARCHAR(255) NOT NULL,
    series_instance_uid VARCHAR(255) NOT NULL,
    sop_instance_uid VARCHAR(255) NOT NULL,
    image_id VARCHAR(255),
    frame_index INTEGER,
    annotation_type VARCHAR(50) NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    text TEXT,
    points_json TEXT NOT NULL,
    style_json TEXT,
    color VARCHAR(50),
    font_size INTEGER,
    visible BOOLEAN DEFAULT TRUE,
    locked BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_annotation_study ON annotations(study_instance_uid);
CREATE INDEX idx_annotation_series ON annotations(series_instance_uid);
CREATE INDEX idx_annotation_sop ON annotations(sop_instance_uid);
```

### Key Images Table

```sql
CREATE TABLE key_images (
    id UUID PRIMARY KEY,
    study_instance_uid VARCHAR(255) NOT NULL,
    series_instance_uid VARCHAR(255) NOT NULL,
    sop_instance_uid VARCHAR(255) NOT NULL,
    image_id VARCHAR(255),
    frame_index INTEGER,
    instance_number INTEGER,
    description TEXT,
    category VARCHAR(100),
    window_width DOUBLE PRECISION,
    window_center DOUBLE PRECISION,
    thumbnail_path VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    UNIQUE(sop_instance_uid, frame_index)
);

CREATE INDEX idx_key_image_study ON key_images(study_instance_uid);
CREATE INDEX idx_key_image_series ON key_images(series_instance_uid);
CREATE INDEX idx_key_image_sop ON key_images(sop_instance_uid);
```
