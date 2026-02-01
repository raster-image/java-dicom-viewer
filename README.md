# Java DICOM Viewer

A comprehensive medical imaging platform built with modern technologies for viewing, managing, and processing DICOM images. This application supports both modern DICOMweb standards and legacy PACS systems.

## Tech Stack

### Backend
- **Java 25** - Latest LTS version with modern language features
- **Spring Boot 4.x** - Enterprise-grade application framework
- **dcm4che 5.x** - DICOM toolkit for network operations and file handling

### Frontend
- **React 18+** with **TypeScript** - Modern, type-safe UI development
- **Cornerstone.js** - Industry-standard DICOM image rendering library
- **Cornerstone WADO Image Loader** - DICOM image loading support

### Protocols
- **DICOMweb** (WADO-RS, QIDO-RS, STOW-RS) - Modern REST-based DICOM services
- **Traditional DICOM** (C-FIND, C-MOVE, C-STORE, C-ECHO) - Legacy PACS compatibility

## Features

### Core Capabilities
- 🖼️ **DICOM Image Viewing** - View medical images with professional-grade tools
- 🔍 **Study/Series/Instance Navigation** - Hierarchical browsing of DICOM data
- 📏 **Measurement Tools** - Distance, angle, ROI measurements
- 🎨 **Window/Level Adjustment** - Optimize image contrast and brightness
- 🔄 **Multi-format Support** - CT, MR, US, XR, and more

### PACS Integration
- 🌐 **DICOMweb Support** - Connect to modern PACS systems
- 🔌 **Legacy PACS Support** - Connect to older systems via C-FIND/C-MOVE
- 📤 **DICOM Send** - Forward studies to other PACS systems
- 📥 **Local Storage** - Store received images locally

### Practical Application Features
- 📋 **Worklist Management** - View and manage study worklists
- 👤 **Patient Demographics** - View patient information
- 📊 **Study Reporting** - Basic reporting capabilities
- 🔐 **User Authentication** - Secure access control
- 📝 **Audit Logging** - Track user actions for compliance

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
│   └── pom.xml
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
./mvnw spring-boot:run

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
- [Implementation Roadmap](docs/ROADMAP.md) - Phased development plan
- [API Documentation](docs/API.md) - REST API reference

## License

MIT License - See [LICENSE](LICENSE) for details

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting pull requests.