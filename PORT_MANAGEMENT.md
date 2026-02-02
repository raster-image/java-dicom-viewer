# Port Management Scripts

## Kill Ports Script

The `kill-ports.sh` script automatically kills all processes using ports required by the DICOM Viewer application.

### Ports Managed:
- **8080** - Backend (Spring Boot)
- **3000** - Frontend (Vite default)
- **5173** - Frontend (Vite alternative)

### Usage:

```bash
# Make the script executable (first time only)
chmod +x kill-ports.sh

# Run the script
./kill-ports.sh
```

### What it does:

1. ✅ Checks and kills processes on port 8080 (Backend)
2. ✅ Checks and kills processes on port 3000 (Frontend - Vite)
3. ✅ Checks and kills processes on port 5173 (Frontend - Vite alternative)
4. ✅ Kills any running DicomViewerApplication Java processes
5. ✅ Kills any running Vite processes
6. ✅ Shows final port status

### Alternative Manual Commands:

If you need to manually kill a specific port:

```bash
# Kill process on port 8080 (Backend)
lsof -ti:8080 | xargs kill -9

# Kill process on port 3000 (Frontend)
lsof -ti:3000 | xargs kill -9

# Kill process on port 5173 (Frontend alternative)
lsof -ti:5173 | xargs kill -9

# Kill all Java processes for DICOM Viewer
pkill -f DicomViewerApplication

# Kill all Vite processes
pkill -f vite
```

### Troubleshooting:

If ports are still in use after running the script:
1. Run the script again
2. Check for processes manually: `lsof -i :8080` or `lsof -i :3000`
3. Reboot your machine if processes are stuck

### Quick Start/Stop Workflow:

```bash
# Stop all services
./kill-ports.sh

# Start backend
cd backend
./gradlew bootRun

# In another terminal, start frontend
cd frontend
npm run dev
```

## Frontend Start Script

The `frontend/start-frontend.sh` script safely starts the frontend by checking and killing any processes using the required ports before starting Vite.

### Usage:

```bash
# Navigate to frontend directory
cd frontend

# Make the script executable (first time only)
chmod +x start-frontend.sh

# Run the script
./start-frontend.sh
```

### What it does:

1. ✅ Checks for existing Vite processes and kills them
2. ✅ Checks and frees port 5173 (Vite default)
3. ✅ Checks and frees port 3000 (Vite alternative)
4. ✅ Verifies you're in the correct directory
5. ✅ Installs npm dependencies if node_modules is missing
6. ✅ Starts the Vite dev server with `npm run dev`

### Alternative Quick Start:

```bash
# From the project root - start frontend
cd frontend && ./start-frontend.sh

# Or from frontend directory
./start-frontend.sh
```

