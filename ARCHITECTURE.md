````md
# ARCHITECTURE.md

# EasySmartPDF Architecture

## Architecture Overview

EasySmartPDF uses a layered MVVM architecture with a simple Clean Architecture style separation.

The goal is to keep the UI simple, keep business logic testable, and isolate Android platform APIs behind repositories.

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
````

---

## Why MVVM

EasySmartPDF follows the MVVM pattern to separate presentation logic from business logic.

Composables are responsible only for rendering UI state and sending user events.

ViewModels coordinate user actions, hold screen state, and communicate with the domain layer.

This separation makes the code easier to maintain, test, and extend.

---

## Why Repository Pattern

Repositories isolate Android framework APIs from the rest of the application.

The ViewModel and domain layer should not directly know about `PdfRenderer`, `MediaStore`, file descriptors, or Android storage details.

This makes the application easier to test and keeps platform-specific code in one place.

Repositories also make it easier to replace or improve implementations later without rewriting the UI.

---

## Why UseCases

Each UseCase represents one business operation.

Examples:

* Convert a PDF into images
* Merge selected PDFs
* Split selected pages
* Read PDF information

This keeps business logic reusable, testable, and independent from the UI.

UseCases also prevent ViewModels from becoming too large.

---

## Why StateFlow

StateFlow works naturally with Jetpack Compose.

It provides a single source of truth for each screen and allows the UI to react predictably when state changes.

StateFlow is also useful for progress reporting during long-running PDF operations.

---

## Why PdfRenderer

Android provides `PdfRenderer` as a native API for rendering PDF pages.

EasySmartPDF uses it because it avoids unnecessary third-party dependencies and works well for page-by-page processing.

Rendering is performed one page at a time instead of loading the full document into memory.

Advantages:

* Native Android API
* No third-party rendering dependency
* Lower memory usage
* Reliable page rendering

Trade-off:

`PdfRenderer` can render PDF pages, but it cannot edit PDFs directly.

Editing-style operations such as merge and split are implemented separately.

---

## Why MediaStore

EasySmartPDF uses `MediaStore` to save public output files in a way that works with modern Android storage rules.

This avoids direct filesystem access and supports Scoped Storage.

Images are saved under public image locations.

PDF documents are saved under public document locations such as Documents or Downloads.

Advantages:

* Compatible with modern Android versions
* Works with Scoped Storage
* Output files are visible to the user
* Public files remain accessible outside the app

---

## Memory Strategy

EasySmartPDF never loads an entire PDF into memory.

Each page is processed individually.

Resources are released immediately after processing.

This allows large PDFs to be processed more safely.

Memory rules:

* Render one page at a time
* Save output incrementally
* Release page resources immediately
* Close file descriptors properly
* Avoid holding large Bitmaps longer than necessary

---

## Large PDF Strategy

Large PDF support is based on incremental processing.

The app processes pages one by one and updates progress after each page.

This keeps memory usage mostly stable regardless of the number of pages.

Large PDF behavior:

* Supports hundreds of pages
* Never loads the entire document at once
* Shows progress during long operations
* Supports cancellation when possible
* Keeps already saved output if cancellation happens after partial completion

---

## Threading Model

```text
                Main Thread

                     │

          UI Events / Rendering

                     │

                 ViewModel

                     │

                Coroutines

                     │

             Dispatchers.IO

                     │

        PdfRenderer • MediaStore • File I/O
```

Heavy operations never run on the Main thread.

PDF processing and file operations run on `Dispatchers.IO` to keep the UI smooth and responsive.

---

## Folder Structure

```text
com.masoudnaji.easysmartpdf

├── data
│   ├── repository
│   └── source
│
├── domain
│   ├── model
│   ├── repository
│   └── usecase
│
├── ui
│   ├── components
│   ├── navigation
│   ├── screens
│   └── theme
│
├── util
│
└── di
```

### data

Contains repository implementations and Android-specific data operations.

This is where platform APIs such as `PdfRenderer`, `MediaStore`, and file access belong.

### domain

Contains business models, repository interfaces, and UseCases.

The domain layer should not depend on Compose or Android UI code.

### ui

Contains Jetpack Compose screens, reusable components, navigation, and theme code.

The UI layer displays state and sends user actions to ViewModels.

### util

Contains small helper functions or extensions that do not belong to a specific feature.

### di

Reserved for dependency injection setup when Hilt becomes necessary.

---

## Future Scalability

The architecture is designed so new PDF tools can be added without rewriting existing features.

Future features should primarily add:

* new screen
* new ViewModel
* new UseCase
* new Repository method or implementation

Examples:

* Image to PDF can reuse MediaStore and the existing progress pattern.
* Scan to PDF can add a camera/image processing layer.
* OCR can be added as a separate UseCase.
* Split and Merge can share PDF output infrastructure.

This keeps the project easier to grow while preserving simplicity.

---

## Trade-offs

EasySmartPDF intentionally favors native Android APIs and simple architecture over large third-party frameworks.

Advantages:

* Smaller dependency surface
* Easier maintenance
* More control over behavior
* Better understanding of platform APIs

Trade-offs:

* Some PDF features require custom implementation
* Advanced editing features may take longer to build
* Native APIs may not support every PDF operation directly

---

## Error Handling

Technical errors should be logged for debugging.

Users should only see friendly messages.

Examples:

Bad:

```text
IOException
```

Good:

```text
We couldn't open this PDF. Please try another file.
```

The app should handle normal errors without crashing.

---

## Progress and Cancellation

Long-running operations such as PDF conversion, merge, and split must show progress.

The user should understand what the app is doing.

Progress should use simple language such as:

```text
Creating picture 4 of 20
```

or

```text
Merging file 2 of 5
```

When possible, long-running operations should support cancellation.

Cancellation should stop future work safely and release resources.

---

## Accessibility

EasySmartPDF is designed for non-technical users and users who may have poor eyesight or limited motor precision.

Accessibility rules:

* Large touch targets
* Large readable text
* High contrast
* Simple labels
* One obvious primary action
* Minimal visual clutter
* Predictable navigation

---

## Design Decisions

Major architectural decisions are documented in `DECISIONS.md`.

This document explains the architecture at a high level.

`DECISIONS.md` explains why important choices were made, what alternatives were considered, and what trade-offs were accepted.

---

## Interview Questions

This section can be used to prepare for technical interviews.

Possible questions:

* Why did you choose MVVM?
* Why use StateFlow instead of LiveData?
* Why use Repository Pattern?
* Why use UseCases?
* Why use PdfRenderer?
* Why use MediaStore?
* How does the app handle large PDFs?
* How do you avoid memory issues?
* How does cancellation work?
* Why not load all pages at once?
* Why not put all logic inside the ViewModel?
* How would you add OCR later?
* How would you add dependency injection later?
* What would you improve if you rebuilt this project?

---

## Guiding Principle

Whenever there is a choice between adding a new feature and making an existing feature simpler, simplicity wins.

```

Confidence: 99%
```
