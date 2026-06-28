# PROJECT_RULES.md - Single Source of Truth

## Project

Name: EasySmartPDF  
Package: `com.masoudnaji.easysmartpdf`  
Platform: Android  
Language: Kotlin  
UI: Jetpack Compose  
Minimum SDK: API 26  
Architecture: MVVM

## Project Goal

EasySmartPDF should become the easiest PDF app to use on Android.

The goal is not to have the most features.

The goal is to make PDF tasks simple for non-technical users.

Every engineering and design decision should prioritize simplicity over feature count.

## Target User

Assume the user:

- has no technical knowledge
- may be elderly
- may have poor eyesight
- may never have used a PDF app before

The app should never feel technical or overwhelming.

## UX Rules

- Every screen should have one obvious primary action. The user should never wonder which button to press.
- Minimalism: If removing a visual element makes the screen easier to understand, remove it. Minimalism > Decoration.
- The UX is frozen. Do not redesign unless requested.
- Every screen should answer one main question.
- Avoid too many choices at once.
- Use large buttons.
- Use large icons.
- Use simple labels.
- Use generous spacing.
- Avoid dense layouts.
- Avoid technical words.

Bad words for UI:

- Render
- Export
- URI
- Bitmap
- MediaStore
- IOException
- Permission denied

Use friendly language instead.

Example:

Bad: `Rendering failed`  
Good: `We couldn't create your pictures. Please try another PDF.`

## Feature Policy

Only add a feature if it:

- improves usability
- is frequently useful
- saves time
- improves accessibility
- supports monetization without hurting UX

Do not add features only because competitors have them.

## Version 1 Scope

Only implement:

PDF to Images

Required:

- choose PDF
- show file name
- show total pages
- choose all pages or page range
- choose PNG or JPG
- choose image quality
- create images
- show progress
- save images
- show success screen

Do not implement yet:

- Image to PDF
- Merge PDF
- Split PDF
- Compress PDF
- Scanner
- OCR
- ZIP export
- Cloud backup
- Premium features

Future features may appear as disabled "Coming Soon" cards only if requested.

## Architecture

Use MVVM.

Follow unidirectional data flow:

UI -> ViewModel -> Repository/UseCase -> Result -> ViewModel State -> UI

Separate:

- UI layer
- domain layer
- data layer
- utility/helper code

Business logic should not live inside Composables.

## Tech Stack

Use:

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- StateFlow
- Kotlin Coroutines
- PdfRenderer
- MediaStore
- ActivityResultLauncher
- Hilt when dependency injection becomes necessary

## Compose Rules

- Use reusable Composables.
- Add `@Preview` for reusable UI components.
- Avoid unnecessary recompositions.
- Keep Composables small.
- Do not put heavy logic inside Composables.
- Use Material 3 components.

## Storage & PDF Rules

- Use Android `PdfRenderer`.
- Use `MediaStore` for saving images.
- Follow Scoped Storage.
- Do not request unnecessary permissions.
- Run PDF and file work on `Dispatchers.IO`.
- Do not load all PDF pages into memory at once.
- Release PDF/page resources properly.

## Ads

Use AdMob.

Rules:

- banner ads may appear at the bottom
- ads must not block important buttons
- use test ad IDs during development
- interstitial ads only after natural breaks, such as after saving
- never harm the user experience for ads

## Error Handling

Never show technical errors to users.

Map technical failures to friendly messages.

The app should not crash during normal errors.

## Accessibility

- minimum 48dp touch targets
- high contrast
- readable text
- screen reader labels
- simple navigation
- large important buttons

## AI Collaboration Rules

Before major changes:

- summarize the plan
- wait for approval
- do not modify unrelated files
- do not rename files without approval
- do not reorganize packages without approval

## Git Rules

Each feature should be small enough for one meaningful commit.

Commit prefixes:

- `feat:`
- `fix:`
- `refactor:`
- `docs:`
- `test:`
- `chore:`

## Documentation

For important decisions, update `DECISIONS.md`.

Include:

- date
- decision
- reason
- alternatives