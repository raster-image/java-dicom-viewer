# Java DICOM Viewer

A Spring Boot application for viewing and analyzing DICOM (Digital Imaging and Communications in Medicine) medical images.

## Features

- Upload DICOM files (.dcm)
- Extract and display DICOM metadata
- RESTful API for DICOM file processing
- Modern web interface
- Support for common DICOM tags (Patient Name, Study Date, Modality, etc.)

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Technologies Used

- **Spring Boot 3.2.1** - Application framework
- **DCM4CHE 5.31.0** - DICOM library for reading and processing DICOM files
- **Thymeleaf** - Server-side template engine
- **Lombok** - Code generation library
- **Maven** - Build and dependency management

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/raster-image/java-dicom-viewer.git
cd java-dicom-viewer
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

Alternatively, you can run the JAR file:

```bash
java -jar target/java-dicom-viewer-1.0.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## Usage

1. Open your browser and navigate to `http://localhost:8080`
2. Click "Choose DICOM File" to select a .dcm file from your computer
3. Click "Upload" to upload and process the file
4. View the extracted DICOM metadata displayed on the page

## API Endpoints

### Health Check
```
GET /api/dicom/health
```

Response:
```json
{
  "status": "UP",
  "service": "DICOM Viewer"
}
```

### Upload DICOM File
```
POST /api/dicom/upload
```

Parameters:
- `file` (multipart/form-data) - DICOM file to upload

Response:
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "metadata": {
    "patientName": "John Doe",
    "patientID": "12345",
    "studyDate": "20240101",
    "modality": "CT",
    "studyDescription": "Chest CT",
    "seriesDescription": "Axial",
    "manufacturer": "GE Medical Systems",
    "imageSize": "512x512"
  }
}
```

## Configuration

The application can be configured via `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# File upload settings
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# DICOM viewer settings
dicom.upload.directory=./uploads
dicom.temp.directory=./temp
```

## Project Structure

```
java-dicom-viewer/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rasterimage/dicomviewer/
│   │   │       ├── DicomViewerApplication.java
│   │   │       ├── config/
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── DicomController.java
│   │   │       │   └── HomeController.java
│   │   │       └── service/
│   │   │           └── DicomService.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── style.css
│   │       │   └── js/
│   │       │       └── app.js
│   │       ├── templates/
│   │       │   └── index.html
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/rasterimage/dicomviewer/
│               ├── DicomViewerApplicationTests.java
│               └── controller/
│                   └── HomeControllerTest.java
├── pom.xml
└── README.md
```

## Running Tests

```bash
mvn test
```

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.