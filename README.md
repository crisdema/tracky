# Tracky (Kotlin + Compose)

## Architecture

- **UI**: Jetpack Compose, MVVM, one `ViewModel` per screen, Hilt for DI.
- **Local storage (Room)**: acts as an offline cache. The UI reads/writes only
  Room, so the app is fully usable with no network.
- **Remote (Firestore + Auth)**: source of truth for anything shared between
  people — accounts, transactions, budgets. `TransactionRepository` mirrors
  Firestore <-> Room in the background (see `startRemoteSync`).
- **Account sharing model**: an `Account` doc has a `memberIds` list. Anyone
  in that list can read/write everything nested under it. This is how
  "shared household ledger" works — invite a member by adding their uid to
  `memberIds` (see `FirestoreSyncRepository.addMember`).

## What's scaffolded

- Gradle project structure (`app/build.gradle.kts` with all dependencies)
- Data models: `Transaction`, `Category`, `Account`, `Budget`
- Room database, DAOs, type converters
- Firestore sync repository + offline-first `TransactionRepository`
- Hilt DI wiring
- Screens: Dashboard (list of accounts) → Transaction list (with
  income/expense/balance summary) → Add Transaction form
- Navigation graph connecting them

## What you still need to do to run it

1. **Open in Android Studio** (Koala/2024.1+ recommended for AGP 8.5 / Compose).
2. **Set up Firebase**:
   - Create a project at console.firebase.google.com
   - Add an Android app with package name `com.crisdema.tracky`
   - Download `google-services.json` and place it in `app/`
   - Enable **Authentication** (start with Email/Password or Google Sign-In)
   - Enable **Firestore** and paste in the security rules sketched in
     `FirestoreSyncRepository.kt`'s doc comment
3. **Add sign-in**: there's no auth screen yet. `AddTransactionViewModel`
   currently falls back to a placeholder `"local-dev-user"` uid — replace
   that flow with real Firebase Auth (email/password or Google) before
   testing sharing between two real accounts.
4. **Seed default categories**: call `DefaultCategories.seed(accountId)` and
   push through `CategoryRepository` (not yet written — same pattern as
   `TransactionRepository`) when a new account is created.
5. **App icon / launcher resources**: add `mipmap` icons (Android Studio's
   Image Asset tool does this in a couple clicks) — the manifest references
   `@mipmap/ic_launcher` which doesn't exist yet.

## Suggested next milestones (in order)

1. Sign-in screen (Firebase Auth) + "create account" / "join account by
   invite code" flow
2. `CategoryRepository` + a real category picker (replace the free-text
   field in `AddTransactionScreen`)
3. `BudgetRepository` + a budgets screen (progress bar per category vs.
   `monthlyLimit`)
4. Reports screen: pie chart of spend by category, line chart of balance
   over time (Compose Canvas or a charting lib)
5. Edit transaction (currently only add/delete)
6. Recurring transactions, multi-currency, CSV export — only if you need them

## Notes / things I simplified on purpose

- `fallbackToDestructiveMigration()` on the Room DB — fine while the schema
  is still moving, but replace with real `Migration`s before you ship.
- The `AccountDao` membership query uses `LIKE` on a comma-joined string
  since Room can't do `IN` against a `TypeConverter`-serialized list column —
  this is a deliberate tradeoff for simplicity; Firestore's `whereArrayContains`
  (the real source of truth) does this properly.
- No conflict resolution if two people edit the same transaction while both
  offline — last-write-wins via Firestore's `set()`. Worth revisiting once
  sharing is core to the UX.
