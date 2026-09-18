# Tracky — Handoff Document

## Core Goal
An Android expense-tracking app (Kotlin, Jetpack Compose) supporting shared "Spaces" — a
household or group can join the same Space via QR invite and see the same transactions,
categories, and budget in real time.

## Tech Stack
- Kotlin + Jetpack Compose (Material 3), dark theme only
- Firebase Auth (**Google Sign-In only** — email/password was fully removed)
- Firebase Firestore (remote source of truth) + Room (local offline-first cache), Room DB
  version 2, `.fallbackToDestructiveMigration()` already configured
- Hilt for DI, Navigation Compose
- ZXing (`com.journeyapps:zxing-android-embedded`) for QR generation/scanning
- Credential Manager + `googleid` library for Google Sign-In
- **Icons: self-hosted Material Symbols vector drawables**, not `material-icons-extended`
  — see Icon System section
- minSdk 26, compileSdk 35, AGP 8.5.0

## Working Method For This Project
1. **Always request the actual current file before editing it.** This project has
   repeatedly hit bugs from stale assumptions — this document deliberately does not embed
   full source, so treat every file below as "ask before touching."
2. **Full-file replacement for heavy changes; targeted diffs for small ones.**
3. **When something "isn't working" after a fix, get real evidence before guessing again**
   — a real `assembleDebug` Gradle run, Logcat output, or a screen recording. IntelliJ's
   own inline error inspector has repeatedly shown *incorrect* errors after dependency
   changes in this project; don't trust it over an actual build.
4. **IDE is IntelliJ IDEA (Ultimate + Android plugin), not Android Studio.**
5. **Import style:** `ui/screens/addtransaction/*` files use wildcard imports
   (`androidx.compose.foundation.layout.*`, etc.) by explicit user preference. Match
   whatever style the actual file already uses rather than assuming.
6. **`Routes` object in `NavGraph.kt`:** group route constants by related feature (e.g.
   `ADD_TRANSACTION` next to `EDIT_TRANSACTION`), not alphabetically.
7. Gather everything you need (file contents, clarifying questions) *before* producing a
   deliverable, rather than iterating on it piecemeal.

## Key Product/Architecture Decisions (chronological)
1. Offline-first: UI reads/writes Room only; a background coroutine mirrors Firestore
   snapshots into Room, and local writes push to Firestore async. Transactions have a
   `pendingSync` retry flag; categories currently do not (no offline-retry for categories).
2. Shared Space model: a user can belong to multiple spaces. On cold start, the app
   prefers a space with more than one member over a personal-only one
   (`SpaceRepository.getOrCreateDefaultSpace`). A **Space Switcher** screen lists every
   space the user belongs to for manual switching.
3. Space names are editable (`Space.name`) — shown as a Home top-bar subtitle, editable
   from Settings.
4. QR invite/join: Settings → "Invite to Space" shows a QR of the raw space ID (ZXing
   `QRCodeWriter`); "Join a Space" scans via ZXing's `ScanContract`/`ScanOptions` (**not**
   `IntentIntegrator` — that class isn't part of this library version; confirmed after a
   full debugging session). Any member can invite further members, no owner-only
   restriction.
5. Google Sign-In via Credential Manager + `GoogleAuthProvider`. **Known, accepted
   limitation:** on cold start for an already-logged-in user, the sign-in button can
   flash briefly before redirecting Home. Root cause traced to variable-speed Play
   Services/GMS broker connectivity on the test device, not app logic — multiple timing
   fixes (loading-state gates, polling, `AuthStateListener` + delay) were tried and
   rolled back. **Currently the simplest version is live**: one-shot `auth.currentUser`
   check in `AuthViewModel.init`, no loading gate in `AuthScreen`. If revisited, start
   with Logcat + a screen recording — that's what finally produced real evidence last
   time, guessing at timing values did not work.
6. Tapping a category card on Home opens `CategoryTransactionsScreen` (that category's
   transactions for the selected month; tap a row to edit, delete button per row).
7. Transactions have full CRUD via one shared screen+ViewModel
   (`AddTransactionScreen`/`AddTransactionViewModel`), branching on whether a
   `transactionId` nav arg is present. Category selection is a **tappable card grid**
   (reusing `CategoryCard`), not a dropdown. Date picked via `DatePickerDialog` with
   careful UTC↔local timezone conversion (Compose's `DatePickerState` is UTC-internal —
   naive handling causes an off-by-one-day bug for users behind UTC).
