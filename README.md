# پښتو شاعري | Pashto Poetry

Flutter-based Pashto poetry platform with two separate Android applications and a shared Supabase backend.

## Android applications

- **Pashto Poetry (User):** browse published poems and categories, search by title/poet/text, save favorites on the phone, read full poems, copy and share.
- **Pashto Poetry Admin:** private sign-in for authorized admins; create, edit, publish/unpublish and delete poems; create and delete categories.

Both apps use a custom charcoal, parchment and antique-gold interface with Pashto right-to-left layout.

## Build and download

GitHub Actions builds both release APKs separately, then places them together into one ZIP file named **Pashto-Poetry-Two-Apps.zip**.

1. Open the repository's **Actions** tab.
2. Open the latest successful **Build Pashto Poetry - Two APKs in One ZIP** run.
3. Download the single artifact named **Pashto-Poetry-Two-Apps-ZIP**.
4. Extract the ZIP. It contains:
   - `Pashto-Poetry-User.apk`
   - `Pashto-Poetry-Admin.apk`
   - `README.txt`

Minimum Android version: Android 7.0 (API 24).

## Supabase

The apps connect to the configured Supabase project using its public publishable key. The database uses Row Level Security.

- Public users can read published poems and categories.
- Only signed-in users whose server-managed `app_metadata.role` is `admin` can manage poems and categories.
- Never place a Supabase service-role secret in either APK.

The database schema and policies are in `supabase/migrations/202611030001_pashto_poetry.sql`. The poetry tables must exist in the connected Supabase project. Add poems using the Admin app after the administrator account has been granted the admin role.
