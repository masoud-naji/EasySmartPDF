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

Accepted

---

## 2026-06-27

### Decision

The app will prioritize non-technical users.

### Reason

Most PDF utility apps are cluttered and confusing. EasySmartPDF should win by being simpler.

### Status

Accepted