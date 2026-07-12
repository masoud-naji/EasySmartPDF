# PROJECT_RULES.md - Single Source of Truth

## Project

Name: Poonel
Package: `com.masoudnaji.easysmartpdf`
Platform: Android
Language: Kotlin
UI: Jetpack Compose
Minimum SDK: API 26
Architecture: MVVM + Clean Architecture style layering

## Project Goal

Poonel should become the easiest and most premium PDF app to use on Android.

The goal is not to have the most features.
The goal is to make common PDF tasks simple and professional for everyone.

Every engineering and design decision should prioritize simplicity, reliability, and high-end visual identity over feature count.

## Target User

Assume the user:

* has no technical knowledge
* values a clean, premium experience
* may be elderly or have poor eyesight
* may never have used a PDF app before

The app should never feel technical, crowded, or overwhelming.

## Design & UX Rules

* **Design Identity**: Use the architectural honeycomb geometric background globally.
* **Palette**: Strictly follow the Poonel palette: Honey Gold (`#E9A600`) and Charcoal (`#263238`).
* **Visual Clarity**: Keep the center of the screen clean. Anchored geometric elements to corners/edges.
* **Navigation**: Every screen should have one obvious primary action.
* **Large UI**: Large buttons (min 48dp), large icons, and generous spacing.
* **Language**: Use simple, friendly labels. Avoid technical words (Render, Export, URI, etc.).

## Feature Policy

Only add a feature if it:

* improves usability
* is frequently useful
* saves time
* supports professional branding

## Current Product Scope

Core PDF tools (All Stable):

1. PDF to Images
2. Merge PDF
3. Split PDF
4. Images to PDF

Future features:

* Scanner
* OCR
* Compress PDF
* Password Protection

## Architecture

Use MVVM + Repository Pattern.

Follow unidirectional data flow:
UI -> ViewModel -> UseCase -> Repository -> Result/Event -> ViewModel State -> UI

Separate:
* UI layer (Compose + ViewModels)
* domain layer (Models + Interfaces + UseCases)
* data layer (Implementations + Android APIs)

## Tech Stack

* Kotlin
* Jetpack Compose
* Material 3
* StateFlow + Coroutines
* PdfRenderer + MediaStore
* Navigation Compose

## Design System Rules

* **PoonelBackground**: Must be used on every screen via `NavGraph`.
* **Official Logo**: Use the full logo (`ic_logo_full`) in About sections and screenshots. Use the icon-only version (`ic_logo_icon`) in TopBars and Success screens.
* **Support Email**: support@poonel.app.
* **Privacy Policy**: https://poonel.app/privacy.
* **Transparency**: Screens should use transparent `Scaffold` containers to allow the geometric background to be visible.
* **Layering**: Use `alpha = 0.85f` for TopBars to create a modern glass effect.

## Storage & PDF Rules

* Use native Android `PdfRenderer`.
* Use `MediaStore` for Scoped Storage compliance.
* Save images under Pictures/Poonel.
* Save PDF documents under Documents/Poonel.
* Process PDFs incrementally (page-by-page) to save memory.
* Run all IO on `Dispatchers.IO`.

## AI Collaboration Rules

* Read `AI_HANDOFF.md` before starting any task.
* Summarize plans and wait for approval for large changes.
* Update `AI_HANDOFF.md` after every completed task.
* Every task must be small and focused.

## Documentation

* Update `DECISIONS.md` for any major architectural or design choice.
* Maintain `ARCHITECTURE.md` as the map for the project structure.
* Keep `README.md` as the public-facing brand and roadmap document.
