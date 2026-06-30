# AI_HANDOFF.md

# EasySmartPDF — Current Project Status

Last Updated:
2026-06-29 (output mode added)

---

## Project Goal

Build the easiest PDF utility app on Android.

Current priority:

Split PDF Phase 1 implemented and building clean. Ready for device testing.

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
* Merge PDF Phase 1 implemented: add PDFs, reorderable list (drag-to-reorder via long press on handle), remove individual files, merge disabled until ≥2 PDFs selected.
* Split PDF Phase 1 implemented: select one PDF, show filename/page count/file size, choose All Pages or Page Range (same range UI as CreatePictures), split each selected page into a separate PDF, save to Documents/EasySmartPDF/Split/<PdfName>_<yyyyMMdd_HHmm>/ with filenames page_001.pdf etc., progress screen ("Splitting page X of Y"), cancel support, success screen with file count, folder name, Open Folder, and Back Home.
* Split PDF output mode: "Output" card appears when more than one page is selected. Default = Single PDF (all selected pages merged into one output PDF). Separate PDFs = one file per page (original behavior). `SplitConfig.separatePdfs: Boolean` routes the repository to the correct branch.
* Merge pipeline: MergeConfig → MergePdfUseCase → MergeRepositoryImpl → PdfRenderer per page → PdfDocument output → MediaStore save.
* Merged output: Documents/EasySmartPDF/Merged_yyyy-MM-dd_HH-mm.pdf — MediaStore.Files requires Documents or Download as root (Pictures caused IllegalArgumentException on API 29+).
* Merge navigation: Home → MergePdf → MergeProgress → MergeSuccess → Home. Shared ViewModel pattern mirrors PDF-to-image flow.
* MergeSuccessScreen shows output filename, "Open File" button (tries DocumentsUI, falls back to PDF viewer intent), "Back Home" button.
* HomeScreen: Merge PDF card is now active (isAvailable = true) and navigates to MergePdfScreen.
* HomeScreen: Split PDF card is now active (isAvailable = true) and navigates to SplitPdfScreen.

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

## Current Feature

Split PDF Phase 1 — implemented and build is clean.

### Bugs fixed in MergeRepositoryImpl (2026-06-28)
1. **Wrong RELATIVE_PATH**: `MediaStore.Files` only accepts `Documents` or `Download` as root.
   Was: `Pictures/EasySmartPDF/Merged` → Fixed: `Documents/EasySmartPDF`.
2. **ParcelFileDescriptor double-close**: removed outer `pfd.use` — PdfRenderer owns the pfd lifecycle.
3. **Silent exceptions**: `.catch` now emits the actual exception class + message.
4. **Logging**: `Log.d(TAG, ...)` added at every major step including final output URI.

### Drag-and-drop fixed in MergePdfScreen (2026-06-28)
Root cause 1: `detectDragGesturesAfterLongPress` lost the gesture to the parent `verticalScroll`
during the 400 ms long-press wait — the parent claimed vertical movement before the long press fired.
Root cause 2: after each swap, `currentList` (backed by `rememberUpdatedState`) still returned
the pre-swap order for the rest of the current frame (recomposition is one frame async). So
`indexOfFirst` found the item at its old index and swapped the wrong entry on rapid multi-step drags.

Fix (MergePdfScreen.kt — `ReorderablePdfList`):
- Replaced `detectDragGesturesAfterLongPress` with `detectDragGestures`. The drag handle is
  an explicit drag target, so no long press is needed. `detectDragGestures` claims the gesture
  at slop in the Main pass (child before parent), preventing the parent scroll from winning.
- Stable `pointerInput` key: `uri.toString()` — survives recompositions from list reorders
  without cancelling an in-progress drag (was `index`, which restarted on every reorder).
- `rememberUpdatedState(pdfList)` — gives the coroutine the current list for `size` checks
  and initial index lookup at drag start.
