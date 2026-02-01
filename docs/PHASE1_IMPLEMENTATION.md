# Phase 1: Foundation - Detailed Implementation Guide

This document provides detailed technical specifications, step-by-step implementation tasks, acceptance criteria, and code examples for Phase 1 (Foundation) of the Java DICOM Viewer project.

> **Duration**: 4-6 weeks  
> **Focus**: Basic viewer functionality and DICOMweb support  
> **Prerequisites**: Java 25+, Node.js 20+, Docker (optional)

---

## Table of Contents

1. [Overview](#overview)
2. [Week 1-2: Project Setup](#week-1-2-project-setup)
3. [Week 3-4: Basic DICOM Viewer](#week-3-4-basic-dicom-viewer)
4. [Week 5-6: DICOMweb Integration](#week-5-6-dicomweb-integration)
5. [Acceptance Criteria](#acceptance-criteria)
6. [Testing Strategy](#testing-strategy)
7. [Deliverables Checklist](#deliverables-checklist)

---

## Overview

### Phase 1 Goals

1. **Establish project infrastructure** - Set up development environment, build tools, and CI/CD basics
2. **Implement basic DICOM viewing** - Display medical images with essential navigation tools
3. **Add DICOMweb support** - Enable connectivity to modern PACS systems via REST APIs

### Success Metrics

| Metric | Target |
|--------|--------|
| Image load time (single) | < 1 second |
| Series load time (500 images) | < 5 seconds |
| Window/Level response | < 50ms |
| Scroll response | < 16ms (60 fps) |
| Test coverage | > 70% |

---

## Week 1-2: Project Setup

### 1. Backend Setup

#### 1.1 Initialize Spring Boot Project

**Tasks:**
- [ ] Create Gradle project structure with Spring Boot 4.x
- [ ] Configure Java 25 compiler settings
- [ ] Add dcm4che 5.x dependencies for DICOM operations
- [ ] Set up project package structure

**Project Structure:**
```
backend/
├── src/main/java/com/dicomviewer/
│   ├── DicomViewerApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   ├── HealthController.java
│   │   ├── StudyController.java
│   │   └── WadoController.java
│   ├── service/
│   │   ├── DicomWebService.java
│   │   ├── StudyService.java
│   │   └── ImageService.java
│   ├── model/
│   │   ├── Study.java
│   │   ├── Series.java
│   │   └── Instance.java
│   ├── repository/
│   │   └── StudyCacheRepository.java
│   └── dicom/
│       └── web/
│           ├── QidoRsClient.java
│           └── WadoRsClient.java
├── src/main/resources/
│   └── application.properties
└── src/test/java/
```

**Configuration (application.properties):**
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# DICOM Configuration
dicom.aetitle=DICOMVIEWER
dicom.storage.path=/var/dicom/storage
dicom.cache.enabled=true
dicom.cache.max-size-gb=10

# Database
spring.datasource.url=jdbc:h2:mem:dicomdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# Logging
logging.level.com.dicomviewer=DEBUG
logging.level.org.dcm4che3=INFO

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

**Acceptance Criteria:**
- Application starts without errors
- Health endpoint returns 200 OK
- All dependencies resolve correctly
- Unit tests pass

---

#### 1.2 HealthController Implementation

**File: `HealthController.java`**
```java
package com.dicomviewer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(summary = "Basic health check")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "service", "DICOM Viewer Backend",
            "version", "1.0.0-SNAPSHOT"
        ));
    }

    @GetMapping("/ready")
    @Operation(summary = "Readiness check")
    public ResponseEntity<Map<String, Object>> ready() {
        // Add checks for database, PACS connectivity, etc.
        return ResponseEntity.ok(Map.of(
            "status", "READY",
            "checks", Map.of(
                "database", "UP",
                "dicomServices", "UP"
            )
        ));
    }
}
```

---

### 2. Frontend Setup

#### 2.1 Initialize React Project

**Tasks:**
- [ ] Create Vite project with React and TypeScript
- [ ] Install and configure Cornerstone.js
- [ ] Set up Tailwind CSS
- [ ] Configure ESLint and Prettier
- [ ] Create basic component structure

**Project Structure:**
```
frontend/
├── src/
│   ├── main.tsx                    # Application entry point
│   ├── App.tsx                     # Root component with routing
│   ├── index.css                   # Global styles (Tailwind)
│   ├── components/
│   │   ├── Layout.tsx              # Main layout wrapper
│   │   ├── Dashboard.tsx           # Home dashboard
│   │   ├── StudyBrowser.tsx        # Study list component
│   │   ├── Viewer.tsx              # DICOM image viewer
│   │   ├── Toolbar.tsx             # Viewer toolbar
│   │   └── Settings.tsx            # Settings panel
│   ├── hooks/
│   │   ├── useCornerstone.ts       # Cornerstone initialization hook
│   │   ├── useStudies.ts           # Study data fetching hook
│   │   └── useViewerTools.ts       # Viewer tools state hook
│   ├── services/
│   │   ├── api.ts                  # Base API configuration
│   │   ├── studyService.ts         # Study-related API calls
│   │   └── wadoService.ts          # WADO image retrieval
│   ├── types/
│   │   ├── index.ts                # Common TypeScript types
│   │   ├── dicom.ts                # DICOM-specific types
│   │   └── api.ts                  # API response types
│   ├── utils/
│   │   ├── cornerstone.ts          # Cornerstone utilities
│   │   └── formatters.ts           # Data formatters
│   └── stores/
│       └── viewerStore.ts          # Zustand viewer state
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.js
└── postcss.config.js
```

---

#### 2.2 Cornerstone.js Integration

**File: `hooks/useCornerstone.ts`**
```typescript
import { useEffect, useState } from 'react';
import * as cornerstone from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
import cornerstoneDICOMImageLoader from '@cornerstonejs/dicom-image-loader';
import dicomParser from 'dicom-parser';

interface CornerstoneState {
  isInitialized: boolean;
  error: Error | null;
}

export function useCornerstone(): CornerstoneState {
  const [state, setState] = useState<CornerstoneState>({
    isInitialized: false,
    error: null,
  });

  useEffect(() => {
    async function initializeCornerstone() {
      try {
        // Initialize Cornerstone core
        await cornerstone.init();

        // Configure DICOM image loader
        cornerstoneDICOMImageLoader.external.cornerstone = cornerstone;
        cornerstoneDICOMImageLoader.external.dicomParser = dicomParser;

        // Configure web worker for decoding
        cornerstoneDICOMImageLoader.configure({
          useWebWorkers: true,
          decodeConfig: {
            convertFloatPixelDataToInt: false,
          },
        });

        // Initialize Cornerstone Tools
        cornerstoneTools.init();

        // Add commonly used tools
        cornerstoneTools.addTool(cornerstoneTools.WindowLevelTool);
        cornerstoneTools.addTool(cornerstoneTools.PanTool);
        cornerstoneTools.addTool(cornerstoneTools.ZoomTool);
        cornerstoneTools.addTool(cornerstoneTools.StackScrollTool);

        setState({ isInitialized: true, error: null });
      } catch (error) {
        setState({ isInitialized: false, error: error as Error });
        console.error('Failed to initialize Cornerstone:', error);
      }
    }

    initializeCornerstone();
  }, []);

  return state;
}
```

---

#### 2.3 TypeScript Types for DICOM

**File: `types/dicom.ts`**
```typescript
/**
 * DICOM Study representation
 */
export interface Study {
  studyInstanceUid: string;
  patientId: string;
  patientName: string;
  patientBirthDate?: string;
  patientSex?: string;
  studyDate: string;
  studyTime?: string;
  studyDescription?: string;
  accessionNumber?: string;
  modalitiesInStudy: string[];
  numberOfStudyRelatedSeries: number;
  numberOfStudyRelatedInstances: number;
  sourcePacs?: PacsSource;
}

/**
 * DICOM Series representation
 */
export interface Series {
  seriesInstanceUid: string;
  seriesNumber: number;
  seriesDescription?: string;
  modality: string;
  bodyPartExamined?: string;
  numberOfSeriesRelatedInstances: number;
  instances?: Instance[];
}

/**
 * DICOM Instance (Image) representation
 */
export interface Instance {
  sopInstanceUid: string;
  sopClassUid: string;
  instanceNumber: number;
  rows: number;
  columns: number;
  bitsAllocated?: number;
  imagePositionPatient?: [number, number, number];
  imageOrientationPatient?: [number, number, number, number, number, number];
  pixelSpacing?: [number, number];
  sliceThickness?: number;
  windowCenter?: number | number[];
  windowWidth?: number | number[];
}

/**
 * PACS source information
 */
export interface PacsSource {
  id: string;
  name: string;
  type: 'DICOMWEB' | 'LEGACY';
}

/**
 * Window/Level preset
 */
export interface WindowLevelPreset {
  name: string;
  windowWidth: number;
  windowCenter: number;
  modality?: string;
}

/**
 * Default Window/Level presets for common imaging scenarios
 */
export const DEFAULT_WINDOW_LEVEL_PRESETS: WindowLevelPreset[] = [
  { name: 'CT Abdomen', windowWidth: 400, windowCenter: 50, modality: 'CT' },
  { name: 'CT Lung', windowWidth: 1500, windowCenter: -600, modality: 'CT' },
  { name: 'CT Bone', windowWidth: 2000, windowCenter: 500, modality: 'CT' },
  { name: 'CT Brain', windowWidth: 80, windowCenter: 40, modality: 'CT' },
  { name: 'CT Soft Tissue', windowWidth: 350, windowCenter: 50, modality: 'CT' },
  { name: 'MR T1', windowWidth: 500, windowCenter: 250, modality: 'MR' },
  { name: 'MR T2', windowWidth: 400, windowCenter: 200, modality: 'MR' },
  { name: 'MR FLAIR', windowWidth: 1200, windowCenter: 600, modality: 'MR' },
];
```

---

### 3. Development Infrastructure

#### 3.1 Docker Compose Setup

**File: `docker-compose.yml`**
```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - PACS_HOST=orthanc
      - PACS_PORT=8042
    depends_on:
      - postgres
      - orthanc
    volumes:
      - dicom-storage:/var/dicom/storage

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "3000:3000"
    environment:
      - VITE_API_URL=http://localhost:8080/api
    depends_on:
      - backend

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: dicomviewer
      POSTGRES_USER: dicom
      POSTGRES_PASSWORD: dicom_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  orthanc:
    image: orthancteam/orthanc:24.1.2
    ports:
      - "4242:4242"  # DICOM port
      - "8042:8042"  # HTTP port
    environment:
      ORTHANC__REGISTERED_USERS: |
        {
          "admin": "orthanc"
        }
      ORTHANC__DICOM_WEB__ENABLE: "true"
    volumes:
      - orthanc-data:/var/lib/orthanc/db

volumes:
  dicom-storage:
  postgres-data:
  orthanc-data:
```

---

#### 3.2 Backend Dockerfile

**File: `backend/Dockerfile`**
```dockerfile
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew .

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application
RUN ./gradlew bootJar --no-daemon

# Runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy built JAR
COPY --from=build /app/build/libs/*.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Week 3-4: Basic DICOM Viewer

### 1. Image Rendering with Cornerstone.js

#### 1.1 Viewer Component

**File: `components/Viewer.tsx`**
```typescript
import React, { useEffect, useRef, useState, useCallback } from 'react';
import * as cornerstone from '@cornerstonejs/core';
import * as cornerstoneTools from '@cornerstonejs/tools';
import { useCornerstone } from '../hooks/useCornerstone';
import { Toolbar } from './Toolbar';
import type { Series, Instance } from '../types/dicom';

interface ViewerProps {
  series: Series;
  onToolChange?: (tool: string) => void;
}

type ViewerTool = 'WindowLevel' | 'Pan' | 'Zoom' | 'StackScroll';

const TOOL_GROUP_ID = 'STACK_TOOL_GROUP';
const VIEWPORT_ID = 'MAIN_VIEWPORT';

export const Viewer: React.FC<ViewerProps> = ({ series, onToolChange }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const { isInitialized, error } = useCornerstone();
  const [activeTool, setActiveTool] = useState<ViewerTool>('WindowLevel');
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [viewport, setViewport] = useState<cornerstone.Types.IStackViewport | null>(null);
  const [renderingEngine, setRenderingEngine] = useState<cornerstone.RenderingEngine | null>(null);

  // Initialize rendering engine and viewport
  useEffect(() => {
    if (!isInitialized || !containerRef.current) return;

    const initViewer = async () => {
      // Create rendering engine
      const engine = new cornerstone.RenderingEngine('VIEWER_ENGINE');
      setRenderingEngine(engine);

      // Create stack viewport
      const viewportInput = {
        viewportId: VIEWPORT_ID,
        type: cornerstone.Enums.ViewportType.STACK,
        element: containerRef.current!,
        defaultOptions: {
          background: [0, 0, 0] as cornerstone.Types.Point3,
        },
      };

      engine.enableElement(viewportInput);
      const vp = engine.getViewport(VIEWPORT_ID) as cornerstone.Types.IStackViewport;
      setViewport(vp);

      // Setup tool group
      const toolGroup = cornerstoneTools.ToolGroupManager.createToolGroup(TOOL_GROUP_ID);
      if (toolGroup) {
        toolGroup.addViewport(VIEWPORT_ID, 'VIEWER_ENGINE');
        
        // Configure tools
        toolGroup.addTool(cornerstoneTools.WindowLevelTool.toolName);
        toolGroup.addTool(cornerstoneTools.PanTool.toolName);
        toolGroup.addTool(cornerstoneTools.ZoomTool.toolName);
        toolGroup.addTool(cornerstoneTools.StackScrollTool.toolName);

        // Set default tool as active
        toolGroup.setToolActive(cornerstoneTools.WindowLevelTool.toolName, {
          bindings: [{ mouseButton: cornerstoneTools.Enums.MouseBindings.Primary }],
        });
        
        // Set scroll tool for mouse wheel
        toolGroup.setToolActive(cornerstoneTools.StackScrollTool.toolName, {
          bindings: [{ mouseButton: cornerstoneTools.Enums.MouseBindings.Wheel }],
        });
      }
    };

    initViewer();

    return () => {
      renderingEngine?.destroy();
      cornerstoneTools.ToolGroupManager.destroyToolGroup(TOOL_GROUP_ID);
    };
  }, [isInitialized]);

  // Load images when series changes
  useEffect(() => {
    if (!viewport || !series.instances?.length) return;

    const imageIds = series.instances.map(
      (instance) => `wadors:${getWadoRsUrl(series, instance)}`
    );

    viewport.setStack(imageIds, 0);
    viewport.render();
  }, [viewport, series]);

  // Handle tool change
  const handleToolChange = useCallback((tool: ViewerTool) => {
    const toolGroup = cornerstoneTools.ToolGroupManager.getToolGroup(TOOL_GROUP_ID);
    if (!toolGroup) return;

    // Deactivate current tool
    toolGroup.setToolPassive(getToolName(activeTool));

    // Activate new tool
    toolGroup.setToolActive(getToolName(tool), {
      bindings: [{ mouseButton: cornerstoneTools.Enums.MouseBindings.Primary }],
    });

    setActiveTool(tool);
    onToolChange?.(tool);
  }, [activeTool, onToolChange]);

  // Navigate to specific image
  const handleNavigateImage = useCallback((index: number) => {
    if (!viewport || !series.instances) return;
    
    const clampedIndex = Math.max(0, Math.min(index, series.instances.length - 1));
    viewport.setImageIdIndex(clampedIndex);
    setCurrentImageIndex(clampedIndex);
  }, [viewport, series.instances]);

  if (error) {
    return (
      <div className="flex items-center justify-center h-full bg-gray-900 text-red-500">
        <div className="text-center">
          <h3 className="text-lg font-semibold">Failed to initialize viewer</h3>
          <p className="text-sm">{error.message}</p>
        </div>
      </div>
    );
  }

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center h-full bg-gray-900 text-white">
        <div className="animate-pulse">Initializing viewer...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-gray-900">
      <Toolbar
        activeTool={activeTool}
        onToolChange={handleToolChange}
        currentImage={currentImageIndex + 1}
        totalImages={series.instances?.length ?? 0}
        onNavigate={handleNavigateImage}
      />
      <div
        ref={containerRef}
        className="flex-1 w-full"
        style={{ minHeight: '400px' }}
      />
      <div className="p-2 text-white text-sm bg-gray-800">
        <span>{series.seriesDescription || 'Unknown Series'}</span>
        <span className="mx-2">|</span>
        <span>Image {currentImageIndex + 1} of {series.instances?.length ?? 0}</span>
      </div>
    </div>
  );
};

// Helper functions
function getToolName(tool: ViewerTool): string {
  const toolNames: Record<ViewerTool, string> = {
    WindowLevel: cornerstoneTools.WindowLevelTool.toolName,
    Pan: cornerstoneTools.PanTool.toolName,
    Zoom: cornerstoneTools.ZoomTool.toolName,
    StackScroll: cornerstoneTools.StackScrollTool.toolName,
  };
  return toolNames[tool];
}

function getWadoRsUrl(series: Series, instance: Instance): string {
  const baseUrl = import.meta.env.VITE_WADO_RS_URL || '/api/wado';
  return `${baseUrl}/studies/${series.seriesInstanceUid.split('.').slice(0, -1).join('.')}/series/${series.seriesInstanceUid}/instances/${instance.sopInstanceUid}`;
}
```

---

#### 1.2 Toolbar Component

**File: `components/Toolbar.tsx`**
```typescript
import React from 'react';

type ViewerTool = 'WindowLevel' | 'Pan' | 'Zoom' | 'StackScroll';

interface ToolbarProps {
  activeTool: ViewerTool;
  onToolChange: (tool: ViewerTool) => void;
  currentImage: number;
  totalImages: number;
  onNavigate: (index: number) => void;
}

interface ToolButton {
  id: ViewerTool;
  label: string;
  icon: string;
  shortcut: string;
}

const TOOLS: ToolButton[] = [
  { id: 'WindowLevel', label: 'Window/Level', icon: '◐', shortcut: 'W' },
  { id: 'Pan', label: 'Pan', icon: '✥', shortcut: 'P' },
  { id: 'Zoom', label: 'Zoom', icon: '🔍', shortcut: 'Z' },
  { id: 'StackScroll', label: 'Scroll', icon: '↕', shortcut: 'S' },
];

export const Toolbar: React.FC<ToolbarProps> = ({
  activeTool,
  onToolChange,
  currentImage,
  totalImages,
  onNavigate,
}) => {
  return (
    <div className="flex items-center justify-between p-2 bg-gray-800 border-b border-gray-700">
      {/* Tool buttons */}
      <div className="flex gap-1">
        {TOOLS.map((tool) => (
          <button
            key={tool.id}
            onClick={() => onToolChange(tool.id)}
            className={`
              px-3 py-2 rounded text-sm font-medium transition-colors
              ${activeTool === tool.id
                ? 'bg-blue-600 text-white'
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
              }
            `}
            title={`${tool.label} (${tool.shortcut})`}
          >
            <span className="mr-1">{tool.icon}</span>
            {tool.label}
          </button>
        ))}
      </div>

      {/* Navigation */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => onNavigate(currentImage - 2)}
          disabled={currentImage <= 1}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
        >
          ◀
        </button>
        <span className="text-white text-sm min-w-[80px] text-center">
          {currentImage} / {totalImages}
        </span>
        <button
          onClick={() => onNavigate(currentImage)}
          disabled={currentImage >= totalImages}
          className="px-2 py-1 bg-gray-700 text-white rounded disabled:opacity-50"
        >
          ▶
        </button>
      </div>

      {/* Window/Level presets */}
      <div className="flex gap-1">
        <select
          className="px-2 py-1 bg-gray-700 text-white rounded text-sm"
          onChange={(e) => {
            // Apply preset logic here
            console.log('Apply preset:', e.target.value);
          }}
        >
          <option value="">W/L Presets</option>
          <option value="ct-abdomen">CT Abdomen</option>
          <option value="ct-lung">CT Lung</option>
          <option value="ct-bone">CT Bone</option>
          <option value="ct-brain">CT Brain</option>
        </select>
      </div>
    </div>
  );
};
```

---

### 2. Study Browser

#### 2.1 StudyBrowser Component

**File: `components/StudyBrowser.tsx`**
```typescript
import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { studyService } from '../services/studyService';
import type { Study } from '../types/dicom';

interface StudyBrowserProps {
  onStudySelect: (study: Study) => void;
}

interface StudyFilters {
  patientName?: string;
  patientId?: string;
  studyDate?: string;
  modality?: string;
  accessionNumber?: string;
}

export const StudyBrowser: React.FC<StudyBrowserProps> = ({ onStudySelect }) => {
  const [filters, setFilters] = useState<StudyFilters>({});
  const [selectedStudy, setSelectedStudy] = useState<string | null>(null);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['studies', filters],
    queryFn: () => studyService.searchStudies(filters),
  });

  const handleStudyClick = (study: Study) => {
    setSelectedStudy(study.studyInstanceUid);
    onStudySelect(study);
  };

  const handleFilterChange = (key: keyof StudyFilters, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value || undefined }));
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    refetch();
  };

  return (
    <div className="flex flex-col h-full bg-white">
      {/* Search Filters */}
      <form onSubmit={handleSearch} className="p-4 border-b">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <input
            type="text"
            placeholder="Patient Name"
            value={filters.patientName || ''}
            onChange={(e) => handleFilterChange('patientName', e.target.value)}
            className="px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="text"
            placeholder="Patient ID"
            value={filters.patientId || ''}
            onChange={(e) => handleFilterChange('patientId', e.target.value)}
            className="px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="date"
            placeholder="Study Date"
            value={filters.studyDate || ''}
            onChange={(e) => handleFilterChange('studyDate', e.target.value)}
            className="px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
          />
          <select
            value={filters.modality || ''}
            onChange={(e) => handleFilterChange('modality', e.target.value)}
            className="px-3 py-2 border rounded focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Modalities</option>
            <option value="CT">CT</option>
            <option value="MR">MR</option>
            <option value="CR">CR</option>
            <option value="DX">DX</option>
            <option value="US">US</option>
            <option value="XA">XA</option>
          </select>
        </div>
        <div className="mt-4 flex justify-end">
          <button
            type="submit"
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Search
          </button>
        </div>
      </form>

      {/* Study List */}
      <div className="flex-1 overflow-auto">
        {isLoading && (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
          </div>
        )}

        {error && (
          <div className="p-4 text-red-600">
            Failed to load studies: {(error as Error).message}
          </div>
        )}

        {data?.studies && (
          <table className="w-full">
            <thead className="bg-gray-100 sticky top-0">
              <tr>
                <th className="px-4 py-2 text-left">Patient Name</th>
                <th className="px-4 py-2 text-left">Patient ID</th>
                <th className="px-4 py-2 text-left">Study Date</th>
                <th className="px-4 py-2 text-left">Description</th>
                <th className="px-4 py-2 text-left">Modality</th>
                <th className="px-4 py-2 text-right">Images</th>
              </tr>
            </thead>
            <tbody>
              {data.studies.map((study) => (
                <tr
                  key={study.studyInstanceUid}
                  onClick={() => handleStudyClick(study)}
                  className={`
                    cursor-pointer hover:bg-blue-50 transition-colors
                    ${selectedStudy === study.studyInstanceUid ? 'bg-blue-100' : ''}
                  `}
                >
                  <td className="px-4 py-3 border-b">{formatPatientName(study.patientName)}</td>
                  <td className="px-4 py-3 border-b">{study.patientId}</td>
                  <td className="px-4 py-3 border-b">{formatDate(study.studyDate)}</td>
                  <td className="px-4 py-3 border-b">{study.studyDescription || '-'}</td>
                  <td className="px-4 py-3 border-b">{study.modalitiesInStudy.join(', ')}</td>
                  <td className="px-4 py-3 border-b text-right">
                    {study.numberOfStudyRelatedInstances}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {data?.studies.length === 0 && (
          <div className="p-8 text-center text-gray-500">
            No studies found. Try adjusting your search criteria.
          </div>
        )}
      </div>
    </div>
  );
};

// Helper functions
function formatPatientName(name: string): string {
  if (!name) return 'Unknown';
  // DICOM names are in format "LAST^FIRST^MIDDLE"
  const parts = name.split('^');
  if (parts.length >= 2) {
    return `${parts[1]} ${parts[0]}`;
  }
  return name;
}

function formatDate(dateStr: string): string {
  if (!dateStr || dateStr.length !== 8) return dateStr;
  // DICOM date format: YYYYMMDD
  const year = dateStr.substring(0, 4);
  const month = dateStr.substring(4, 6);
  const day = dateStr.substring(6, 8);
  return `${year}-${month}-${day}`;
}
```

---

## Week 5-6: DICOMweb Integration

### 1. Backend QIDO-RS Client

#### 1.1 QidoRsClient Service

**File: `dicom/web/QidoRsClient.java`**
```java
package com.dicomviewer.dicom.web;

import com.dicomviewer.model.Study;
import com.dicomviewer.model.Series;
import com.dicomviewer.model.Instance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * QIDO-RS (Query based on ID for DICOM Objects - RESTful Services) client
 * Implements DICOMweb query operations for studies, series, and instances.
 */
@Component
public class QidoRsClient {
    
    private static final Logger log = LoggerFactory.getLogger(QidoRsClient.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // DICOM tags as hex strings
    private static final String TAG_PATIENT_NAME = "00100010";
    private static final String TAG_PATIENT_ID = "00100020";
    private static final String TAG_PATIENT_BIRTH_DATE = "00100030";
    private static final String TAG_PATIENT_SEX = "00100040";
    private static final String TAG_STUDY_INSTANCE_UID = "0020000D";
    private static final String TAG_STUDY_DATE = "00080020";
    private static final String TAG_STUDY_TIME = "00080030";
    private static final String TAG_STUDY_DESCRIPTION = "00081030";
    private static final String TAG_ACCESSION_NUMBER = "00080050";
    private static final String TAG_MODALITIES_IN_STUDY = "00080061";
    private static final String TAG_NUMBER_OF_STUDY_RELATED_SERIES = "00201206";
    private static final String TAG_NUMBER_OF_STUDY_RELATED_INSTANCES = "00201208";
    private static final String TAG_SERIES_INSTANCE_UID = "0020000E";
    private static final String TAG_SERIES_NUMBER = "00200011";
    private static final String TAG_SERIES_DESCRIPTION = "0008103E";
    private static final String TAG_MODALITY = "00080060";
    private static final String TAG_BODY_PART_EXAMINED = "00180015";
    private static final String TAG_NUMBER_OF_SERIES_RELATED_INSTANCES = "00201209";
    private static final String TAG_SOP_INSTANCE_UID = "00080018";
    private static final String TAG_SOP_CLASS_UID = "00080016";
    private static final String TAG_INSTANCE_NUMBER = "00200013";
    private static final String TAG_ROWS = "00280010";
    private static final String TAG_COLUMNS = "00280011";
    
    public QidoRsClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Search for studies matching the given criteria
     */
    public List<Study> searchStudies(String qidoRsUrl, Map<String, String> queryParams) {
        String url = buildQueryUrl(qidoRsUrl, "studies", queryParams);
        log.debug("QIDO-RS study query: {}", url);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            createRequestEntity(),
            String.class
        );
        
        return parseStudyResponse(response.getBody());
    }
    
    /**
     * Get all series for a study
     */
    public List<Series> getSeriesForStudy(String qidoRsUrl, String studyInstanceUid) {
        String url = buildQueryUrl(
            qidoRsUrl, 
            String.format("studies/%s/series", studyInstanceUid),
            Map.of()
        );
        log.debug("QIDO-RS series query: {}", url);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            createRequestEntity(),
            String.class
        );
        
        return parseSeriesResponse(response.getBody());
    }
    
    /**
     * Get all instances for a series
     */
    public List<Instance> getInstancesForSeries(
            String qidoRsUrl, 
            String studyInstanceUid, 
            String seriesInstanceUid) {
        String url = buildQueryUrl(
            qidoRsUrl,
            String.format("studies/%s/series/%s/instances", studyInstanceUid, seriesInstanceUid),
            Map.of()
        );
        log.debug("QIDO-RS instance query: {}", url);
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            createRequestEntity(),
            String.class
        );
        
        return parseInstanceResponse(response.getBody());
    }
    
    private String buildQueryUrl(String baseUrl, String path, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(baseUrl)
            .path("/" + path);
        
        queryParams.forEach(builder::queryParam);
        
        // Add common include fields for studies
        if (path.equals("studies")) {
            builder.queryParam("includefield", "all");
        }
        
        return builder.build().toUriString();
    }
    
    private HttpEntity<?> createRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/dicom+json");
        return new HttpEntity<>(headers);
    }
    
    private List<Study> parseStudyResponse(String jsonResponse) {
        List<Study> studies = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Study study = new Study();
                    study.setStudyInstanceUid(getStringValue(node, TAG_STUDY_INSTANCE_UID));
                    study.setPatientId(getStringValue(node, TAG_PATIENT_ID));
                    study.setPatientName(getStringValue(node, TAG_PATIENT_NAME));
                    study.setPatientBirthDate(getStringValue(node, TAG_PATIENT_BIRTH_DATE));
                    study.setPatientSex(getStringValue(node, TAG_PATIENT_SEX));
                    study.setStudyDate(getStringValue(node, TAG_STUDY_DATE));
                    study.setStudyTime(getStringValue(node, TAG_STUDY_TIME));
                    study.setStudyDescription(getStringValue(node, TAG_STUDY_DESCRIPTION));
                    study.setAccessionNumber(getStringValue(node, TAG_ACCESSION_NUMBER));
                    study.setModalitiesInStudy(getStringArrayValue(node, TAG_MODALITIES_IN_STUDY));
                    study.setNumberOfStudyRelatedSeries(getIntValue(node, TAG_NUMBER_OF_STUDY_RELATED_SERIES));
                    study.setNumberOfStudyRelatedInstances(getIntValue(node, TAG_NUMBER_OF_STUDY_RELATED_INSTANCES));
                    studies.add(study);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse QIDO-RS study response", e);
        }
        return studies;
    }
    
    private List<Series> parseSeriesResponse(String jsonResponse) {
        List<Series> seriesList = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Series series = new Series();
                    series.setSeriesInstanceUid(getStringValue(node, TAG_SERIES_INSTANCE_UID));
                    series.setSeriesNumber(getIntValue(node, TAG_SERIES_NUMBER));
                    series.setSeriesDescription(getStringValue(node, TAG_SERIES_DESCRIPTION));
                    series.setModality(getStringValue(node, TAG_MODALITY));
                    series.setBodyPartExamined(getStringValue(node, TAG_BODY_PART_EXAMINED));
                    series.setNumberOfSeriesRelatedInstances(getIntValue(node, TAG_NUMBER_OF_SERIES_RELATED_INSTANCES));
                    seriesList.add(series);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse QIDO-RS series response", e);
        }
        return seriesList;
    }
    
    private List<Instance> parseInstanceResponse(String jsonResponse) {
        List<Instance> instances = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.isArray()) {
                for (JsonNode node : root) {
                    Instance instance = new Instance();
                    instance.setSopInstanceUid(getStringValue(node, TAG_SOP_INSTANCE_UID));
                    instance.setSopClassUid(getStringValue(node, TAG_SOP_CLASS_UID));
                    instance.setInstanceNumber(getIntValue(node, TAG_INSTANCE_NUMBER));
                    instance.setRows(getIntValue(node, TAG_ROWS));
                    instance.setColumns(getIntValue(node, TAG_COLUMNS));
                    instances.add(instance);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse QIDO-RS instance response", e);
        }
        return instances;
    }
    
    // Helper methods for extracting DICOM JSON values
    private String getStringValue(JsonNode node, String tag) {
        JsonNode tagNode = node.get(tag);
        if (tagNode != null && tagNode.has("Value") && tagNode.get("Value").isArray()) {
            JsonNode value = tagNode.get("Value").get(0);
            if (value != null) {
                if (value.isTextual()) {
                    return value.asText();
                } else if (value.has("Alphabetic")) {
                    return value.get("Alphabetic").asText();
                }
            }
        }
        return null;
    }
    
    private int getIntValue(JsonNode node, String tag) {
        JsonNode tagNode = node.get(tag);
        if (tagNode != null && tagNode.has("Value") && tagNode.get("Value").isArray()) {
            JsonNode value = tagNode.get("Value").get(0);
            if (value != null && value.isNumber()) {
                return value.asInt();
            }
        }
        return 0;
    }
    
    private List<String> getStringArrayValue(JsonNode node, String tag) {
        List<String> values = new ArrayList<>();
        JsonNode tagNode = node.get(tag);
        if (tagNode != null && tagNode.has("Value") && tagNode.get("Value").isArray()) {
            for (JsonNode value : tagNode.get("Value")) {
                if (value.isTextual()) {
                    values.add(value.asText());
                }
            }
        }
        return values;
    }
}
```

---

### 2. Backend WADO-RS Client

#### 2.1 WadoRsClient Service

**File: `dicom/web/WadoRsClient.java`**
```java
package com.dicomviewer.dicom.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * WADO-RS (Web Access to DICOM Objects - RESTful Services) client
 * Implements DICOMweb retrieval operations for DICOM instances.
 */
@Component
public class WadoRsClient {
    
    private static final Logger log = LoggerFactory.getLogger(WadoRsClient.class);
    
    private final RestTemplate restTemplate;
    
    public WadoRsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Retrieve a DICOM instance as raw bytes
     */
    public byte[] retrieveInstance(
            String wadoRsUrl,
            String studyInstanceUid,
            String seriesInstanceUid,
            String sopInstanceUid) {
        
        String url = String.format(
            "%s/studies/%s/series/%s/instances/%s",
            wadoRsUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid
        );
        
        log.debug("WADO-RS instance retrieval: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/dicom");
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class
        );
        
        return response.getBody();
    }
    
    /**
     * Retrieve a rendered image (JPEG/PNG)
     */
    public byte[] retrieveRendered(
            String wadoRsUrl,
            String studyInstanceUid,
            String seriesInstanceUid,
            String sopInstanceUid,
            Integer windowWidth,
            Integer windowCenter,
            Integer quality) {
        
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(String.format(
            "%s/studies/%s/series/%s/instances/%s/rendered",
            wadoRsUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid
        ));
        
        // Add query parameters
        boolean hasParams = false;
        if (windowWidth != null && windowCenter != null) {
            urlBuilder.append("?window=").append(windowWidth).append(",").append(windowCenter);
            hasParams = true;
        }
        if (quality != null) {
            urlBuilder.append(hasParams ? "&" : "?").append("quality=").append(quality);
        }
        
        String url = urlBuilder.toString();
        log.debug("WADO-RS rendered retrieval: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "image/jpeg");
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class
        );
        
        return response.getBody();
    }
    
    /**
     * Retrieve thumbnail for a DICOM instance
     */
    public byte[] retrieveThumbnail(
            String wadoRsUrl,
            String studyInstanceUid,
            String seriesInstanceUid,
            String sopInstanceUid,
            int size) {
        
        String url = String.format(
            "%s/studies/%s/series/%s/instances/%s/thumbnail?viewport=%d,%d",
            wadoRsUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid, size, size
        );
        
        log.debug("WADO-RS thumbnail retrieval: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "image/jpeg");
        
        ResponseEntity<byte[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            byte[].class
        );
        
        return response.getBody();
    }
    
    /**
     * Retrieve metadata for a DICOM instance
     */
    public String retrieveMetadata(
            String wadoRsUrl,
            String studyInstanceUid,
            String seriesInstanceUid,
            String sopInstanceUid) {
        
        String url = String.format(
            "%s/studies/%s/series/%s/instances/%s/metadata",
            wadoRsUrl, studyInstanceUid, seriesInstanceUid, sopInstanceUid
        );
        
        log.debug("WADO-RS metadata retrieval: {}", url);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/dicom+json");
        
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        return response.getBody();
    }
}
```

---

### 3. Frontend API Services

#### 3.1 Study Service

**File: `services/studyService.ts`**
```typescript
import { api } from './api';
import type { Study, Series, Instance } from '../types/dicom';

interface StudySearchParams {
  patientName?: string;
  patientId?: string;
  studyDate?: string;
  modality?: string;
  accessionNumber?: string;
  limit?: number;
  offset?: number;
}

interface StudyListResponse {
  total: number;
  offset: number;
  limit: number;
  studies: Study[];
}

interface SeriesListResponse {
  series: Series[];
}

interface InstanceListResponse {
  instances: Instance[];
}

export const studyService = {
  /**
   * Search for studies with optional filters
   */
  async searchStudies(params: StudySearchParams = {}): Promise<StudyListResponse> {
    const response = await api.get<StudyListResponse>('/studies', { params });
    return response.data;
  },

  /**
   * Get details for a specific study
   */
  async getStudy(studyInstanceUid: string): Promise<Study> {
    const response = await api.get<Study>(`/studies/${studyInstanceUid}`);
    return response.data;
  },

  /**
   * Get all series for a study
   */
  async getSeriesForStudy(studyInstanceUid: string): Promise<SeriesListResponse> {
    const response = await api.get<SeriesListResponse>(
      `/studies/${studyInstanceUid}/series`
    );
    return response.data;
  },

  /**
   * Get details for a specific series
   */
  async getSeries(
    studyInstanceUid: string,
    seriesInstanceUid: string
  ): Promise<Series> {
    const response = await api.get<Series>(
      `/studies/${studyInstanceUid}/series/${seriesInstanceUid}`
    );
    return response.data;
  },

  /**
   * Get all instances for a series
   */
  async getInstancesForSeries(
    studyInstanceUid: string,
    seriesInstanceUid: string
  ): Promise<InstanceListResponse> {
    const response = await api.get<InstanceListResponse>(
      `/studies/${studyInstanceUid}/series/${seriesInstanceUid}/instances`
    );
    return response.data;
  },
};
```

---

## Acceptance Criteria

### Phase 1 Completion Checklist

#### Backend Requirements
- [ ] Spring Boot application starts without errors
- [ ] Health endpoint (`/api/health`) returns 200 OK with status information
- [ ] QIDO-RS proxy endpoint successfully queries configured PACS
- [ ] WADO-RS proxy endpoint successfully retrieves DICOM instances
- [ ] Study, Series, and Instance endpoints return properly formatted JSON
- [ ] Error handling returns appropriate HTTP status codes and messages
- [ ] All API endpoints documented with OpenAPI/Swagger

#### Frontend Requirements
- [ ] React application builds without errors
- [ ] Cornerstone.js initializes without console errors
- [ ] Study browser displays list of studies from backend
- [ ] Search filters work correctly (patient name, ID, date, modality)
- [ ] Clicking a study loads the viewer
- [ ] DICOM images display correctly in the viewer
- [ ] Window/Level tool adjusts image brightness/contrast
- [ ] Zoom tool magnifies/reduces image view
- [ ] Pan tool moves the image within the viewport
- [ ] Stack scroll navigates through series images
- [ ] Image navigation buttons work correctly

#### Integration Requirements
- [ ] Frontend can communicate with backend API
- [ ] CORS is properly configured
- [ ] Authentication tokens are handled (if implemented)
- [ ] Error states are properly displayed to users

#### Performance Requirements
- [ ] Single image loads in under 1 second
- [ ] Study list query completes in under 2 seconds
- [ ] Window/Level adjustment is responsive (< 50ms)
- [ ] Stack scrolling maintains 60 fps

---

## Testing Strategy

### Backend Testing

#### Unit Tests
```java
// Example: StudyServiceTest.java
@SpringBootTest
class StudyServiceTest {
    
    @Autowired
    private StudyService studyService;
    
    @MockBean
    private QidoRsClient qidoRsClient;
    
    @Test
    void searchStudies_shouldReturnStudies_whenPacsResponds() {
        // Given
        when(qidoRsClient.searchStudies(any(), any()))
            .thenReturn(List.of(createTestStudy()));
        
        // When
        List<Study> studies = studyService.searchStudies(Map.of("PatientName", "DOE*"));
        
        // Then
        assertThat(studies).hasSize(1);
        assertThat(studies.get(0).getPatientName()).contains("DOE");
    }
    
    @Test
    void searchStudies_shouldHandleEmptyResponse() {
        // Given
        when(qidoRsClient.searchStudies(any(), any()))
            .thenReturn(Collections.emptyList());
        
        // When
        List<Study> studies = studyService.searchStudies(Map.of());
        
        // Then
        assertThat(studies).isEmpty();
    }
}
```

#### Integration Tests
```java
// Example: StudyControllerIntegrationTest.java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class StudyControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void getStudies_shouldReturn200_withValidResponse() throws Exception {
        mockMvc.perform(get("/api/studies")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.studies").isArray());
    }
}
```

### Frontend Testing

#### Component Tests
```typescript
// Example: StudyBrowser.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { StudyBrowser } from '../components/StudyBrowser';

