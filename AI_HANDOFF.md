# AI_HANDOFF.md

# Poonel — Current Project Status

Last Updated: 2026-07-11
Brand: **Poonel** (Smart PDF Tools)
Visual Identity: **Premium Honey & Charcoal Architectural Design**

---

## Project Goal
Build the easiest and most premium PDF utility app on Android.

---

## Current Status: Phase 1 Completed + Page Editor In Progress

### Completed Features
* ✅ **PDF → Images**: Stable conversion with quality settings and folder organization.
* ✅ **Merge PDF + Page Editor**: Multi-file selection, then full page-level editing (reorder, rotate, delete, zoom preview) before merging.
* ✅ **Split PDF**: All pages or range, output as single or separate files.
* ✅ **Images → PDF**: Multi-image to PDF with page size, orientation, and quality options.
* ✅ **Design System**: Global "PoonelBackground" with architectural honeycomb network.
* ✅ **Navigation**: Unified flow with transparent screen layering.
* ✅ **Branding**: Official "Poonel" branding with Honey Gold & Charcoal palette.
* ✅ **Settings**: Live theme switching (Light/Dark) via DataStore. Logo integrated into About section.

---

## Active Branch: `feature/page-editor`

### What Was Built
A reusable Page Editor inserted between file selection and the merge pipeline.

**New files:**
- `domain/model/PageItem.kt` — core domain model for a single editable page
- `domain/model/ThumbnailState.kt` — loading lifecycle (Pending/Loading/Loaded/Error)
- `domain/repository/PageEditorRepository.kt` — interface for thumbnail rendering
- `data/repository/PageEditorRepositoryImpl.kt` — native PdfRenderer implementation
- `domain/usecase/LoadPageThumbnailsUseCase.kt` — emits loaded PageItems one by one
- `ui/components/PageThumbnailCard.kt` — reusable thumbnail card with rotate/delete controls
- `ui/components/PageEditorGrid.kt` — reusable 2-column lazy grid with drag-to-reorder
- `ui/components/ZoomPreviewDialog.kt` — full-screen pager overlay for page zoom
- `ui/screens/merge/MergePageEditorScreen.kt` — the full editor screen

**Modified files:**
- `domain/model/MergeConfig.kt` — replaced `List<Uri>` with `List<PageItem>`
- `data/repository/MergeRepositoryImpl.kt` — page-level processing + rotation matrix
- `ui/screens/merge/MergePdfUiState.kt` — added page editor phase fields
- `ui/screens/merge/MergePdfViewModel.kt` — extended with page editing operations
- `ui/screens/merge/MergePdfScreen.kt` — "Merge" button → "Arrange Pages" button
- `ui/navigation/NavGraph.kt` — added `merge_page_editor` route with shared ViewModel
- `res/values/strings.xml` + `values-fa/strings.xml` — all page editor strings

### Navigation Flow
```
MergePdfScreen (file selection)
    → "Arrange Pages" button
MergePageEditorScreen (page editor)
    → "Merge N Pages" button
MergeProgressScreen
    → MergeSuccessScreen
```

### Reusability
`PageItem`, `PageThumbnailCard`, `PageEditorGrid`, and `ZoomPreviewDialog` are all
feature-agnostic. Split PDF, Images to PDF, and future features can use them by
passing their own `List<PageItem>` and calling `PageEditorRepository` directly.

---

## Design System Details
* **PoonelBackground**: Procedural Canvas-drawn vertical technical network. Anchored to corners/edges. Honey Gold (`#E9A600`) on Charcoal (`#1C1B17`).
* **Transparency**: All `Scaffold` containers use `Color.Transparent`.
* **Glass Effect**: Top bars use `alpha = 0.85f` for a modern layered look.

---

## Architecture
* **MVVM + Clean Architecture**: Clear separation between UI, Domain (Models/UseCases), and Data (PdfRenderer/MediaStore).
* **Global Branding**: `PoonelBackground` wraps the `NavHost` in `NavGraph.kt`.
* **Local Processing**: No external SDKs; all work done via native Android APIs.
* **Shared ViewModel**: `MergePdfViewModel` is shared across `MergePdfScreen` and `MergePageEditorScreen` via back stack entry scoping.

---

## Reusable Page Editing Architecture (In Progress)

A multi-phase refactor to make Page Organizer and Page Editor fully reusable across all features.

### Phase 0 — COMPLETE (2026-07-12)
Extracted `PageOperationsDelegate` from `MergePdfViewModel`. App behavior unchanged.

New files:
- `domain/model/PageOrganizerState.kt` — pages + thumbnail progress + zoom state
- `ui/screens/pageorganizer/PageOperationsDelegate.kt` — rotate, delete, reorder, thumbnail loading, bitmap lifecycle

Changed files:
- `MergePdfViewModel.kt` — now composes `PageOperationsDelegate`; internal state split into `_fileState` (file selection + merge) + `delegate.state` (pages); combined via `combine().stateIn()` into same `MergePdfUiState` shape

### Phase 1 — COMPLETE (2026-07-12)
Generalized `MergePageEditorScreen` → `PageOrganizerScreen` (pure composable). Updated NavGraph route `merge_page_editor` → `page_organizer`.

