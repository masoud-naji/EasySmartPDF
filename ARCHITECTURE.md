# ARCHITECTURE.md

# Poonel Architecture

## Branding & Design System

Poonel uses a unique architectural design system and a professional brand identity:

* **Official Logo**: A hexagon-based logo combining PDF and honeycomb elements, representing the "Smart PDF Tools" philosophy.
* **Official Support**: support@poonel.app for user inquiries.
* **Privacy Policy**: Hosted at https://poonel.app/privacy.
* **PoonelBackground**: A procedural `Canvas`-drawn geometric background integrated at the Navigation level.
* **Global Theming**: All screens use transparent containers to layer above the branding background.
* **Glass UI**: Top bars use alpha-blending for a modern, premium feel.

---

## Architecture Overview

Poonel uses a layered MVVM architecture with a Clean Architecture style separation.

The goal is to maintain a high-performance UI with a premium visual identity while isolating complex Android platform APIs.

```text
                 UI (Jetpack Compose)
                         │
                    ViewModel
                         │
                      UseCases
                         │
                   Repositories
                         │
              Android Framework APIs
          PdfRenderer • MediaStore • SAF
```

---

## Layers

### UI Layer (`ui/`)
* **Composables**: Screens and reusable components (`AppCard`, `PrimaryButton`).
* **ViewModels**: Manage UI state and interact with UseCases.
* **Theme**: Centralized Poonel Honey & Charcoal palette.

### Domain Layer (`domain/`)
* **Models**: Pure Kotlin data classes (e.g., `PdfInfo`, `ImageEntry`).
* **UseCases**: Feature-specific business logic (e.g., `MergePdfUseCase`).
* **Repositories**: Interface definitions to maintain abstraction.

### Data Layer (`data/`)
* **Repository Impl**: concrete logic using `PdfRenderer` and `MediaStore`.
* **Scoped Storage**: Handles file persistence according to Android 10+ rules.

---

## PDF Processing Strategy

Poonel follows a **"Safe & Incremental"** strategy:

1. **Native Rendering**: Uses Android `PdfRenderer` to avoid heavy third-party SDKs.
2. **Page-by-Page**: Loads and processes only one page at a time to keep memory usage flat.
3. **Incremental Saving**: Saves output files as they are generated, supporting long-running tasks.
4. **Cooperative Cancellation**: Coroutines allow tasks to be cancelled instantly while cleaning up resources.

---

## Storage Model

* **Pictures/Poonel**: Destination for PDF-to-Image exports.
* **Documents/Poonel**: Destination for Merged, Split, and Image-to-PDF documents.
* **Scoped Storage**: Fully compliant with modern Android requirements using `MediaStore`.

---

## Threading Model

* **Main Thread**: UI Rendering and simple state updates.
* **Dispatchers.IO**: All PDF rendering, Bitmap encoding, and File I/O.
* **Dispatchers.Default**: Complex list reordering or metadata processing.

---

## Reusable Page Editing Architecture

Page editing logic is split into three reusable layers so any future feature (Scanner, OCR, etc.) can plug in without duplicating code.

```
┌─────────────────────────────────────────────────────────┐
│ Feature ViewModel (MergePdfViewModel / PdfEditViewModel) │
│  • Owns file-selection state and pipeline state          │
│  • Composes PageOperationsDelegate (not inherits)        │
│  • Exposes combined StateFlow<FeatureUiState>            │
└──────────────────────┬──────────────────────────────────┘
                       │ delegate.*
┌──────────────────────▼──────────────────────────────────┐
│ PageOperationsDelegate                                   │
│  • Single source of truth for pages + zoom state         │
│  • rotate / delete / move / updatePage / loadThumbnails  │
│  • Bitmap lifecycle: recycles stale thumbnails           │
└──────────────────────┬──────────────────────────────────┘
                       │ state
┌──────────────────────▼──────────────────────────────────┐
│ PageOrganizerScreen (pure composable)                    │
│  • No ViewModel / navigation imports                     │
│  • All state + callbacks passed from NavGraph            │
│  • Tap → navigate to PageEditorScreen                    │
│  • Long press → zoom preview                             │
│  • CTA text is caller-supplied ("Merge N Pages" / "Save")│
└──────────────────────┬──────────────────────────────────┘
                       │ onPageClick
┌──────────────────────▼──────────────────────────────────┐
│ PageEditorScreen (pure composable)                       │
│  • Receives PageItem from NavGraph                       │
│  • 90° snap rotate + fine rotation slider (±45°)         │
│  • Apply → calls FeatureViewModel.updatePage()           │
│  • PageEditorViewModel is its own scoped ViewModel       │
└─────────────────────────────────────────────────────────┘
```

**NavGraph is the wiring layer.** Each feature adds composable entries that resolve the correct ViewModel from its own back-stack entry, then pass state and callbacks down to the pure composables. The composables themselves have no knowledge of which feature is calling them.

---

## Folder Structure

```text
com.masoudnaji.easysmartpdf

├── data
│   └── repository
│       ├── MergeRepositoryImpl      — PDF merge + rotation pipeline
│       └── PageEditorRepositoryImpl — thumbnail rendering via PdfRenderer
│
├── domain
│   ├── model
│   │   ├── PageItem                 — single editable page (rotation, fineRotation, thumbnail)
│   │   ├── PageOrganizerState       — pages + thumbnail progress + zoom state
│   │   ├── MergeConfig / MergeEvent — merge pipeline I/O
│   │   └── ThumbnailState           — Pending / Loading / Loaded / Error
│   ├── repository                   — interfaces
│   └── usecase
│       ├── MergePdfUseCase          — wraps MergeRepository (reused by PDF Edit)
│       └── LoadPageThumbnailsUseCase
│
├── ui
│   ├── components
│   │   ├── PageThumbnailCard        — thumbnail card (tap=edit, long press=zoom)
│   │   ├── PageEditorGrid           — 2-column lazy grid with drag-to-reorder
│   │   ├── ZoomPreviewDialog        — full-screen pager overlay
│   │   └── PoonelBackground / AppCard / PrimaryButton / …
│   ├── navigation
│   │   └── NavGraph                 — all routes, wiring layer for shared screens
│   ├── screens
│   │   ├── home                     — HomeScreen
│   │   ├── merge                    — MergePdfScreen + ViewModel + UiState
│   │   ├── pdfedit                  — PdfEditScreen + ViewModel + UiState + Progress + Success
│   │   ├── pageeditor               — PageEditorScreen + ViewModel + UiState (per-page)
│   │   ├── pageorganizer            — PageOrganizerScreen + PageOperationsDelegate
│   │   ├── pdftoimage               — CreatePicturesScreen + ViewModel
│   │   ├── split                    — SplitPdfScreen + ViewModel
│   │   ├── imagetopdf               — ImageToPdfScreen + ViewModel
│   │   └── settings                 — SettingsScreen + ViewModel
│   └── theme                        — Colors, Typography, Spacing, Radius
│
└── util
```

---

## Principles

* **Zero Performance Impact**: High-end visuals are drawn in code, not loaded from bitmaps.
* **Calm UI**: Minimalist layouts with clear focal points.
* **Privacy First**: All processing is local; no data leaves the device.
* **Accessibility**: Large touch targets and high-contrast professional color palette.
