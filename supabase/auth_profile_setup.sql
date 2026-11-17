-- =====================================================================
-- Eventify: setup Auth + Profil
-- Jalankan SEKALI di Supabase Dashboard > SQL Editor.
-- Aman dijalankan ulang (memakai IF NOT EXISTS / OR REPLACE / DROP IF EXISTS).
-- =====================================================================

-- 1. Tabel profil. Satu baris per akun di auth.users.
create table if not exists public.profiles (
  id            uuid primary key references auth.users (id) on delete cascade,
  full_name     text not null default '',
  nim           text not null default '',
  phone         text,
  study_program text,
  avatar_url    text,
  updated_at    timestamptz not null default now()
);

-- 2. Otomatis isi updated_at setiap baris diubah.
create or replace function public.touch_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists profiles_touch_updated_at on public.profiles;
create trigger profiles_touch_updated_at
  before update on public.profiles
  for each row execute function public.touch_updated_at();

-- 3. Buat baris profil otomatis saat user mendaftar.
--    Nama dan NIM dikirim aplikasi lewat user metadata (full_name, nim).
--    Dilakukan di database (bukan di aplikasi) supaya tetap jalan walau konfirmasi email aktif,
--    karena saat itu user belum punya sesi dan tidak bisa menulis ke tabel.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
  insert into public.profiles (id, full_name, nim)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    coalesce(new.raw_user_meta_data ->> 'nim', '')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- 4. Row Level Security: semua user login boleh membaca profil (nama penyelenggara, dsb),
--    tetapi hanya pemilik yang boleh mengubah profilnya sendiri.
alter table public.profiles enable row level security;

drop policy if exists "profiles_select_authenticated" on public.profiles;
create policy "profiles_select_authenticated"
  on public.profiles for select
  to authenticated
  using (true);

drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
  on public.profiles for update
  to authenticated
  using (id = (select auth.uid()))
  with check (id = (select auth.uid()));

-- Tidak ada policy insert/delete untuk klien: insert dilakukan trigger di atas,
-- delete terjadi otomatis lewat on delete cascade saat akun dihapus.

-- 5. Bucket foto profil (publik untuk dibaca, hanya pemilik yang boleh menulis).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('avatars', 'avatars', true, 5242880, array['image/jpeg', 'image/png'])
on conflict (id) do update
  set public = excluded.public,
      file_size_limit = excluded.file_size_limit,
      allowed_mime_types = excluded.allowed_mime_types;

-- File disimpan di path "<user_id>/avatar", jadi folder pertama harus sama dengan id user.
drop policy if exists "avatars_insert_own" on storage.objects;
create policy "avatars_insert_own"
  on storage.objects for insert
  to authenticated
  with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);

drop policy if exists "avatars_update_own" on storage.objects;
create policy "avatars_update_own"
  on storage.objects for update
  to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text)
  with check (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);

drop policy if exists "avatars_delete_own" on storage.objects;
create policy "avatars_delete_own"
  on storage.objects for delete
  to authenticated
  using (bucket_id = 'avatars' and (storage.foldername(name))[1] = (select auth.uid())::text);

-- Pembacaan publik tidak butuh policy karena bucket bertanda public = true.