- Local `trackedIdx` and `accumulator` inside the `pointerInput` block: updated synchronously
  on every swap (`trackedIdx++/--`, `accumulator -= itemHeightPx`), so multi-step drags across
  the entire list work correctly even when recomposition lags a frame.
- `key(entry.uri.toString())` wraps each `PdfListItem` inside `forEachIndexed`. Without it,
  `Column` uses positional reconciliation: when items swap positions Compose sees a different
  `pointerInput` key at each slot, restarts both coroutines, and cancels the in-progress gesture
  after every single move — forcing the user to release and re-drag. With `key()`, Compose tracks
  composables by URI identity so the item moves in the tree instead of being recreated; the
  `pointerInput` coroutine (and its `trackedIdx`) survives the reorder and the gesture continues
  across the full list in one uninterrupted press-and-hold.

### Progressive metadata loading (2026-06-28)
`PdfEntry` has `isLoadingMetadata: Boolean = false`.
`addPdf()` now two-phases:
1. Queries `DISPLAY_NAME` synchronously (ContentResolver DB lookup, main-thread safe) and adds
   the entry immediately with `isLoadingMetadata = true` — filename appears at once.
2. Launches a coroutine: `withContext(IO) { readSizeAndPageCount(uri) }` reads `SIZE` and opens
   `PdfRenderer` to get page count, then finds the entry by URI and patches it with
   `copy(pageCount, fileSize, isLoadingMetadata = false)`.
Each file updates individually; multi-file selections appear and resolve one by one.
`PdfListItem` shows `"Loading details…"` while `isLoadingMetadata = true`, then the real meta line.
Summary totals (`totalPages`, `totalSize`) automatically reflect partial state during loading.

### PDF metadata in list items (2026-06-28)
`PdfEntry` extended with `pageCount: Int` and `fileSize: Long`.
`addPdf()` now launches `withContext(Dispatchers.IO) { buildPdfEntry(uri) }` which:
- queries `OpenableColumns.DISPLAY_NAME` + `OpenableColumns.SIZE` in one ContentResolver call
- opens a `PdfRenderer` to read `pageCount`, then closes it (PdfRenderer owns the pfd lifecycle)
- double-checks for duplicates after IO before updating state (concurrent-add guard)
`PdfListItem` shows `"N pages • X.X MB"` as a second line using `Formatter.formatShortFileSize`.
Line is omitted entirely if both values are zero (graceful failure).
`index`/`total` parameters removed from `PdfListItem` (were only used for "File X of Y" display).

### PDF list summary (MergePdfScreen)
When the list is non-empty, two `Text` lines appear above `ReorderablePdfList` inside the card:
- Line 1: `pluralStringResource(R.plurals.merge_summary_count, count, count)` → "5 PDFs selected"
- Line 2: `"$totalPages pages • ${Formatter.formatShortFileSize(context, totalSize)}"` → "146 pages • 142 MB"
  (only rendered when at least one value is non-zero; each part omitted if its value is zero)
Both recompute from `uiState.pdfList` on every recomposition — automatically reflects add/remove/reorder.
Typography: `bodyLarge`/`onSurface` for count; `bodySmall`/`outline` for pages+size (matches existing card style).

### "Add More PDFs" button (MergePdfScreen)
The existing `SecondaryButton` at the bottom of the card changes its label based on list state:
- Empty list → "Add PDF" (`merge_add_pdf`)
- Non-empty list → "Add More PDFs" (`merge_add_more_pdf`)
Same position, same action (opens multi-file picker), same duplicate detection.

### Multi-file picker (MergePdfScreen — pdfPickerLauncher)
`OpenDocument()` replaced with `OpenMultipleDocuments()`. Result callback iterates the returned
list and calls `viewModel.addPdf(uri)` for each entry. Duplicate detection in `addPdf()` already
handles repeated URIs across selections — each duplicate shows the Snackbar and is skipped.

