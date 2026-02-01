document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const fileInput = document.getElementById('dicomFile');
    const fileLabel = document.querySelector('.file-label');
    const uploadStatus = document.getElementById('uploadStatus');
    const metadataSection = document.getElementById('metadataSection');
    const metadataContent = document.getElementById('metadataContent');

    // Update label when file is selected
    fileInput.addEventListener('change', function() {
        if (this.files && this.files.length > 0) {
            fileLabel.textContent = this.files[0].name;
        } else {
            fileLabel.textContent = 'Choose DICOM File';
        }
    });

    // Handle form submission
    uploadForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const file = fileInput.files[0];
        if (!file) {
            showStatus('Please select a file', 'error');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            showStatus('Uploading...', 'info');
            
            const response = await fetch('/api/dicom/upload', {
                method: 'POST',
                body: formData
            });

            const data = await response.json();

            if (data.success) {
                showStatus(data.message, 'success');
                displayMetadata(data.metadata);
            } else {
                showStatus(data.message, 'error');
                metadataSection.style.display = 'none';
            }
        } catch (error) {
            showStatus('Error uploading file: ' + error.message, 'error');
            metadataSection.style.display = 'none';
        }
    });

    function showStatus(message, type) {
        uploadStatus.textContent = message;
        uploadStatus.className = type;
        uploadStatus.style.display = 'block';
    }

    function displayMetadata(metadata) {
        metadataContent.innerHTML = '';
        
        if (!metadata || Object.keys(metadata).length === 0) {
            metadataContent.innerHTML = '<p>No metadata available</p>';
            return;
        }

        for (const [key, value] of Object.entries(metadata)) {
            const item = document.createElement('div');
            item.className = 'metadata-item';
            
            const label = document.createElement('div');
            label.className = 'metadata-label';
            label.textContent = formatLabel(key) + ':';
            
            const valueDiv = document.createElement('div');
            valueDiv.className = 'metadata-value';
            valueDiv.textContent = value || 'N/A';
            
            item.appendChild(label);
            item.appendChild(valueDiv);
            metadataContent.appendChild(item);
        }

        metadataSection.style.display = 'block';
    }

    function formatLabel(key) {
        // Convert camelCase to Title Case
        return key
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, str => str.toUpperCase());
    }
});