New files:
- `ui/screens/pageorganizer/PageOrganizerScreen.kt` — pure composable, no ViewModel imports

Deleted files:
- `ui/screens/merge/MergePageEditorScreen.kt`

Changed files:
- `ui/navigation/NavGraph.kt` — `Screen.MergePageEditor` → `Screen.PageOrganizer`; NavGraph entry now resolves `MergePdfViewModel` and passes all state + lambdas to `PageOrganizerScreen`

### Phase 2 — COMPLETE (2026-07-12)
Created `PageEditorScreen` + `PageEditorViewModel` for per-page fine editing. Single tap opens editor; long press opens zoom preview.

New files:
- `domain/model/PageEditorUiState.kt` → `ui/screens/pageeditor/PageEditorUiState.kt`
- `ui/screens/pageeditor/PageEditorViewModel.kt` — plain ViewModel, `loadPage()` is idempotent
- `ui/screens/pageeditor/PageEditorScreen.kt` — pure composable: page preview, 90° rotate, fine-rotation slider

Changed files:
- `domain/model/PageItem.kt` — added `fineRotation: Float = 0f`
- `ui/screens/pageorganizer/PageOperationsDelegate.kt` — added `updatePage(PageItem)`
- `ui/screens/merge/MergePdfViewModel.kt` — added `updatePage()` pass-through
- `ui/components/PageThumbnailCard.kt` — `combinedClickable` (tap = edit, long press = zoom); rotation preview includes `fineRotation`
- `ui/components/PageEditorGrid.kt` — added `onLongClick` parameter
- `ui/screens/pageorganizer/PageOrganizerScreen.kt` — added `onLongPressPage` parameter
- `ui/navigation/NavGraph.kt` — added `page_editor/{pageIndex}` route; `onPageClick` navigates to editor; `onLongPressPage` triggers zoom
- `data/repository/MergeRepositoryImpl.kt` — applies `fineRotation` in matrix transform
- `res/values/strings.xml` + `values-fa/strings.xml` — added `single_page_editor_*` strings

### Phase 3 — COMPLETE (2026-07-12)
Standalone PDF Editor: pick one PDF, edit pages, save as new file.

New files:
- `ui/screens/pdfedit/PdfEditUiState.kt` — state + `PdfEditSaveState` sealed interface
- `ui/screens/pdfedit/PdfEditViewModel.kt` — composes `PageOperationsDelegate` + `MergePdfUseCase`
- `ui/screens/pdfedit/PdfEditScreen.kt` — single file selector
- `ui/screens/pdfedit/PdfEditProgressScreen.kt` — save progress
- `ui/screens/pdfedit/PdfEditSuccessScreen.kt` — save success

Changed files:
- `ui/components/FeatureIllustration.kt` — added `PdfOperation.PDF_EDIT`
- `ui/screens/home/HomeScreen.kt` — added `onPdfEditClick` + PDF Editor card
- `ui/navigation/NavGraph.kt` — five new routes; all screens reused via NavGraph wiring
- `res/values/strings.xml` + `values-fa/strings.xml` — `pdf_edit_*` strings

## Scanner Feature — ML Kit Document Scanner COMPLETE (2026-08-16)

### Infrastructure
Integrated Google ML Kit Document Scanner for high-quality document acquisition from both Camera and Gallery.

New dependencies:
- `com.google.android.gms:play-services-mlkit-document-scanner:16.0.0`

Changed files:
- `ScannerEntryScreen.kt` — replaced raw gallery picker with ML Kit Scanner; `setGalleryImportAllowed(true)` enables unified camera/gallery flow with document correction
- `ScannerViewModel.kt` — added `copyUriToSession` logic to persist corrected ML Kit results into `filesDir/scanner_session/`; implemented `clearSession()` in `onCleared`
- `NavGraph.kt` — updated `ScannerPageOrganizer` to use ML Kit for "Import More", allowing users to add additional corrected pages during a session
- `strings.xml` + `values-fa/strings.xml` — added `scanner_start_action`, `scanner_not_available`, and refined empty state descriptions

### Technical Implementation Details
- **ML Kit Configuration**: `SCANNER_MODE_FULL` (enables filters/enhancement), `RESULT_FORMAT_JPEG`, `pageLimit=50`.
- **Session Persistence**: ML Kit returns temporary/cached URIs. The ViewModel now copies these into app-private storage to ensure they survive until PDF generation is complete.
- **Cleanup Strategy**: The `scanner_session` directory is recursively deleted in `ScannerViewModel.onCleared()`. Since the VM is scoped to the scanner navigation entry, this ensures cleanup happens when the user leaves the feature.

---

## Known TODOs / Next Steps
1. **Language Switching**: Finalize the runtime locale override for Persian/English.
2. **Permissions**: Request `WRITE_EXTERNAL_STORAGE` on API 26-28.
3. **OCR**: Text extraction from PDF/Images — not yet planned.

---

## Development Rule
**The center of the screen must always remain clean for content focus. Background elements must stay anchored to corners and edges.**
