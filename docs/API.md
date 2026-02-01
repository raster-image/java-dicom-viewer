# API Documentation

This document describes the REST API endpoints for the Java DICOM Viewer application.

## Base URL

```
Development: http://localhost:8080/api
Production: https://your-domain.com/api
```

## Authentication

All API endpoints (except `/api/auth/*`) require authentication via JWT bearer token.

```http
Authorization: Bearer <jwt-token>
```

---

## Endpoints

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}

Response 200:
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "username": "string",
    "roles": ["ROLE_RADIOLOGIST"]
  }
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "string"
}

Response 200:
{
  "accessToken": "string",
  "expiresIn": 3600
}
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <token>

Response 204: No Content
```

---

### Studies

#### List Studies
```http
GET /api/studies
Authorization: Bearer <token>

Query Parameters:
- patientId (string): Filter by patient ID
- patientName (string): Filter by patient name (supports wildcards)
- studyDate (string): Filter by study date (YYYYMMDD or range YYYYMMDD-YYYYMMDD)
- modality (string): Filter by modality
- accessionNumber (string): Filter by accession number
- limit (integer): Maximum results (default: 50)
- offset (integer): Pagination offset (default: 0)
- pacsId (uuid): Query specific PACS (optional)

Response 200:
{
  "total": 150,
  "offset": 0,
  "limit": 50,
  "studies": [
    {
      "studyInstanceUid": "1.2.3.4.5",
      "patientId": "PAT001",
      "patientName": "DOE^JOHN",
      "patientBirthDate": "19800115",
      "patientSex": "M",
      "studyDate": "20240115",
      "studyTime": "103000",
      "studyDescription": "CT CHEST",
      "accessionNumber": "ACC123456",
      "modalitiesInStudy": ["CT"],
      "numberOfStudyRelatedSeries": 3,
      "numberOfStudyRelatedInstances": 245,
      "sourcePacs": {
        "id": "uuid",
        "name": "Main PACS"
      }
    }
  ]
}
```

#### Get Study Details
```http
GET /api/studies/{studyInstanceUid}
Authorization: Bearer <token>

Response 200:
{
  "studyInstanceUid": "1.2.3.4.5",
  "patientId": "PAT001",
  "patientName": "DOE^JOHN",
  "studyDate": "20240115",
  "studyDescription": "CT CHEST",
  "series": [
    {
      "seriesInstanceUid": "1.2.3.4.5.1",
      "seriesNumber": 1,
      "seriesDescription": "SCOUT",
      "modality": "CT",
      "numberOfSeriesRelatedInstances": 2
    }
  ]
}
```

---

### Series

#### List Series for Study
```http
GET /api/studies/{studyInstanceUid}/series
Authorization: Bearer <token>

Response 200:
{
  "series": [
    {
      "seriesInstanceUid": "1.2.3.4.5.1",
      "seriesNumber": 1,
      "seriesDescription": "SCOUT",
      "modality": "CT",
      "bodyPartExamined": "CHEST",
      "numberOfSeriesRelatedInstances": 2
    }
  ]
}
```

#### Get Series Details
```http
GET /api/studies/{studyInstanceUid}/series/{seriesInstanceUid}
Authorization: Bearer <token>

Response 200:
{
  "seriesInstanceUid": "1.2.3.4.5.1",
  "seriesNumber": 1,
  "seriesDescription": "AXIAL",
  "modality": "CT",
  "instances": [
    {
      "sopInstanceUid": "1.2.3.4.5.1.1",
      "instanceNumber": 1,
      "rows": 512,
      "columns": 512
    }
  ]
}
```

---

### Instances

#### List Instances for Series
```http
GET /api/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances
Authorization: Bearer <token>

Response 200:
{
  "instances": [
    {
      "sopInstanceUid": "1.2.3.4.5.1.1",
      "sopClassUid": "1.2.840.10008.5.1.4.1.1.2",
      "instanceNumber": 1,
      "rows": 512,
      "columns": 512
    }
  ]
}
```

---

### WADO-RS (Image Retrieval)

#### Retrieve Instance
```http
GET /api/wado/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}
Authorization: Bearer <token>
Accept: application/dicom

Response 200:
Content-Type: application/dicom
[DICOM file binary data]
```

#### Retrieve Instance Frames
```http
GET /api/wado/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/frames/{frameNumbers}
Authorization: Bearer <token>
Accept: multipart/related; type="application/octet-stream"

Response 200:
Content-Type: multipart/related; type="application/octet-stream"
[Frame pixel data]
```

#### Retrieve Rendered Image
```http
GET /api/wado/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/rendered
Authorization: Bearer <token>
Accept: image/jpeg

Query Parameters:
- window: Window width/center (e.g., "400/40")
- quality: JPEG quality 1-100 (default: 85)

Response 200:
Content-Type: image/jpeg
[Rendered image data]
```

#### Retrieve Thumbnail
```http
GET /api/wado/studies/{studyInstanceUid}/series/{seriesInstanceUid}/instances/{sopInstanceUid}/thumbnail
Authorization: Bearer <token>
Accept: image/jpeg

Query Parameters:
- size: Thumbnail size (default: 128)

Response 200:
Content-Type: image/jpeg
[Thumbnail image data]
```

---

### PACS Configuration

#### List PACS Configurations
```http
GET /api/pacs
Authorization: Bearer <token>

