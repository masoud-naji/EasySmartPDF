# Poonel — UI Guidelines

---

## Palette

| Token | Value | Use |
|-------|-------|-----|
| Honey Gold | `#E9A600` | Primary actions, icons, accents |
| Charcoal | `#263238` | Dark background, body text in dark mode |
| Surface | Material3 `surface` | Card backgrounds |
| Outline | Material3 `outline` | Secondary labels, metadata |

Never introduce new colors outside this palette without a recorded decision in `DECISIONS.md`.

---

## Background

- `PoonelBackground` wraps the entire `NavHost`. Every screen is transparent by default.
- The geometric network is anchored to corners and edges — the center must remain clear for content.
- Pattern opacity: ~12%. Recognizable within one second; never distracting.

---

## Typography

Use Material3 type scale only:

| Style | Use |
|-------|-----|
| `displayMedium` | Success screen headings |
| `headlineMedium` | Progress counters |
| `titleLarge` | Top bar titles |
| `titleMedium` | Card section headings |
| `bodyLarge` | Primary content labels |
| `bodyMedium` / `bodySmall` | Metadata, hints |
| `labelMedium` | Action chips, button labels |
| `labelSmall` | Page number badges |

---

## Spacing

All spacing uses the `Spacing` object (defined in theme):

| Token | Value | Use |
|-------|-------|-----|
| `xs` | 4 dp | Internal padding, badge insets |
| `sm` | 8 dp | Between related elements |
| `md` | 16 dp | Card internal padding, grid gaps |
| `lg` | 24 dp | Section gaps, bottom bar padding |
| `xl` | 32 dp | Screen-level breathing room |
| `screenPadding` | 16 dp | Horizontal page margin |

---

## Components

### `AppCard`
Use for all content sections. Provides rounded corners, elevation, and consistent padding.

### `PrimaryButton`
The one obvious action per screen. Full width, 48 dp minimum height.

### `SecondaryButton`
Secondary actions (Cancel, Add More, Change). Same size as primary.

### Top Bar
`CenterAlignedTopAppBar` with `containerColor = surface.copy(alpha = 0.85f)` for the glass effect. Never use an opaque top bar.

### `PageThumbnailCard`
- Tap → navigate to `PageEditorScreen`
- Long press → zoom preview via `ZoomPreviewDialog`
- Thumbnail area aspect ratio: 0.707 (A4 portrait)
- Rotation preview includes both `rotation` (90° snap) and `fineRotation` (±45°)

---

## Navigation

- Every screen has one back arrow (top-left).
- Every screen has one primary CTA (bottom bar).
- Progress screens block back navigation while running; show cancel dialog.
- Success screens pop the full feature stack and offer Open File + Back to Home.

---

## Language Rules

- Never use technical terms in UI labels (no "Render", "Export", "URI", "Bitmap").
- Use friendly, task-oriented language: "Save PDF", "Edit Pages", "Arrange Pages".
- All user-visible strings live in `res/values/strings.xml` and `res/values-fa/strings.xml`.

---

## Accessibility

- Minimum touch target: 48 dp.
- All icons have `contentDescription` (null only for purely decorative icons).
- Use `MaterialTheme.colorScheme` tokens — never hardcode colors in screens.
