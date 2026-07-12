# DECISIONS.md

This file records important product, UX, and architecture decisions for Poonel.

## 2026-06-27
### Decision
Minimum SDK is API 26.
### Reason
Gives a good balance between device coverage and modern Android development.
### Status
Accepted

---

## 2026-07-04
### Decision
Use DataStore Preferences for persisting user settings (theme, language).
### Reason
Modern Android replacement for SharedPreferences: coroutine-native and type-safe.
### Status
Accepted

---

## 2026-07-06
### Decision
Rebrand the application to "Poonel".
### Reason
Creates a premium, modern atmosphere while maintaining the project's focus on simplicity.
### Status
Accepted

---

## 2026-07-10 (Production Branding)
### Decision
Integrate official support channels and privacy policy URL (https://poonel.app/privacy and support@poonel.app).
### Reason
Transitions the app from a development prototype to a production-ready product with professional support infrastructure.
### Status
Accepted

---

## 2026-07-07 (Official Branding)
### Decision
Adopt the official Poonel logo (Hexagon icon with "Poonel" and "Smart PDF Tools" tagline).
### Reason
Establishes a professional, high-end brand identity that aligns with the premium architectural design of the app.
### Status
Accepted

---

## 2026-07-07 (Background Redesign)
### Decision
Implement a procedural "Architectural Honeycomb" background using `Canvas`.
### Reason
Avoids the "wallpaper" effect of static images. Supports perfect scaling and zero performance overhead while creating a distinct brand identity.
### Status
Accepted

---

## 2026-07-07 (Branding Visibility)
### Decision
Increase background pattern opacity to ~12% and anchor it to corners.
### Reason
Ensures the brand identity is "recognizable within one second" without cluttering the center area where user content resides.
### Status
Accepted

---

## 2026-07-07 (Global Background Integration)
### Decision
Wrap the entire `NavHost` in `PoonelBackground` and make all screen containers transparent.
### Reason
Ensures a unified visual identity across the whole app. All future screens automatically inherit the premium branding without extra code.
### Status
Accepted

---

## 2026-07-07 (Vertical Technical Identity)
### Decision
Use a vertical "circuit-like" honeycomb network with vertex nodes (dots).
### Reason
Matches the specific professional, engineered look requested for the Poonel brand.
### Status
Accepted
