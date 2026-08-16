# Poonel — Roadmap

---

## Completed

| Feature | Notes |
|---------|-------|
| PDF → Images | Quality presets, page range, saves to Pictures/Poonel |
| Merge PDF | Multi-file, page-level reorder/rotate/delete, fine rotation |
| Split PDF | Page range, single or separate PDFs |
| Images → PDF | Page size, orientation, margins, quality, fit mode |
| Edit PDF | Single-file page editor — reorder, rotate, delete, save |
| Design System | Honey Gold & Charcoal, geometric background, glass UI |
| Settings | Live theme + language switching (English / Farsi) |
| Reusable Page Architecture | PageOperationsDelegate + PageOrganizerScreen + PageEditorScreen |

---

## Next Up

### Compress PDF
Reduce file size by re-rendering pages at a lower resolution.
- Input: single PDF
- Output: compressed PDF saved to Documents/Poonel
- Quality slider: Low / Medium / High

### Scanner
Capture document photos and convert to a clean PDF.
- Camera integration with Android CameraX
- Auto-crop and perspective correction
- Page organizer reused for arranging scanned pages

### OCR (Text Recognition)
Extract readable text from scanned PDFs or images.
- Uses Android ML Kit or on-device Tesseract
- Output: selectable text layer or plain text file

---

## Future Consideration

- Password Protection — encrypt/decrypt PDFs
- Sign PDF — simple electronic signature
- Fill PDF Forms — basic form field detection and filling

---

## Architecture Principle for New Features

Every new tool should:
1. Pick up a single PDF or image set on a selector screen.
2. Pass pages through `PageOperationsDelegate` + `PageOrganizerScreen` for editing.
3. Run the pipeline via a UseCase on `Dispatchers.IO`.
4. Show progress and success screens.

Reuse existing components — do not duplicate page management logic.