8. Home screen shows **two** category grids, Expense and Income, each with per-category
   month totals, in one scrollable screen (no nested scroll regions).
9. **Sync bug fixed:** `TransactionRepository`/`CategoryRepository`'s `startRemoteSync`
   originally only *upserted* each Firestore snapshot, never removing local rows that
   disappeared from a snapshot — meaning a deletion by one space member never propagated
   to others. Fixed via `deleteMissing(spaceId, keepIds)` DAO queries called after every
   `upsertAll`, with a `pendingSync = 0` guard on transactions so unsynced offline
   additions are never wiped.
10. **Category manual ordering exists**, verified: `Category.order: Long`,
    `CategoryDao` queries `ORDER BY \`order\` ASC` (backticks required, reserved SQL
    word). Reordering UI is a **"Reorder categories" dialog** — Expense and Income shown
    as two independent single-column lists with ▲▼ per row (confirmed: **no drag-and-drop
    exists**, that approach was assessed as high-effort/high-risk and replaced with this
    simpler interaction before being built). Each arrow tap persists immediately via
    `CategoryRepository.reorderCategories`.
11. **Full icon system migration** — see dedicated section below.
12. Color palette: 11 hand-tuned families (not raw Material Design colors), 6 shades
    each, warm→cool ordered, verified current in `CategoriesPreset.kt`. Landed on current
    values after multiple rounds of saturation/darkness adjustment — don't regenerate
    without being asked.
13. **Pending, not started:** a Monefy-style pie/donut chart of expenses by category on
    Home. Options discussed: `ehsannarmani/ComposeCharts` (closest visual match, least
    code), `patrykandpatrick/vico` (more general-purpose, weaker pie support), or hand-built
    `Canvas`/`drawArc` (full control, more code). **User has not picked one yet.**
14. GitHub repo exists for this project (created mid-session); push via Personal Access
    Token or SSH — GitHub disabled plain password auth for git in 2021, this caused a
    `403` the first time and was resolved.

## Icon System — Material Symbols (important, non-obvious)
The app used to use `material-icons-extended` (`Icons.Default.X` → `ImageVector`). This
was **fully migrated** to real Material Symbols vector drawable XML files because (a)
Google no longer updates `material-icons-extended`, and (b) some needed icons (`apparel`,
`pill`) only exist in Material Symbols, not the old classic set.

- Icon files live in `app/src/main/res/drawable/`, named `ic_<name>.xml`.
- `CategoriesPreset.kt`'s `ICON_CATEGORIES`/`ALL_ICONS_FLAT` are `Map<String, Int>`
  (drawable resource IDs), **not** `Map<String, ImageVector>`.
- Every render site uses `Icon(painter = painterResource(id = ...), ...)`.
- Map keys are stable strings that don't necessarily match their drawable's name (keys
  have been renamed/repointed multiple times as icons were swapped) — **always check
  `CategoriesPreset.kt` directly rather than assuming a key-to-icon mapping**, it has
  drifted from what any prior conversation described at least once already.
- Fallback icon everywhere is `R.drawable.ic_category`.

**How to add a new icon (repeatable, no new dependency):**
1. Find the exact snake_case name at fonts.google.com/icons, filtered to "Material
   Symbols".
2. Fetch the real vector drawable from Google's own GitHub repo (works even in sandboxed
   environments where the Fonts website itself isn't reachable):
   `https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android/<name>/materialsymbolsoutlined/<name>_fill1_24px.xml`
   (drop `_fill1_` for the outline style).
3. **Delete this line if present** — references a theme attribute this project doesn't
   define, breaks the build otherwise: `android:tint="?attr/colorControlNormal"`.
4. Save as `ic_<name>.xml` in `res/drawable/`.
5. Add one line to `CategoriesPreset.kt`'s `ICON_CATEGORIES`. Nothing else needs to
   change — every consumer already reads generically through `ALL_ICONS_FLAT`.
6. Not every icon has every variant — if a URL 404s, check what actually exists rather
   than guessing.

## Full File Inventory
Verified against a real `find app/src/main -type f` output — trust this structure.
Package root: `com.crisdema.tracky`

