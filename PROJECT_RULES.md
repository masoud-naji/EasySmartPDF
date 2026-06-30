# PROJECT_RULES.md - Single Source of Truth

## Project

Name: EasySmartPDF
Package: `com.masoudnaji.easysmartpdf`
Platform: Android
Language: Kotlin
UI: Jetpack Compose
Minimum SDK: API 26
Architecture: MVVM + Clean Architecture style layering

## Project Goal

EasySmartPDF should become the easiest PDF app to use on Android.

The goal is not to have the most features.
The goal is to make common PDF tasks simple for non-technical users.

Every engineering and design decision should prioritize simplicity, reliability, and clarity over feature count.

## Target User

Assume the user:

* has no technical knowledge
* may be elderly
* may have poor eyesight
* may never have used a PDF app before

The app should never feel technical, crowded, or overwhelming.

## UX Rules

* Every screen should have one obvious primary action.
* The user should never wonder which button to press.
* Every screen should answer one main question.
* Avoid too many choices at once.
* Use large buttons.
* Use large icons.
* Use simple labels.
* Use generous spacing.
* Avoid dense layouts.
* Avoid technical words.
* If removing a visual element makes the screen easier to understand, remove it.
* Do not redesign frozen screens unless explicitly requested.

Bad words for UI:

* Render
* Export
* URI
* Bitmap
* MediaStore
* IOException
* Permission denied

Use friendly language instead.

Example:

Bad: `Rendering failed`
Good: `We couldn't create your pictures. Please try another PDF.`

## Feature Policy

Only add a feature if it:

* improves usability
* is frequently useful
* saves time
* improves accessibility
* supports monetization without hurting UX

Do not add features only because competitors have them.

## Current Product Scope

Core PDF tools:

1. PDF to Images
2. Merge PDF
3. Split PDF

These are the current priority features.

Future features may be added later only after the core tools are stable:

* Image to PDF
* Compress PDF
* Scanner
* OCR
* Sign PDF
* Fill PDF forms
* ZIP export
* Cloud backup
* Premium features

## Completed / Stable Features

PDF to Images is considered stable when:

* PDF can be selected
* file name, size, and page count are shown
* all pages or page range can be selected
* PNG/JPG can be selected
* image quality can be selected
* progress is shown
* cancel works
* images are saved into organized folders
* success screen works
* output files are verified

Do not keep changing this feature unless fixing bugs or improving reliability.

## Active Feature Rules

For Merge PDF:

* allow multiple PDFs
* show filename, page count, and file size
* support duplicate detection
* support drag-and-drop reordering
* preserve selected order during merge
* show progress
* allow cancel
* save output into Documents or Downloads, not Pictures
* open merged PDF using a valid content URI and read permission

For Split PDF:

* select one PDF
* show filename, page count, and file size
* support all pages or page range
* show progress
* allow cancel
* save split PDFs into a clearly named folder
* show success screen

## Architecture

Use MVVM.

Follow unidirectional data flow:

UI -> ViewModel -> UseCase -> Repository -> Result/Event -> ViewModel State -> UI

Separate:

* UI layer
* domain layer
* data layer
* utility/helper code

Business logic must not live inside Composables.

Composables should display state and send user events only.

## Tech Stack

Use:

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* StateFlow
* Kotlin Coroutines
* PdfRenderer
* MediaStore
* ActivityResultLauncher
* Hilt only when dependency injection becomes necessary

## Compose Rules

* Use reusable Composables.
* Add `@Preview` for reusable UI components.
* Avoid unnecessary recompositions.
* Keep Composables small.
* Do not put heavy logic inside Composables.
* Use Material 3 components.
* Keep UI consistent with the existing Design System.

## Storage & PDF Rules

* Use Android `PdfRenderer` when rendering PDF pages.
* Use `MediaStore` for saving public files.
* Save images under Pictures.
* Save PDF documents under Documents or Downloads.
* Follow Scoped Storage.
* Do not request unnecessary permissions.
* Run PDF and file work on `Dispatchers.IO`.
* Do not load all PDF pages into memory at once.
* Release PDF/page/file resources properly.
* Handle large PDFs safely.

## Progress & Cancellation

Long-running tasks must:

* show progress
* show the current operation in friendly language
* support cancel when possible
* avoid blocking the UI
* clean up resources properly
* keep already saved output if cancellation happens after partial completion

## Ads

Use AdMob later.

Rules:

* banner ads may appear at the bottom
* ads must not block important buttons
* use test ad IDs during development
* interstitial ads only after natural breaks, such as after saving
* never harm the user experience for ads

## Error Handling

Never show technical errors to users.

Map technical failures to friendly messages.

The app should not crash during normal errors.

Log technical details for debugging, but show friendly messages in the UI.

## Accessibility

* minimum 48dp touch targets
* high contrast
* readable text
* screen reader labels
* simple navigation
* large important buttons

## AI Collaboration Rules

Before major changes:

* read `AI_HANDOFF.md`
* summarize the plan briefly
* wait for approval when the change is large
* do not modify unrelated files
* do not rename files without approval
* do not reorganize packages without approval

Every AI task should be small and focused.

Prefer one feature or bug fix per prompt.

After each task:

* update `AI_HANDOFF.md`
* keep the summary short
* stop and wait for review

## Git Rules

Each feature should be small enough for one meaningful commit.

Commit prefixes:

* `feat:`
* `fix:`
* `refactor:`
* `docs:`
* `test:`
* `chore:`

## Documentation

For important decisions, update `DECISIONS.md`.

Include:

* date
* decision
* reason
* alternatives
