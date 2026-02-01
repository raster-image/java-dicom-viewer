# Phase 3 User Guide: Measurements, Annotations & Key Images

This guide describes how to use the Phase 3 features of the Java DICOM Viewer, including measurement tools, annotation tools, and key image marking.

## Table of Contents

1. [Measurement Tools](#measurement-tools)
2. [Annotation Tools](#annotation-tools)
3. [Key Image Marking](#key-image-marking)
4. [Keyboard Shortcuts](#keyboard-shortcuts)
5. [Tips and Best Practices](#tips-and-best-practices)

---

## Measurement Tools

The viewer provides several measurement tools for clinical use. All measurements are automatically saved to the backend for persistence.

### Length Tool (📏)

**Purpose**: Measure distance between two points in millimeters.

**How to use**:
1. Select the Length tool from the toolbar or press `L`
2. Click on the image to place the first point
3. Click again to place the second point
4. The distance will be displayed in millimeters (mm)

**Accuracy**: Measurement accuracy depends on the quality of DICOM pixel spacing metadata and image resolution. Typically ±1mm when proper calibration data is available in the DICOM headers.

### Angle Tool (∠)

**Purpose**: Measure the angle between three points in degrees.

**How to use**:
1. Select the Angle tool from the toolbar or press `A`
2. Click to place the vertex point (corner of the angle)
3. Click to place the first arm endpoint
4. Click to place the second arm endpoint
5. The angle will be displayed in degrees (°)

### Cobb's Angle Tool (⌒)

**Purpose**: Measure spinal curvature angle (commonly used in scoliosis evaluation).

**How to use**:
1. Select the Cobb's Angle tool from the toolbar or press `C`
2. Click and drag to draw the first line along the superior endplate
3. Click and drag to draw the second line along the inferior endplate
4. The Cobb's angle will be calculated and displayed

### Rectangle ROI (▭)

**Purpose**: Draw a rectangular region of interest and view pixel statistics.

**How to use**:
1. Select the Rectangle ROI tool from the toolbar or press `R`
2. Click and drag to draw a rectangle over the area of interest
3. Statistics displayed include:
   - Mean (average pixel value)
   - Standard Deviation
   - Minimum and Maximum values
   - Area in mm²
   - For CT images: Hounsfield Units (HU)

### Elliptical ROI (⬭)

**Purpose**: Draw an elliptical region of interest and view pixel statistics.

**How to use**:
1. Select the Elliptical ROI tool from the toolbar or press `E`
2. Click and drag to draw an ellipse over the area of interest
3. Statistics displayed include:
   - Mean, Standard Deviation, Min/Max values
   - Area in mm²
   - For CT images: HU values

### Probe Tool (✚)

**Purpose**: View the pixel value at a specific point.

**How to use**:
1. Select the Probe tool from the toolbar or press `B`
2. Click on any point in the image
3. The pixel value will be displayed
   - For CT images: HU value
   - For other modalities: raw pixel value

---

## Annotation Tools

Annotation tools allow you to add labels and markers to images for documentation purposes.

### Arrow Annotation (➤)

**Purpose**: Point to a specific finding with an arrow and optional text label.

**How to use**:
1. Select the Arrow tool from the toolbar or press `J`
2. Click on the target point (arrow head)
3. Drag to position the arrow tail
4. A text input may appear to add a label

### Text Marker (T)

**Purpose**: Add a text note to a specific location on the image.

**How to use**:
1. Select the Text Marker tool from the toolbar or press `T`
2. Click on the image where you want to place the text
3. Enter your text in the dialog that appears

---

## Key Image Marking

Key images are significant images in a study that you want to flag for attention or reference.

### Marking a Key Image

1. Navigate to the image you want to mark
2. Click the **⭐ Key** button in the toolbar
3. The image is now marked as a key image
   - A "⭐ Key Image" indicator appears in the top-right corner
   - The button becomes highlighted

### Unmarking a Key Image

1. Navigate to a marked key image
2. Click the **⭐ Key** button again
3. The key image marker is removed

### Viewing Key Images

Key images appear in the sidebar panel under "⭐ Key Images" with a count of total key images. Click on any key image in the list to navigate directly to it.

### Key Image Persistence

- Key images are saved to the backend automatically
- Key image status persists across sessions
- Other users can see your key images (based on permissions)

---

## Keyboard Shortcuts

| Shortcut | Tool/Action |
|----------|-------------|
| `W` | Window/Level |
| `P` | Pan |
| `Z` | Zoom |
| `S` | Stack Scroll |
| `L` | Length measurement |
| `A` | Angle measurement |
| `C` | Cobb's Angle |
| `R` | Rectangle ROI |
| `E` | Elliptical ROI |
| `B` | Probe (pixel value) |
| `J` | Arrow annotation |
| `T` | Text marker |

---

## Tips and Best Practices

### For Accurate Measurements

1. **Ensure proper calibration**: Measurements depend on DICOM pixel spacing metadata. Verify the source images have correct calibration data.

2. **Use appropriate zoom**: For small structures, zoom in before measuring to improve precision.

3. **Reference lines**: When measuring angles, use anatomical landmarks consistently.

4. **Document measurements**: Add labels to measurements to clarify what was measured.

### For Key Images

1. **Be selective**: Mark only truly significant images to maintain clinical value.

2. **Add descriptions**: Use the description field to explain why an image is marked as key.

3. **Use categories**: Organize key images using categories like "findings", "comparison", or "critical".

### For Annotations

1. **Keep text concise**: Use short, clear labels for arrows and text markers.

2. **Position carefully**: Place arrows and text where they don't obscure important anatomy.

3. **Lock important annotations**: Use the lock feature to prevent accidental modifications.

### Workflow Tips

1. **Review before finalizing**: Check all measurements and annotations before completing your review.

2. **Toggle visibility**: Use the 👁 button to hide/show all annotations when you need a clear view.

3. **Use presets**: Apply Window/Level presets (CT Lung, CT Bone, etc.) appropriate to the study type.

---

## Data Persistence

All measurements, annotations, and key images are automatically saved to the backend:

- **Measurements**: Saved when the measurement is completed (after placing all points)
- **Annotations**: Saved when the annotation is completed
- **Key Images**: Saved immediately when toggled

Your data is associated with your user account and will be available in future sessions.

---

## Troubleshooting

### Measurements not saving

1. Check your network connection
2. Verify you have permission to create measurements
3. Check the browser console for error messages

### Annotations not appearing

1. Ensure annotations are visible (👁 button should be highlighted)
2. Try toggling annotation visibility off and on
3. Refresh the page if needed

### Key image status not persisting

1. Wait a moment for the save to complete
2. Check network connectivity
3. Verify you have write permissions for the study

---

## Need Help?

If you encounter issues not covered in this guide:

1. Check the system documentation
2. Contact your system administrator
3. Report bugs via the issue tracker