```
AndroidManifest.xml
MainActivity.kt
TrackyApp.kt

data/
  model/
    Category.kt        — id, spaceId, name, type, colorHex, icon, order
    Space.kt            — id, name, ownerId, memberIds, createdAt
    Transaction.kt      — id, spaceId, categoryId, type, amount, note, date, createdBy,
                           updatedAt, pendingSync (TransactionType enum lives here too)
  local/
    AppDatabase.kt      — Room DB, version 2, fallbackToDestructiveMigration
    Converters.kt       — Room TypeConverters
    Daos.kt             — ALL DAOs (Transaction/Category/Budget/Space) combined in ONE file
  remote/
    FirestoreSyncRepository.kt — all Firestore reads/writes
  repository/
    CategoryRepository.kt
    SettingsRepository.kt — thin SharedPreferences wrapper; currency code is per-device,
                             NOT synced across space members
    SpaceRepository.kt
    TransactionRepository.kt

di/
  AppModule.kt          — Hilt providers: Room DB, DAOs, Firestore, FirebaseAuth, app CoroutineScope

ui/
  navigation/
    NavGraph.kt         — Routes object + NavHost
  theme/
    Color.kt            — palette constants (TrackyPrimary, AccentLilac, etc.)
    Theme.kt            — `TrackyTheme` composable, dark scheme only
  screens/
    auth/                    AuthScreen.kt, AuthViewModel.kt
    transactions/            TransactionListScreen.kt, TransactionListViewModel.kt (Home)
    addtransaction/          AddTransactionScreen.kt, AddTransactionViewModel.kt (shared Add/Edit)
    categorytransactions/    CategoryTransactionsScreen.kt, CategoryTransactionsViewModel.kt
    categories/
      CategoriesScreen.kt, CategoriesViewModel.kt
      CategoriesPreset.kt    — COLOR_FAMILIES + ICON_CATEGORIES + ALL_ICONS_FLAT (source of truth, verify directly)
      components/
        AddCategoryDialog.kt, EditCategoryDialog.kt, IconPickerDialog.kt
        ColorPickerDialog.kt, DeleteCategoryDialog.kt, CategoryCard.kt
        ReorderCategoriesDialog.kt — up/down arrows, Expense/Income independent, NO drag-and-drop
    settings/                SettingsScreen.kt, SettingsViewModel.kt
    spaceswitcher/           SpaceSwitcherScreen.kt, SpaceSwitcherViewModel.kt

res/
  drawable/             — Material Symbols vector drawables (ic_*.xml); verify exact set in CategoriesPreset.kt
  mipmap-anydpi-v26/, mipmap-{h,m,x,xx,xxx}hdpi/
                        — adaptive app icon, custom-named "tracky" (NOT default "ic_launcher"):
                          tracky.xml/tracky_round.xml + per-density tracky*.webp variants
  values/
    strings.xml, themes.xml
tracky-playstore.png    — sits directly under src/main/ (not res/), Play Store preview from Image Asset wizard
```

## Routes (`NavGraph.kt`)
```
auth
home/{spaceId}
add_transaction/{spaceId}/{type}
edit_transaction/{spaceId}/{transactionId}
settings/{spaceId}
categories/{spaceId}
category_transactions/{spaceId}/{categoryId}/{yearMonth}
space_switcher/{spaceId}
```

## Firestore Layout & Security Rules
```
spaces/{spaceId}
spaces/{spaceId}/transactions/{id}
spaces/{spaceId}/categories/{id}
spaces/{spaceId}/budgets/{id}
```
Rules: any UID in `resource.data.memberIds` can read/write the space doc and everything
under it. **No per-user restriction exists** — any member can rename the space, invite
more members, or delete any transaction/category, including ones they didn't create.
Deleting a category does not cascade or reassign transactions still pointing at it (they
become orphaned, rendering with the fallback icon). Accepted tradeoffs, not bugs.

## Known Issues / Roadblocks (accepted, not yet fixed)
- Login-button flash on cold start for already-authenticated users (see decision #5).
- Edit Transaction's amount field can prefill showing a trailing `.0` (e.g. `"45.0"`).
- Categories have no offline-retry mechanism, unlike transactions.
- Any space member can delete any transaction/category — no ownership check.
- If Google Sign-In throws `ApiException: 10` (`DEVELOPER_ERROR`) on a fresh
  machine/clone, check the debug keystore SHA-1 against Firebase console — the SHA-1 in
  `google-services.json`'s `client_type: 1` entry must match.
- Pie chart feature is a pending decision (see decision #13), not started.

## Style/Process Preferences To Maintain
- Ask for actual current file contents before editing.
- Full-file replacement for heavily-changed files; precise targeted edits for small ones.
- No unnecessary code comments.
- Get real build/Logcat/screen-recording evidence before proposing another fix to a bug
  that "still isn't working."
