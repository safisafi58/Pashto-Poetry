create table if not exists public.poetry_categories (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  slug text not null unique,
  description text,
  sort_order integer not null default 0,
  created_at timestamptz not null default now()
);

create table if not exists public.poems (
  id uuid primary key default gen_random_uuid(),
  category_id uuid references public.poetry_categories(id) on delete set null,
  title text not null,
  poet_name text,
  poem_text text not null,
  tags text[] not null default '{}',
  is_featured boolean not null default false,
  is_published boolean not null default true,
  view_count bigint not null default 0,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists poems_category_id_idx on public.poems(category_id);
create index if not exists poems_published_created_idx on public.poems(is_published, created_at desc);
create index if not exists poems_featured_idx on public.poems(is_featured) where is_published = true;

alter table public.poetry_categories enable row level security;
alter table public.poems enable row level security;

create policy "Public can read categories" on public.poetry_categories
for select to anon, authenticated using (true);
create policy "Admins manage categories" on public.poetry_categories
for all to authenticated
using ((select coalesce(auth.jwt()->'app_metadata'->>'role', '')) = 'admin')
with check ((select coalesce(auth.jwt()->'app_metadata'->>'role', '')) = 'admin');
create policy "Public can read published poems" on public.poems
for select to anon, authenticated using (is_published = true);
create policy "Admins manage poems" on public.poems
for all to authenticated
using ((select coalesce(auth.jwt()->'app_metadata'->>'role', '')) = 'admin')
with check ((select coalesce(auth.jwt()->'app_metadata'->>'role', '')) = 'admin');

grant usage on schema public to anon, authenticated;
revoke all privileges on table public.poetry_categories, public.poems from anon, authenticated;
grant select on table public.poetry_categories, public.poems to anon, authenticated;
grant insert, update, delete on table public.poetry_categories, public.poems to authenticated;
