# React Screen Overview

This document tracks the web React migration surface. Vue remains the behavior reference until each React screen is implemented and verified.

## App Shell

| Item | React value |
| --- | --- |
| Vite base | `/react-app/` |
| Dev port | `5174` |
| Build output | `static/react-app` |
| Backend proxy | `SERVER_DOMAIN` / `SERVER_PORT`, default `localhost:18081` (`/api`, `/oauth2`, `/login`, `/logout`, `/css`, `/font`) |
| Spring static | `/react-app/**` → `static/react-app/` SPA fallback |

## Route Map

| Route | Status | Notes |
| --- | --- | --- |
| `/sign-in` | Implemented | Vue `SignIn.vue` parity. `AuthLayout` + `SignInPage.tsx` + `shared/auth/authStore.ts`. |
| `/` | Partial | Redirects to `/journal/weekly`. |
| `/journal` | Partial | Redirects to `/journal/weekly`. |
| `/journal/weekly` | Partial | First React shell only. Vue data, modals, aside filters, and card parity are not implemented yet. |
| `/journal/monthly` | Missing | Must match Vue `JournalDayMonthly.vue`. |
| `/journal/calendar` | Missing | Must match Vue `JournalDayCalendar.vue`. |
| `/journal/meta` | Missing | Must match Vue `JournalDayMeta.vue`. |
| `/journal/daily` | Missing | Must match Vue `JournalDayDaily.vue`. |
| `/journal/entry/search` | Missing | Must match Vue `JournalEntrySearchPage.vue`. |

## Savepoint 1 Scope

- Create the React Vite project under `app/frontend-react`.
- Reuse the Gradle-provided Node/npm runtime for dependency installation and build checks.
- Keep React output separate from Vue output so the migration can converge without changing the Vue app in this savepoint.
- Do not introduce Vue fallback, bridge, or dual runtime behavior inside the React app.
