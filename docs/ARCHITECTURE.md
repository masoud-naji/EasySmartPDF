# Poonel — Architecture Reference

This is the detailed technical reference. For a quick overview see the root `ARCHITECTURE.md`.

---

## Layer Map

```
UI Layer (Compose + ViewModels)
    ↓ calls
Domain Layer (UseCases + Models + Repository interfaces)
    ↓ implemented by
Data Layer (PdfRenderer, MediaStore, ContentResolver)
```

All IO runs on `Dispatchers.IO`. ViewModels hold `StateFlow` and are never accessed from the data layer.

---

## Reusable Page Editing Pattern

This is the core architectural pattern introduced in the `feature/page-editor` branch.

### Composition over inheritance

`PageOperationsDelegate` is composed into any ViewModel that needs page editing:

```kotlin
class MergePdfViewModel : AndroidViewModel(...) {
    private val delegate = PageOperationsDelegate(loadThumbnailsUseCase, viewModelScope)
    val uiState = combine(_fileState, delegate.state) { ... }.stateIn(...)
}

class PdfEditViewModel : AndroidViewModel(...) {
    private val delegate = PageOperationsDelegate(loadThumbnailsUseCase, viewModelScope)
    val uiState = combine(_fileState, delegate.state) { ... }.stateIn(...)
}
```

### Pure composables

`PageOrganizerScreen` and `PageEditorScreen` have zero ViewModel or navigation imports. They receive all state and callbacks as parameters. The NavGraph is the wiring layer:

```kotlin
composable(Screen.PageOrganizer) { backStackEntry ->
    val vm: MergePdfViewModel = viewModel(navController.getBackStackEntry(Screen.MergePdf))
    PageOrganizerScreen(
        pages = state.pages,
        onPageClick = { pageId -> navController.navigate(...) },
        onConfirm = { vm.startMerge(); navController.navigate(Screen.MergeProgress) },
        ...
    )
}
```

Adding PDF Edit reuses the same screens with different ViewModel wiring — no screen code changed.

---

## ViewModel Scoping

Each feature flow scopes its ViewModel to its entry screen's NavBackStackEntry:

| Screen | ViewModel | Scoped to |
|--------|-----------|-----------|
| MergePdfScreen | MergePdfViewModel | `merge_pdf` entry |
| PageOrganizerScreen (merge) | MergePdfViewModel | `merge_pdf` entry (resolved) |
| PageEditorScreen (merge) | MergePdfViewModel + PageEditorViewModel | `merge_pdf` entry + current entry |
| PdfEditScreen | PdfEditViewModel | `pdf_edit` entry |
| PageOrganizerScreen (edit) | PdfEditViewModel | `pdf_edit` entry (resolved) |
| PageEditorScreen (edit) | PdfEditViewModel + PageEditorViewModel | `pdf_edit` entry + current entry |

`PageEditorViewModel` is always scoped to its own screen entry — it holds a temporary copy of the page being edited and propagates changes back to the feature ViewModel on Apply.

---

## State Flow

```
User action
    → Composable callback
        → ViewModel method
            → delegate.*(pageId) or _fileState.update { }
                → StateFlow emission
                    → collectAsState() in NavGraph composable block
                        → recomposition
```

`MergePdfUiState` / `PdfEditUiState` are produced by `combine(fileState, delegate.state)` — a single observable for the UI.

---

## PDF Pipeline

```
pages: List<PageItem>
    → MergePdfUseCase(MergeConfig)
        → MergeRepositoryImpl.mergePdfs()
            → group pages by sourceUri (minimize open/close cycles)
            → for each page:
                PdfRenderer.openPage(sourcePageIndex)
                apply Matrix(rotation + fineRotation)
                drawBitmap to PdfDocument canvas
            → saveMergedPdf() via MediaStore IS_PENDING pattern
        → emit MergeEvent.Progress(current, total)
        → emit MergeEvent.Completed(fileName)
```

`swapDims` (outW ↔ outH) is determined by the 90° base rotation only. `fineRotation` is added to the matrix without affecting output dimensions.

---

## Memory Safety

- `PageOperationsDelegate.loadThumbnails()` recycles the previous bitmap before replacing.
- `PageOperationsDelegate.clear()` cancels the thumbnail coroutine and recycles all bitmaps.
- `onCleared()` in every feature ViewModel calls `delegate.clear()`.
- `PageThumbnailCard` checks `!bitmap.isRecycled` before drawing.
- PDF pages are rendered one at a time; the Bitmap is recycled immediately after drawing to the PdfDocument canvas.

---

## Navigation Routes

| Route | Screen | ViewModel source |
|-------|--------|-----------------|
| `home` | HomeScreen | — |
| `settings` | SettingsScreen | own entry |
| `merge_pdf` | MergePdfScreen | own entry |
| `page_organizer` | PageOrganizerScreen | `merge_pdf` entry |
| `page_editor/{pageIndex}` | PageEditorScreen | `merge_pdf` entry + own |
| `merge_progress` | MergeProgressScreen | `merge_pdf` entry |
| `merge_success/{fileName}` | MergeSuccessScreen | — |
| `pdf_edit` | PdfEditScreen | own entry |
| `pdf_edit_page_organizer` | PageOrganizerScreen | `pdf_edit` entry |
| `pdf_edit_page_editor/{pageIndex}` | PageEditorScreen | `pdf_edit` entry + own |
| `pdf_edit_progress` | PdfEditProgressScreen | `pdf_edit` entry |
| `pdf_edit_success/{fileName}` | PdfEditSuccessScreen | — |
| `split_pdf` | SplitPdfScreen | own entry |
| `image_to_pdf` | ImageToPdfScreen | own entry |
| `create_pictures` | CreatePicturesScreen | own entry |
