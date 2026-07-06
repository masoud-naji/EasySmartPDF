# DECISIONS.md

This file records important product, UX, and architecture decisions for EasySmartPDF.

## 2026-06-27

### Decision

Project name is EasySmartPDF.

### Reason

The name communicates that the app is easy to use and smart enough to handle PDF tasks simply.

### Alternatives

- PDF Toolkit
- PDF Studio
- Smart PDF Tools

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-06-27

### Decision

Minimum SDK is API 26.

### Reason

API 26 gives a good balance between device coverage and modern Android development.

### Alternatives

API 24 was considered but rejected to reduce compatibility complexity.

API 29 was considered but rejected because it would exclude more older devices.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-06-27

### Decision

Version 1 will only implement PDF to Images.

### Reason

Starting with one useful feature keeps the project manageable and publishable.

### Alternatives

Building a full PDF toolkit immediately was rejected because it would make the project too large and slow.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-06-27

### Decision

The app will prioritize non-technical users.

### Reason

Most PDF utility apps are cluttered and confusing. EasySmartPDF should win by being simpler.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-07-04

### Decision

Use DataStore Preferences for persisting user settings (theme, language).

### Reason

PROJECT_RULES.md prohibits SharedPreferences. DataStore is the modern Android replacement: coroutine-native, type-safe, and no main-thread I/O risk.

### Alternatives

- SharedPreferences — rejected per rules.
- Room database — overkill for simple key-value preferences.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-07-04

### Decision

Settings ViewModel is scoped to the Activity and shared with NavGraph via parameter, not re-created per destination.

### Reason

The theme preference must be read at the Activity level (to wrap EasySmartPDFTheme) and must also be writable from SettingsScreen. Without Hilt, the cleanest pattern is to create one SettingsViewModel in MainActivity via `by viewModels()` and pass it into NavHost.

### Alternatives

- Separate ViewModel instances reading the same DataStore file — works but less clean.
- Hilt singleton — deferred until Hilt is adopted project-wide.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-07-04

### Decision

Language preference is stored in DataStore but does not change the app locale at runtime.

### Reason

Locale switching on Android requires restarting the Activity and handling configuration changes — a non-trivial feature. The data structure and DataStore key are in place; actual locale switching can be added later without changing the architecture.

### Alternatives

- Apply locale immediately via AppCompatDelegate.setApplicationLocales() — deferred to a future step.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted
 (locale switching deferred)

---

## 2026-07-04

### Decision

Dark theme primary color kept identical to light mode brand color (`0xFF455A64`).

### Reason

The task requires brand button color to not change between themes. The previous dark palette used `0xFFCFD8DC` (light gray), which visibly changed the button color. Keeping the same primary color preserves brand identity.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-07-04

### Decision

Use `AppCompatDelegate.setApplicationLocales()` for runtime language switching. MainActivity extends `AppCompatActivity`.

### Reason

This is the Android-standard approach for per-app language preferences. On API 33+, it applies instantly without restart. On API 26–32, it triggers Activity recreation. The API 26–32 path is handled automatically by AppCompatActivity.

### Alternatives

- Compose `LocalLayoutDirection` wrapper only — RTL layout without real locale, text direction wrong.
- Manual `ContextWrapper` — more code, same end result, more fragile.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted


---

## 2026-07-04

### Decision

RTL illustration selection driven by `LocalLayoutDirection` in Compose, not by language preference directly.

### Reason

`LocalLayoutDirection` reflects the actual resolved layout direction after locale is applied. This is the correct signal — it handles any RTL language automatically without hardcoding language checks.

### Status

---

## 2026-07-05

### Decision

Use a granular manual control system for Home screen illustrations instead of a centralized sizing system.

### Reason

PNG assets (`ic_pdf.png` and `ic_images.png`) have different internal transparent padding. Furthermore, stacked icons (Merge/Split) and single icons have different "visual weight" even at the same mathematical size. Granular variables (one for each icon in each card) allow the developer to achieve perfect visual balance by hand without complex automatic scaling logic that might break when assets are changed.

### Alternatives

- Centralized sizing (e.g., `SINGLE_ICON_SIZE`, `STACK_ICON_SIZE`) — rejected because it doesn't account for asset-specific padding differences per situation.
- Use a single combined PNG per card — rejected because it's harder to maintain and doesn't support RTL/Lottie/SVG as easily as a composable system.

### Status

Accepted
