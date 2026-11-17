
create index if not exists reviews_event_id_idx on public.reviews(event_id);
create index if not exists reviews_user_id_idx on public.reviews(user_id);

create or replace function public.validate_review_write()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
  v_event public.events%rowtype;
  v_uid uuid;
begin
  v_uid := (select auth.uid());
  if v_uid is null then
    raise exception 'Silakan login terlebih dahulu.';
  end if;

  if tg_op = 'INSERT' then
    if new.user_id <> v_uid then
      raise exception 'Ulasan hanya boleh dibuat untuk akun sendiri.';
    end if;
    select * into v_event from public.events where id = new.event_id;
    if not found then
      raise exception 'Event tidak ditemukan.';
    end if;
    if v_event.created_by = v_uid then
      raise exception 'Penyelenggara tidak dapat mengulas event miliknya sendiri.';
    end if;
    if v_event.event_date >= current_date then
      raise exception 'Ulasan hanya dapat diberikan setelah event selesai.';
    end if;
    if not exists (
      select 1 from public.registrations r
      where r.event_id = new.event_id
        and r.user_id = v_uid
        and r.status = 'CONFIRMED'
    ) then
      raise exception 'Hanya peserta dengan pendaftaran CONFIRMED yang dapat memberi ulasan.';
    end if;
  else
    if new.user_id <> old.user_id or new.event_id <> old.event_id then
      raise exception 'Pemilik dan event ulasan tidak boleh diubah.';
    end if;
    if old.user_id <> v_uid then
      raise exception 'Kamu hanya dapat mengubah ulasan milik sendiri.';
    end if;
    new.updated_at := now();
  end if;

  return new;
end;
$$;

drop trigger if exists reviews_validate_write on public.reviews;
create trigger reviews_validate_write
  before insert or update on public.reviews
  for each row execute function public.validate_review_write();

alter table public.reviews enable row level security;

drop policy if exists "reviews_select_authenticated" on public.reviews;
create policy "reviews_select_authenticated"
  on public.reviews for select to authenticated
  using (true);

drop policy if exists "reviews_insert_own" on public.reviews;
create policy "reviews_insert_own"
  on public.reviews for insert to authenticated
  with check (user_id = (select auth.uid()));

drop policy if exists "reviews_update_own" on public.reviews;
create policy "reviews_update_own"
  on public.reviews for update to authenticated
  using (user_id = (select auth.uid()))
  with check (user_id = (select auth.uid()));

drop policy if exists "reviews_delete_own" on public.reviews;
create policy "reviews_delete_own"
  on public.reviews for delete to authenticated
  using (user_id = (select auth.uid()));

-- ---------------------------------------------------------------------
-- 6. STORAGE POSTER EVENT - Fitur Zaqy
-- Path file: <auth.uid()>/<nama-file>
-- Bucket public agar poster dapat ditampilkan menggunakan public URL.
-- ---------------------------------------------------------------------
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

-- Public bucket: pembacaan poster memakai public URL.
drop policy if exists "event_posters_insert_own" on storage.objects;
create policy "event_posters_insert_own"
  on storage.objects for insert to authenticated
  with check (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

drop policy if exists "event_posters_update_own" on storage.objects;
create policy "event_posters_update_own"
  on storage.objects for update to authenticated
  using (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  )
  with check (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

drop policy if exists "event_posters_delete_own" on storage.objects;
create policy "event_posters_delete_own"
  on storage.objects for delete to authenticated
  using (
    bucket_id = 'event-posters'
    and (storage.foldername(name))[1] = (select auth.uid())::text
  );

-- ---------------------------------------------------------------------
-- 7. Selesai. Tabel utama:
-- profiles (sudah dibuat auth_profile_setup.sql)
-- categories, events, favorites, registrations, reviews
-- Storage buckets: avatars (sudah ada), event-posters
-- ---------------------------------------------------------------------
