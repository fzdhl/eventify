-- 1. Buat atau perbarui bucket poster event.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'event-posters',
    'event-posters',
    true,
    10485760,
    array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

-- 2. SELECT: user login dapat melihat/list file di folder miliknya.
-- Pembacaan poster melalui public URL tetap dapat dilakukan publik karena
-- bucket ditandai public = true.
drop policy if exists "event_posters_select_own" on storage.objects;
create policy "event_posters_select_own"
on storage.objects
for select
to authenticated
using (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

-- 3. INSERT: user hanya dapat mengunggah ke folder dengan UUID miliknya.
drop policy if exists "event_posters_insert_own" on storage.objects;
create policy "event_posters_insert_own"
on storage.objects
for insert
to authenticated
with check (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

-- 4. UPDATE: user hanya dapat mengganti/memperbarui file miliknya.
-- Dibutuhkan jika proses upload menggunakan upsert = true.
drop policy if exists "event_posters_update_own" on storage.objects;
create policy "event_posters_update_own"
on storage.objects
for update
to authenticated
using (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
)
with check (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

-- 5. DELETE: user hanya dapat menghapus file miliknya.
drop policy if exists "event_posters_delete_own" on storage.objects;
create policy "event_posters_delete_own"
on storage.objects
for delete
to authenticated
using (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

-- 6. Verifikasi konfigurasi bucket.
select
    id,
    name,
    public,
    file_size_limit,
    allowed_mime_types
from storage.buckets
where id = 'event-posters';

