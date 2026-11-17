# eventify
Project Akhir Pengembangan Aplikasi Mobile 

## Setup Supabase (Auth + Profil)

1. Buat project di [supabase.com](https://supabase.com), lalu salin **Project URL** dan **anon / publishable key** dari *Project Settings > API*. Jangan pernah memakai `service_role` key di aplikasi.
2. Buka *SQL Editor*, jalankan seluruh isi `supabase/auth_profile_setup.sql` (tabel `profiles`, trigger, RLS, bucket `avatars`).
3. Di *Authentication > Providers > Email*, untuk development matikan **Confirm email** supaya akun baru langsung bisa masuk. Jika dibiarkan aktif, aplikasi tetap jalan: setelah daftar, user diminta memverifikasi email dulu.
4. Tambahkan dua baris ini ke `local.properties` di root proyek (file ini sudah di-ignore git, jadi tiap anggota mengisi sendiri):

   ```properties
   supabase.url=https://xxxxxxxx.supabase.co
   supabase.anon.key=eyJ...
   ```
5. Sync Gradle dan jalankan. Tanpa dua baris itu, aplikasi menampilkan layar "Supabase belum dikonfigurasi".

Anggota lain yang ingin memakai Supabase cukup memakai `SupabaseProvider.client` (`data/SupabaseProvider.kt`), tidak perlu membuat klien baru. Id user asli ada di `client.auth.currentUserOrNull()?.id`.