### Duplicate PDF detection (MergePdfViewModel — addPdf)
`addPdf()` checks `pdfList.any { it.uri == uri }` before adding. If duplicate, sets
`errorMessage = "This PDF has already been added."` and returns. `MergePdfScreen` shows
this via `LaunchedEffect(uiState.errorMessage) → snackbarHostState.showSnackbar(...)` then
calls `onErrorDismissed()` to clear it.

### Known follow-up (low priority)
NavGraph's `openMergedFile()` still references the old `Pictures/EasySmartPDF/Merged/` DocumentsUI path.
The "Open File" button on the Success screen may not open the file until this is updated.

## Split PDF — Implementation Details

### Output mode (added same session)
- `SplitOutputMode` enum in `SplitPdfUiState.kt`: SINGLE_PDF (default) / SEPARATE_PDFS
- `SplitConfig.separatePdfs: Boolean` — domain-layer flag; ViewModel maps `outputMode == SEPARATE_PDFS`
- `SplitRepositoryImpl`: two branches — Separate PDFs (unchanged: one PdfDocument per page, subfolder); Single PDF (all pages into one PdfDocument, no subfolder, `folderName=""` in Completed event)
- `SplitPdfScreen`: Output card (Card 3) visible when `selectedPageCount > 1`; hidden for single-page PDFs/ranges
- `SplitSuccessScreen`: empty `folderName` → "Saved in Documents/EasySmartPDF/Split"; non-empty → "Saved in <folderName>"
- `openSplitFolder` in NavGraph: `folderName.isEmpty()` → opens `primary:Documents/EasySmartPDF/Split`

### New files (Split PDF Phase 1)
- `domain/model/SplitConfig.kt` — pdfUri, folderName, pagesToSplit (1-based list)
- `domain/model/SplitEvent.kt` — Started / Progress / Completed / Failed
- `domain/repository/SplitRepository.kt` — interface
- `domain/usecase/SplitPdfUseCase.kt` — delegates to SplitRepository
- `data/repository/SplitRepositoryImpl.kt` — PdfRenderer opens source once; each page rendered to bitmap → PdfDocument (1 page) → MediaStore. RELATIVE_PATH = Documents/EasySmartPDF/Split/<folderName>
- `ui/screens/split/SplitPdfUiState.kt` — SplitPdfUiState + SplitPageMode enum + SplitState sealed interface
- `ui/screens/split/SplitPdfViewModel.kt` — AndroidViewModel; queries fileSize via ContentResolver alongside PdfInfo
- `ui/screens/split/SplitPdfScreen.kt` — PDF picker, file info card (name/pages/size), page selection card (All / Range with slider+input identical to CreatePictures)
- `ui/screens/split/SplitProgressScreen.kt` — "Splitting page X of Y", LinearProgressIndicator, Cancel with dialog
- `ui/screens/split/SplitSuccessScreen.kt` — file count, folder name, Open Folder, Back Home

### Output path
`Documents/EasySmartPDF/Split/<PdfName>_<yyyyMMdd_HHmm>/page_001.pdf`

### Navigation
Home → SplitPdf → SplitProgress → SplitSuccess → Home.
Shared ViewModel pattern: SplitProgressScreen receives SplitPdf back-stack entry.

### Open Folder
`openSplitFolder()` in NavGraph: docId = `primary:Documents/EasySmartPDF/Split/<folderName>`. Same DocumentsUI intent pattern as other features.

## Next Expected Step

1. Test Split PDF on device — pick PDF, split all pages, verify files in Documents/EasySmartPDF/Split/.
2. Test Page Range split — verify only selected pages are saved.
3. Test Cancel — verify partial files remain, navigation returns to SplitPdfScreen.
4. Fix NavGraph `openMergedFile()` path if still needed.
5. Resolve open decisions (data source layer, use case location, Hilt, API 26-28 storage).

Stop after each step. Wait for review.
