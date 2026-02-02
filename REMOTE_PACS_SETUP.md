# Remote PACS Configuration

This document describes the configuration for connecting to a remote PACS server without running a local DICOM server.

## Configuration Overview

The application is configured to connect to the **TEAMPACS** remote PACS server at `117.247.185.219:11112` without starting a local DICOM Application Entity (AE) server.

## Application Settings

### Backend Configuration (`application.properties`)

```properties
# Local AE Configuration (disabled - using remote PACS only)
dicom.ae.title=DICOM_VIEWER
dicom.ae.port=11113
dicom.ae.enabled=false

# Default PACS Server Configuration
pacs.default.calledAETitle=TEAMPACS
pacs.default.callingAETitle=MAYAM
pacs.default.host=117.247.185.219
pacs.default.port=11112
pacs.default.isNetworkEnabled=true
```

### Key Settings:

- **`dicom.ae.enabled=false`**: Disables the local DICOM server (no port binding)
- **`dicom.ae.port=11113`**: Port is set but not used since server is disabled
- **Called AE Title**: `TEAMPACS` (remote PACS server)
- **Calling AE Title**: `MAYAM` (this application identifies as MAYAM to the remote PACS)
- **Remote PACS Host**: `117.247.185.219:11112`

## Database Configuration

The default PACS configuration is automatically loaded via Flyway migrations:

- **V2__Insert_default_pacs.sql**: Adds TEAMPACS to `application_entities` table
- **V3__Insert_default_pacs_configuration.sql**: Adds TEAMPACS to `pacs_configuration` table

## How It Works

1. **No Local DICOM Server**: With `dicom.ae.enabled=false`, the DicomConfig class creates a minimal DICOM device without binding to any port. This means:
   - No port 11113 (or any port) is listening for incoming DICOM connections
   - The device can still make outgoing connections to remote PACS servers
   - C-ECHO, C-FIND, and C-MOVE operations work as SCU (Service Class User) only

2. **Remote PACS Connection**: The application connects to TEAMPACS as:
   - **Calling AE Title**: `MAYAM` (what this application calls itself)
   - **Called AE Title**: `TEAMPACS` (the remote PACS server)
   - All queries and retrievals are performed against the remote PACS

3. **Frontend Integration**: The frontend (`http://localhost:3000`) will:
   - List TEAMPACS in the Study Browser PACS dropdown
   - Send C-FIND queries to TEAMPACS to search for studies
   - Request C-MOVE operations to retrieve images from TEAMPACS

## Running the Application

### Prerequisites
- Java 17+ installed
- Node.js 18+ installed

### Start Backend
```bash
cd backend
./gradlew bootRun
```

Backend will start on port **8080** without binding any DICOM listening ports.

### Start Frontend
```bash
cd frontend
npm install  # First time only
npm run dev
```

Frontend will start on port **3000**.

### Access Points
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console

## Testing Remote PACS Connection

### Via API
Test the connection to TEAMPACS using the REST API:

```bash
# Get all configured AEs
curl http://localhost:8080/api/ae

# Test connection to AE (replace {id} with the actual ID from above)
curl -X POST http://localhost:8080/api/ae/{id}/test
```

### Via Frontend
1. Navigate to **Study Browser**
2. Select **TEAMPACS** from the PACS dropdown
3. Enter search criteria (Patient ID, Name, etc.)
4. Click **Search** to query the remote PACS

## Troubleshooting

### Port Conflicts
If you see "Address already in use" errors:
```bash
# Check what's using ports 8080 or 3000
lsof -i :8080
lsof -i :3000

# Kill processes if needed
lsof -ti:8080 | xargs kill -9
lsof -ti:3000 | xargs kill -9
```

### Remote PACS Not Responding
- Verify network connectivity to `117.247.185.219:11112`
- Check if the TEAMPACS server allows connections from your IP
- Verify the AE titles are correct (Calling: MAYAM, Called: TEAMPACS)

### Enable Local DICOM Server (If Needed)
If you need to receive C-STORE requests from the remote PACS:

1. Edit `application.properties`:
   ```properties
   dicom.ae.enabled=true
   dicom.ae.port=11113
   ```

2. Ensure port 11113 is not in use
3. Restart the backend

## Architecture Notes

- **Client-only Mode**: With `dicom.ae.enabled=false`, this application acts purely as a DICOM client (SCU)
- **No C-STORE Reception**: Cannot receive images pushed from PACS via C-STORE
- **Query/Retrieve Only**: Supports C-FIND (query) and C-MOVE (retrieve) operations
- **Image Storage**: Retrieved images are stored locally in `${user.home}/dicom-storage`

## Security Considerations

- DICOM connections are **unencrypted** by default
- No authentication is configured (relies on AE title matching)
- For production use, consider:
  - Enabling TLS for DICOM connections
  - Implementing IP whitelisting
  - Using VPN for remote PACS access
  - Changing default JWT secret key

