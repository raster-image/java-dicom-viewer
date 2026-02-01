# Java DICOM Viewer

A comprehensive medical imaging platform built with modern technologies for viewing, managing, and processing DICOM images. This application supports both modern DICOMweb standards and legacy PACS systems.

## Tech Stack

### Backend
- **Java 25** - Latest LTS version with modern language features
- **Spring Boot 4.x** - Enterprise-grade application framework
- **dcm4che 5.x** - DICOM toolkit for network operations and file handling

### Frontend
- **React 18+** with **TypeScript** - Modern, type-safe UI development
- **Cornerstone.js / Cornerstone3D** - Industry-standard DICOM image rendering
- **VTK.js** - 3D visualization and volume rendering

### Protocols
- **DICOMweb** (WADO-RS, QIDO-RS, STOW-RS) - Modern REST-based DICOM services
- **Traditional DICOM** (C-FIND, C-MOVE, C-STORE, C-ECHO, C-GET) - Legacy PACS compatibility

## Features

### Core 2D Viewer Capabilities
- 🖼️ **DICOM Image Viewing** - View medical images with professional-grade tools
- 🔍 **Study/Series/Instance Navigation** - Hierarchical browsing of DICOM data
- 📏 **Measurement Tools** - Distance, angle, area, ROI measurements
- 🎨 **Window/Level Adjustment** - Optimize image contrast and brightness
- 🔄 **Multi-format Support** - CT, MR, US, XR, CR, NM, and more
- ✏️ **Annotation Tools** - Text, arrows, shapes, markers
- 🎬 **Cine Loop Playback** - Dynamic series and cardiac phase playback
- 🔗 **Series Synchronization** - Linked scrolling and reference lines

### 3D Reconstruction
- 🧊 **Multi-Planar Reconstruction (MPR)** - Axial, sagittal, coronal, oblique views
- 🌀 **Volume Rendering** - True 3D visualization with transfer functions
- 📊 **MIP/MinIP** - Maximum and minimum intensity projections
- 📐 **Curved MPR** - Vessel centerline reformats

### Clinical Modules
- ❤️ **Cardiac Imaging** - Coronary visualization, phase selection
- 🧠 **Neuro Imaging** - Brain analysis, perfusion, diffusion
- 🦴 **Orthopedic Tools** - Bone reconstruction, fracture analysis
- 🩸 **Vascular Analysis** - Vessel segmentation, stenosis measurement

### PACS Integration
- 🌐 **DICOMweb Support** - Connect to modern PACS systems
- 🔌 **Legacy PACS Support** - Connect to older systems via C-FIND/C-MOVE
- 📤 **DICOM Send** - Forward studies to other PACS systems
- 📥 **Local Storage** - Store received images locally

### Workflow & Reporting
- 📋 **Worklist Management** - View and manage study worklists
- 📝 **Structured Reporting** - DICOM SR with auto-measurement population
- 👤 **Patient Demographics** - View patient information
- 🔐 **User Authentication** - Secure role-based access control
- 📊 **Audit Logging** - Track user actions for compliance

### AI Integration
- 🤖 **AI Plugin Framework** - Extensible API for AI model integration
- 🎯 **Automated Segmentation** - AI-assisted organ and lesion segmentation
- 📈 **Lesion Tracking** - Growth trend analysis across studies

## Project Structure

```
java-dicom-viewer/
├── backend/                    # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/dicomviewer/
│   │   │   │       ├── config/         # Configuration classes
│   │   │   │       ├── controller/     # REST API controllers
│   │   │   │       ├── service/        # Business logic
│   │   │   │       ├── repository/     # Data access
│   │   │   │       ├── model/          # Domain models
│   │   │   │       ├── dicom/          # DICOM-specific code
│   │   │   │       │   ├── network/    # C-FIND, C-MOVE, C-STORE
│   │   │   │       │   └── web/        # DICOMweb services
│   │   │   │       └── security/       # Authentication/Authorization
│   │   │   └── resources/
│   │   └── test/
│   ├── build.gradle
│   └── settings.gradle
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── services/           # API services
│   │   ├── hooks/              # Custom hooks
│   │   ├── types/              # TypeScript types
│   │   └── utils/              # Utility functions
│   ├── package.json
│   └── tsconfig.json
├── docker/                     # Docker configurations
├── docs/                       # Documentation
│   ├── ARCHITECTURE.md
│   ├── ROADMAP.md
│   └── API.md
└── README.md
```

## Quick Start

### Prerequisites
- Java 25+
- Node.js 20+
- Docker & Docker Compose (optional)

### Development Setup

```bash
# Clone the repository
git clone https://github.com/raster-image/java-dicom-viewer.git
cd java-dicom-viewer

# Backend setup
cd backend
./gradlew bootRun

# Frontend setup (in another terminal)
cd frontend
npm install
npm run dev
```

### Docker Setup

```bash
docker-compose up -d
```

## Documentation

- [Architecture Overview](docs/ARCHITECTURE.md) - Technical architecture and design decisions
- [Implementation Roadmap](docs/ROADMAP.md) - Phased development plan (7 phases)
- [Phase 1 Implementation Guide](docs/PHASE1_IMPLEMENTATION.md) - Detailed Phase 1 specs with code examples
- [Phase 2 Implementation Guide](docs/PHASE2_IMPLEMENTATION.md) - Detailed Phase 2 specs with code examples (Legacy PACS Support)
- [Phase 3 Implementation Guide](docs/PHASE3_IMPLEMENTATION.md) - Detailed Phase 3 specs with code examples (Measurements, Annotations, Key Images)
- [Phase 3 User Guide](docs/USER_GUIDE_PHASE3.md) - User guide for Phase 3 features
- [Module Specifications](docs/MODULE_SPECIFICATIONS.md) - Detailed viewer module specifications
- [API Documentation](docs/API.md) - REST API reference

## Implementation Phases

| Phase | Duration | Focus | Status |
|-------|----------|-------|--------|
| 1 | 4-6 weeks | Foundation - Basic viewer, DICOMweb | ✅ Complete |
| 2 | 3-4 weeks | Legacy PACS - C-FIND, C-MOVE, C-STORE | ✅ Complete |
| 3 | 4-6 weeks | Core Viewer - Measurements, annotations, MPR | 🔄 In Progress |
| 4 | 4-6 weeks | Advanced 2D - Image processing, workflow | 📋 Planned |
| 5 | 6-8 weeks | 3D Reconstruction - Volume rendering, MIP | 📋 Planned |
| 6 | 8-10 weeks | Clinical Modules - Cardiac, neuro, vascular | 📋 Planned |
| 7 | 6-8 weeks | AI Integration - Plugin framework, premium features | 📋 Planned |

## License

MIT License - See [LICENSE](LICENSE) for details

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.