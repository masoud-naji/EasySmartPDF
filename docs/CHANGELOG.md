# Changelog

All notable changes to Poonel are documented here.

---

## [Unreleased] — feature/page-editor branch

### Added
- **Edit PDF** — new home screen feature: pick a single PDF, reorder/rotate/delete pages, save as a new file.
- **Page Editor** — tap any page thumbnail to open a per-page editor: 90° snap rotation and a ±45° fine-rotation slider.
- **Zoom preview** — long-press any thumbnail in the Page Organizer to open a full-screen zoom view.
- **Drag-to-reorder** — drag thumbnails in the 2-column grid to change page order; works correctly in both LTR and RTL (Farsi) layouts.
- `fineRotation: Float` field on `PageItem`; applied in the merge/save pipeline matrix transform.
- `PageOperationsDelegate` — extracted from `MergePdfViewModel` so any future feature can compose page management without duplicating code.
- `PageOrganizerScreen` — pure composable replacing `MergePageEditorScreen`; feature-agnostic, wired by NavGraph.
- `PageEditorScreen` — pure composable for single-page fine editing.
- Farsi translations for all new strings.

### Changed
- Nav route `merge_page_editor` renamed to `page_organizer`.
- `MergePdfViewModel` now composes `PageOperationsDelegate` instead of owning page logic directly.
- `PageThumbnailCard` uses `combinedClickable` (tap = edit, long press = zoom).
- Thumbnail rotation preview includes `fineRotation`.

---

## [Stable] — main branch

### Features
- **PDF → Images** — convert every page of a PDF into images; quality presets; saved to Pictures/Poonel.
- **Merge PDF** — combine multiple PDFs; reorder files; arrange pages before merging.
- **Split PDF** — extract a page range or split into separate files; single or multiple PDF output.
- **Images → PDF** — combine images into a PDF; A4/Letter, portrait/landscape, margin, quality, fit options.
- **Settings** — live theme switching (Light/Dark/System); language switching (English/Farsi).
- **Design System** — Honey Gold & Charcoal palette; procedural architectural geometric background; glass top bars.
