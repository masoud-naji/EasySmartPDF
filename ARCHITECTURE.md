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

## Folder Structure

```text
com.masoudnaji.easysmartpdf

├── data
│   └── repository (Concrete Android API implementations)
│
├── domain
│   ├── model (Core business entities)
│   ├── repository (Abstraction interfaces)
│   └── usecase (Single-responsibility logic blocks)
│
├── ui
│   ├── components (Shared UI elements + PoonelBackground)
│   ├── navigation (Central NavGraph and route definitions)
│   ├── screens (Feature-specific screens and ViewModels)
│   └── theme (Colors, Typography, Dimens)
│
├── util (Formatting, Uri helpers)
│
└── di (Manual dependency wiring)
```

---

## Principles

* **Zero Performance Impact**: High-end visuals are drawn in code, not loaded from bitmaps.
* **Calm UI**: Minimalist layouts with clear focal points.
* **Privacy First**: All processing is local; no data leaves the device.
* **Accessibility**: Large touch targets and high-contrast professional color palette.
