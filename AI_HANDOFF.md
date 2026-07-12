# AI_HANDOFF.md

# Poonel — Current Project Status

Last Updated: 2026-07-07
Brand: **Poonel** (Smart PDF Tools)
Visual Identity: **Premium Honey & Charcoal Architectural Design**

---

## Project Goal
Build the easiest and most premium PDF utility app on Android.

---

## Current Status: Phase 1 Completed
The app has 4 stable core features and a fully integrated premium design system.

### Completed Features
* ✅ **PDF → Images**: Stable conversion with quality settings and folder organization.
* ✅ **Merge PDF**: Multi-file selection with drag-and-drop reordering.
* ✅ **Split PDF**: All pages or range, output as single or separate files.
* ✅ **Images → PDF**: Multi-image to PDF with page size, orientation, and quality options.
* ✅ **Design System**: Global "PoonelBackground" with architectural honeycomb network.
* ✅ **Navigation**: Unified flow with transparent screen layering.
* ✅ **Branding**: Official "Poonel" branding integrated with new logo (Hexagon + PDF + Honeycomb design).
* ✅ **About Section**: Updated with clickable Privacy Policy (https://poonel.app/privacy) and Support (support@poonel.app) links.
* ✅ **Settings**: Live theme switching (Light/Dark) via DataStore. Logo integrated into About section.

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

---

## Known TODOs / Next Steps
1. **Language Switching**: Finalize the runtime locale override for Persian/English.
2. **Permissions**: Request `WRITE_EXTERNAL_STORAGE` on API 26-28.
3. **Scanner (Phase 2)**: Implementation of document scanning.
4. **OCR (Phase 2)**: Text extraction from PDF/Images.
5. **App Icon**: Update launcher foreground with the Honeycomb/Hexagon brand logo.

---

## Documentation Updated
* ✅ `README.md`: New branding, philosophy, and features.
* ✅ `PROJECT_RULES.md`: Design system rules and global background requirements.
* ✅ `ARCHITECTURE.md`: Layer definitions and design system integration.
* ✅ `DECISIONS.md`: Brand identity and background evolution history.
* ✅ `AI_HANDOFF.md`: Current project state.

---

## Development Rule
**The center of the screen must always remain clean for content focus. Background elements must stay anchored to corners and edges.**