const queryClient = new QueryClient();

describe('StudyBrowser', () => {
  it('renders study list after loading', async () => {
    render(
      <QueryClientProvider client={queryClient}>
        <StudyBrowser onStudySelect={jest.fn()} />
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.queryByText(/loading/i)).not.toBeInTheDocument();
    });

    expect(screen.getByRole('table')).toBeInTheDocument();
  });

  it('calls onStudySelect when a study is clicked', async () => {
    const mockOnSelect = jest.fn();
    
    render(
      <QueryClientProvider client={queryClient}>
        <StudyBrowser onStudySelect={mockOnSelect} />
      </QueryClientProvider>
    );

    await waitFor(() => {
      const rows = screen.getAllByRole('row');
      if (rows.length > 1) {
        rows[1].click();
      }
    });

    expect(mockOnSelect).toHaveBeenCalled();
  });
});
```

---

## Deliverables Checklist

### Documentation
- [ ] Updated README with Phase 1 setup instructions
- [ ] API documentation (OpenAPI/Swagger)
- [ ] Cornerstone.js integration guide
- [ ] Development environment setup guide

### Code Deliverables
- [ ] Backend Spring Boot application
  - [ ] Health endpoint
  - [ ] Study/Series/Instance controllers
  - [ ] QIDO-RS client
  - [ ] WADO-RS client
  - [ ] Basic error handling
- [ ] Frontend React application
  - [ ] Study browser component
  - [ ] DICOM viewer component
  - [ ] Toolbar component
  - [ ] API services

### Infrastructure
- [ ] Docker Compose configuration
- [ ] Backend Dockerfile
- [ ] Frontend Dockerfile
- [ ] Test PACS (Orthanc) configuration

### Testing
- [ ] Backend unit tests (>70% coverage)
- [ ] Frontend component tests
- [ ] Integration tests
- [ ] Manual testing checklist

---

## Next Steps After Phase 1

Upon completing Phase 1, the following capabilities will be ready for Phase 2:

1. **Infrastructure** is established for adding legacy PACS support
2. **DICOM viewing** foundation enables measurement and annotation tools
3. **API patterns** established for extending to C-FIND/C-MOVE
4. **Frontend architecture** ready for additional viewer features

Continue to [Phase 2: Legacy PACS Support](./ROADMAP.md#phase-2-legacy-pacs-support-weeks-7-10) in the main roadmap.
