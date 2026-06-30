# EasySmartPDF

<p align="center">
  <img src="docs/images/logo.png" width="140" alt="EasySmartPDF Logo">
</p>

<h3 align="center">
The easiest PDF utility for Android.
</h3>

---

## Philosophy

EasySmartPDF is **not** trying to become the biggest PDF application.

It is designed to become the **easiest** PDF utility for everyday users.

Every feature is evaluated using one simple question:

> **"Does this make PDF tasks simpler?"**

If the answer is **no**, the feature does not belong in the app.

---

# Why this project exists

Most PDF applications try to solve every possible PDF problem.

As a result they become:

- difficult to navigate
- overloaded with features
- full of technical language
- frustrating for non-technical users

EasySmartPDF takes the opposite approach.

The goal is to provide only the features people actually use, wrapped inside a clean, calm and approachable interface.

The application is designed for everyone, including:

- First-time Android users
- Elderly users
- Users with limited technical experience
- Anyone who simply wants to get a PDF task done quickly

---

# Core Principles

- Simplicity over feature count
- One obvious action per screen
- Friendly, non-technical language
- Accessibility first
- Calm and minimal UI
- Large touch targets
- Large readable typography
- Consistent design system
- Predictable user experience
- Reliability before adding new features

---

# Current Features

| Feature | Status |
|----------|--------|
| PDF → Images | ✅ Stable |
| Merge PDF | ✅ In Progress |
| Split PDF | 🚧 Under Development |

---

# Planned Features

## Phase 2

- Image → PDF
- Scan → PDF
- OCR (Extract Text)

## Future Ideas

- Sign PDF
- Fill PDF Forms
- Password Protection
- Watermark
- Rotate Pages
- Reorder Pages
- Extract Images
- Delete Pages

Only features that improve usability will be added.

---

# Architecture

```
                 EasySmartPDF

                      │

          ┌───────────┼───────────┐
          │           │           │

      PDF to       Merge      Split
      Images        PDF         PDF

          │           │           │

      Select      Select      Select
       PDF        PDFs         PDF

          │           │           │

     Configure    Reorder     Select Pages

          │           │           │

       Progress    Progress    Progress

          │           │           │

       Success     Success     Success

────────────────────────────────────────

Upcoming

Image → PDF

Scan → PDF

OCR
```

---

# Application Architecture

```
                 UI (Compose)

                      │

                 ViewModel

                      │

                  UseCases

                      │

             Repository Layer

          ┌───────────┴───────────┐

      PDF Repository       Image Repository

          └───────────┬───────────┘

              Android Platform APIs

        PdfRenderer • MediaStore • SAF
```

---

# Project Structure

```
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

---

# Roadmap

## Phase 1

✅ PDF → Images

✅ Merge PDF

🚧 Split PDF

---

## Phase 2

⬜ Image → PDF

⬜ Scan → PDF

⬜ OCR

---

## Phase 3

⬜ Sign PDF

⬜ Fill PDF Forms

⬜ Password Protection

⬜ Watermark

---

# Tech Stack

### Language

- Kotlin

### UI

- Jetpack Compose
- Material 3

### Architecture

- MVVM
- StateFlow
- Kotlin Coroutines

### Android

- Navigation Compose
- Activity Result API
- MediaStore
- Scoped Storage
- PdfRenderer

### Dependency Injection

- Hilt (planned)

---

# Design Goals

EasySmartPDF should feel:

- Calm
- Clean
- Fast
- Friendly
- Predictable

The user should never feel overwhelmed.

Every screen should answer only one question.

---

# Development Principles

- Clean Architecture
- Small reusable components
- Simple code over clever code
- Testable business logic
- Progressive enhancement
- Memory-efficient PDF processing
- Large file support
- Accessibility-first development

---

# AI-Assisted Development

This project is developed collaboratively using multiple AI assistants.

Project knowledge is maintained through:

- PROJECT_RULES.md
- AI_HANDOFF.md
- DECISIONS.md

This allows development to continue consistently across sessions while keeping prompts small and focused.

---

# Screenshots

Coming soon.

---

# License

MIT License

---

<p align="center">

Made with ❤️ using Kotlin and Jetpack Compose

</p>