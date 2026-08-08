# SplitEase — Smart Bill Splitting App

A full-stack web app that splits bills among friends, with support for per-item exclusions (e.g. "Akash didn't have the ice cream, so exclude him from that item") and automatic item extraction from receipt photos using OCR.

## Why this project

Most split-bill demo apps just divide a total evenly. This one solves the harder, more realistic problem: **not everyone shares every item on a bill**. The core engineering challenge was designing a data model and split algorithm that handle per-item exclusions cleanly, then extending that to work with real, messy OCR text from photographed receipts.

## Features

- **Multi-bill support** — create and manage multiple bills, each with its own members and items
- **Per-item exclusions** — exclude specific members from specific items; the cost is automatically redistributed among the remaining participants
- **Receipt scanning (OCR)** — upload a photo of a bill; the app extracts item names and prices automatically via OCR.space, with a review/edit step before anything is committed (OCR is never blindly trusted)
- **Cross-bill summaries** — select multiple bills and see combined totals per person, plus a per-bill breakdown
- **User accounts** — register/login with hashed passwords (BCrypt) and JWT-based authentication; each user only sees their own bills
- **Persistent storage** — PostgreSQL database, so bills survive server restarts

## Tech stack

**Backend:** Java, Spring Boot, Spring Data JPA, Spring Security, PostgreSQL, JWT (jjwt), Maven, JUnit 5, Mockito

**Frontend:** React (Vite), React Router, plain CSS (custom design system)

**External integration:** OCR.space API for receipt text extraction

## The interesting part: parsing real OCR output

Receipt OCR is genuinely unreliable on tabular data — text is extracted with inconsistent spacing, and there's no guarantee that a "Price" column maps cleanly to a single token. This project includes a hand-built parser (`OcrService.extractItemDrafts`) that:

- Identifies real item rows by their leading serial number, filtering out headers/footers/totals
- Extracts the item name as the run of non-numeric tokens after the serial number
- Takes the *last* valid decimal number on the line as the item's price (the "Amount" column after any discount — not the pre-discount listed price)

This was tuned and verified against a real photographed invoice, not synthetic test data. Because OCR accuracy isn't guaranteed to generalize to every receipt layout, extracted items are always shown to the user as an **editable review table** before being added to a bill — the app never silently trusts automated extraction for financial data.

## Architecture notes

- **Bill ownership & security**: `BillService` methods take the requesting user's username as an explicit parameter (extracted from the JWT in the controller) rather than reading it from Spring Security's global context internally — this keeps the service layer easily unit-testable with plain mocks, no security-context mocking required.
- **JSON serialization**: bidirectional JPA relationships (`Item.bill`, `Bill.owner`) are marked `@JsonIgnore` to prevent infinite recursion during serialization — a common but easy-to-miss issue when exposing JPA entities directly as REST responses.
- **Token storage**: the JWT is kept in React state (via Context), not `localStorage`, to reduce exposure to XSS — the tradeoff is that refreshing the page requires logging in again, which is an intentional, defensible choice for this app's threat model.
- **In-memory review step**: OCR-scanned items are held in frontend state as an editable draft and only sent to the backend once the user confirms — the backend never receives unreviewed OCR output.

## Running locally

**Prerequisites:** Java 17+, Maven, Node.js, PostgreSQL, an [OCR.space API key](https://ocr.space/ocrapi) (free tier)

### Backend
```bash
# from the repo root
# create a local PostgreSQL database named `splitbill`
# add application-local.properties with:
#   spring.datasource.url=jdbc:postgresql://localhost:5432/splitbill
#   spring.datasource.username=postgres
#   spring.datasource.password=<your password>
#   spring.jpa.hibernate.ddl-auto=update
#   jwt.secret=<a random 32+ character string>
#   ocr.space.api.key=<your OCR.space API key>
#   spring.profiles.active=local
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

The app runs at `http://localhost:5173`, backend API at `http://localhost:8080`.

## Testing

```bash
# from the repo root
mvn test
```

16 tests covering the split algorithm (even splits, exclusions, edge cases), service-layer logic (via Mockito), and the OCR text parser (against real captured receipt text).

## Possible next steps

- Deploy live (backend + Postgres on Render, frontend on Netlify)
- Timestamp bills for a proper history view
- Move floating-point money math to `BigDecimal` for exact precision
- Broaden the OCR parser to handle additional receipt layouts