Response 200:
{
  "configurations": [
    {
      "id": "uuid",
      "name": "Main PACS",
      "host": "pacs.hospital.com",
      "port": 104,
      "aeTitle": "MAINPACS",
      "pacsType": "LEGACY",
      "isActive": true
    },
    {
      "id": "uuid",
      "name": "Cloud PACS",
      "host": "dicomweb.hospital.com",
      "port": 443,
      "aeTitle": "CLOUDPACS",
      "pacsType": "DICOMWEB",
      "wadoRsUrl": "https://dicomweb.hospital.com/wado-rs",
      "qidoRsUrl": "https://dicomweb.hospital.com/qido-rs",
      "stowRsUrl": "https://dicomweb.hospital.com/stow-rs",
      "isActive": true
    }
  ]
}
```

#### Create PACS Configuration
```http
POST /api/pacs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New PACS",
  "host": "pacs.hospital.com",
  "port": 104,
  "aeTitle": "NEWPACS",
  "pacsType": "LEGACY"
}

Response 201:
{
  "id": "uuid",
  "name": "New PACS",
  ...
}
```

#### Test PACS Connection (C-ECHO)
```http
POST /api/pacs/{id}/test
Authorization: Bearer <token>

Response 200:
{
  "success": true,
  "responseTime": 45,
  "message": "Connection successful"
}

Response 200 (failure):
{
  "success": false,
  "responseTime": null,
  "message": "Connection refused"
}
```

---

### Worklist

#### Get Worklist
```http
GET /api/worklist
Authorization: Bearer <token>

Query Parameters:
- date (string): Worklist date (YYYYMMDD, default: today)
- modality (string): Filter by modality
- status (string): Filter by status (NEW, IN_PROGRESS, COMPLETED)

Response 200:
{
  "items": [
    {
      "id": "uuid",
      "accessionNumber": "ACC123",
      "patientId": "PAT001",
      "patientName": "DOE^JOHN",
      "scheduledDate": "20240115",
      "scheduledTime": "1030",
      "modality": "CT",
      "procedureDescription": "CT CHEST W/O CONTRAST",
      "status": "NEW",
      "priority": "ROUTINE",
      "study": {
        "studyInstanceUid": "1.2.3.4.5"
      }
    }
  ]
}
```

#### Update Worklist Item Status
```http
PATCH /api/worklist/{id}/status
Authorization: Bearer <token>
Content-Type: application/json

{
  "status": "IN_PROGRESS"
}

Response 200:
{
  "id": "uuid",
  "status": "IN_PROGRESS"
}
```

---

### Reports

#### Create Report
```http
POST /api/reports
Authorization: Bearer <token>
Content-Type: application/json

{
  "studyInstanceUid": "1.2.3.4.5",
  "content": "FINDINGS: ...",
  "impression": "Normal study"
}

Response 201:
{
  "id": "uuid",
  "studyInstanceUid": "1.2.3.4.5",
  "status": "DRAFT",
  "content": "FINDINGS: ...",
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "dr.smith"
}
```

#### Get Report
```http
GET /api/reports/{id}
Authorization: Bearer <token>

Response 200:
{
  "id": "uuid",
  "studyInstanceUid": "1.2.3.4.5",
  "status": "DRAFT",
  "content": "FINDINGS: ...",
  "impression": "Normal study",
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "dr.smith",
  "history": [
    {
      "version": 1,
      "modifiedAt": "2024-01-15T10:30:00Z",
      "modifiedBy": "dr.smith"
    }
  ]
}
```

#### Finalize Report
```http
POST /api/reports/{id}/finalize
Authorization: Bearer <token>

Response 200:
{
  "id": "uuid",
  "status": "FINAL",
  "finalizedAt": "2024-01-15T11:00:00Z",
  "finalizedBy": "dr.smith"
}
```

---

### Audit Log

#### Get Audit Logs
```http
GET /api/audit
Authorization: Bearer <token>

Query Parameters:
- startDate (string): Start date filter (ISO 8601)
- endDate (string): End date filter (ISO 8601)
- userId (string): Filter by user
- action (string): Filter by action type
- resourceType (string): Filter by resource type
- limit (integer): Maximum results
- offset (integer): Pagination offset

Response 200:
{
  "total": 1000,
  "logs": [
    {
      "id": "uuid",
      "timestamp": "2024-01-15T10:30:00Z",
      "userId": "dr.smith",
      "action": "VIEW_STUDY",
      "resourceType": "STUDY",
      "resourceId": "1.2.3.4.5",
      "details": {
        "ipAddress": "192.168.1.100"
      }
    }
  ]
}
```

---

## Error Responses

All endpoints may return the following error responses:

### 400 Bad Request
```json
{
  "error": "BAD_REQUEST",
  "message": "Invalid parameter: studyDate",
  "details": "Date must be in format YYYYMMDD"
}
```

### 401 Unauthorized
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "error": "FORBIDDEN",
  "message": "Insufficient permissions"
}
```

### 404 Not Found
```json
{
  "error": "NOT_FOUND",
  "message": "Study not found",
  "resourceId": "1.2.3.4.5"
}
```

### 500 Internal Server Error
```json
{
  "error": "INTERNAL_ERROR",
  "message": "An unexpected error occurred",
  "traceId": "abc-123-def"
}
```

---

## Rate Limiting

API requests are rate limited per user:
- Standard endpoints: 100 requests per minute
- Image retrieval: 500 requests per minute

Rate limit headers are included in all responses:
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1705319400
```

---

## Pagination

List endpoints support pagination with `limit` and `offset` parameters.

Response includes pagination metadata:
```json
{
  "total": 150,
  "offset": 0,
  "limit": 50,
  "data": [...]
}
```
