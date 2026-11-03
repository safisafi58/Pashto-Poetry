# پښتو شاعري | Pashto Poetry

A Pashto poetry reader built with Flutter and Supabase.

## Features
- Pashto right-to-left interface
- Browse poems and categories from Supabase
- Search by poem title, poet, or text
- Favorites saved locally on the phone
- Full poem view, copy, and share
- Pull to refresh
- No user login required for reading

## Backend
Supabase project: `PashtoPoetry`

The app uses the project's public URL and publishable key. Public users can read published poems and categories. Database Row Level Security protects writes; only authenticated users with the server-managed `app_metadata.role = admin` claim can manage content.

Tables:
- `poetry_categories`
- `poems`

The database schema and RLS policies have already been created in the connected Supabase project. The tables are initially empty; add categories and poems in Supabase Table Editor to populate the app.

Never put a Supabase secret/service-role key in this Android app.

## Build APK
GitHub Actions automatically builds an APK on every push to `main`, or manually from **Actions → Build Pashto Poetry APK → Run workflow**. Download the APK from the run's Artifacts section.
