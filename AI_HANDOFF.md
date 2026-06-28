# AI_HANDOFF.md

# EasySmartPDF — Current Project Status

Last Updated:
2026-06-28

---

## Project Goal

Build the easiest PDF to Image application on Android.

Current priority:

Finish a complete PDF → Image workflow before adding any additional features.

---

## Completed

* Android Studio project created.
* Git repository initialized.
* Initial project structure completed.
* Material 3 Design System completed.
* Navigation implemented.
* Home Screen completed.
* Create Pictures screen completed.
* PDF picker implemented.
* PDF metadata reading implemented.
* Page count detection implemented.
* Picture Size selection implemented.
* Page Selection UI implemented.
* Range sliders implemented.
* Numeric page inputs implemented (widened to 96dp to fit 4-digit page numbers).
* Unsaved changes confirmation dialog implemented.
* Domain layer implemented: PdfInfo, ImageFormat, ImageQuality, RenderedPage, ConversionConfig, ConversionEvent, PdfRepository (interface), ImageRepository (interface).
* UseCase layer implemented: GetPdfInfoUseCase, ConvertPdfToImagesUseCase.
* Data layer implemented: PdfRepositoryImpl (PdfRenderer, IO dispatcher, white background, page-by-page rendering, proper cleanup), ImageRepositoryImpl (MediaStore, IS_PENDING on API 29+, scoped storage).
* CreatePicturesViewModel implemented (AndroidViewModel, StateFlow, all user events).
* CreatePicturesUiState implemented (PageMode, ConversionState sealed interface).
* CreatePicturesScreen wired to ViewModel. All business logic removed from Composable.
* ConversionEvent pipeline: Started → Progress → PageSaved → Completed → Failed.
* Progress Screen implemented: "Page X of Y", "Saving image X…", LinearProgressIndicator updating after each saved page, Cancel button with confirmation dialog.
* Conversion cancellation implemented: cooperative coroutine cancellation via Job.cancel(), already-saved images preserved, returns to CreatePictures with settings intact.
* Per-conversion folder structure: Pictures/EasySmartPDF/<PdfName>_<Quality>_<yyyyMMdd_HHmm>/page_001.jpg (API 29+). Invalid filename chars stripped. 3-digit zero-padded page numbers.
* Success Screen implemented: shows total pictures saved, folder name, "Open Folder" button (opens DocumentsUI, fallback to gallery), "Back Home" button.
* Navigation flow complete: Home → CreatePictures → Progress → Success → Home.
* Success nav args: savedCount (Int) and folderName (String) passed via nav route.

---

## Current Architecture

```
UI (Composable)
↓
ViewModel (AndroidViewModel + StateFlow)
↓
UseCases (domain/usecase/)   ← should be in domain/usecase/pdftoimage/ but not yet moved
↓
Repository Interfaces (domain/repository/)
↓
Repository Implementations (data/repository/)
↓
PdfRenderer / MediaStore
```

Note: Hilt is listed in PROJECT_RULES.md but not yet added.
AndroidViewModel with manual wiring is the current DI approach.

---

## Key Implementation Details

### Folder naming
Generated in `CreatePicturesViewModel.buildFolderName()`:
`<sanitized_pdf_name>_<Quality>_<yyyyMMdd_HHmm>` (e.g., `MyDocument_Best_20260628_1430`).
Chars `/ \ : * ? " < > |` replaced with `_`. Name capped at 40 chars before suffix.
On API 26-28: RELATIVE_PATH is not supported; images saved without subfolder (known limitation).

### Cancellation flow
`cancelConversion()` in ViewModel: cancels the coroutine Job, sets `conversionState = Cancelled`.
ProgressScreen detects `Cancelled` via `LaunchedEffect(state)` → calls `onConversionCancelled()` → navController pops back to CreatePictures.
`isConverting` in CreatePicturesScreen excludes `Cancelled` state so the button re-enables after cancel.
`startConversion()` guard also accepts `Cancelled` state to allow restarting.

### Open Folder
NavGraph.kt `openFolder()` private function: on API 29+, tries DocumentsUI with
`com.android.externalstorage.documents` authority and `primary:Pictures/EasySmartPDF/<folderName>` doc ID.
Falls back to opening the system gallery if ActivityNotFoundException.

---

## Open Decisions — Must Resolve Before Next Step

1. Should data/source/pdf/ and data/source/storage/ be used as a separate DataSource layer
   below the repository implementations, or removed?
   Current state: directories exist but are empty.

2. Move GetPdfInfoUseCase and ConvertPdfToImagesUseCase to domain/usecase/pdftoimage/
   to match the pre-existing scaffold. Not yet done.

3. Runtime permission for WRITE_EXTERNAL_STORAGE on API 26-28 is NOT yet requested.
   The manifest declares it but the ViewModel never requests it at runtime.
   Conversion will fail silently on API 26-28 without this.
   Must be resolved before the first real end-to-end test.

4. On API 26-28, images are saved without subfolder (RELATIVE_PATH not available).
   The folder name shown on the Success Screen will be technically inaccurate on those devices.
   Decide: implement File-based fallback for API 26-28, or document as limitation.

---

## Not Started

* Runtime permission request (WRITE_EXTERNAL_STORAGE, API 26-28) — conversion will fail silently on API 26-28 without this
* Share images from Success Screen
* Hilt dependency injection
* OCR
* Merge PDF
* Split PDF
* Scanner

---

## Frozen

Do not redesign:

* Home Screen
* Navigation
* Design System
* UX Flow

unless explicitly requested.

---

## AI Rules

Always read before making changes:

1. PROJECT_RULES.md
2. CLAUDE.md or GEMINI.md
3. DECISIONS.md
4. AI_HANDOFF.md

Never continue from memory.

Always continue from this file.

---

## Next Expected Step

Resolve the open decisions above (data source layer, use case location, Hilt, API 26-28 subfolder).

Then implement runtime WRITE_EXTERNAL_STORAGE permission request for API 26-28.

Stop after implementation. Wait for review.
