Product Requirement Document (PRD) — Gaming-Optimized VPN Client
Dokumen ini menjadi acuan teknis dan fungsional untuk membangun aplikasi Android VPN client berbasis Kotlin dan Sing-box core yang berfokus pada stabilitas ping, latensi rendah, serta antarmuka yang familier bagi pengguna tools injeksi jaringan.
1. Ringkasan Produk
 * Kategori: Android Network Utility / Tunneling Client
 * Target Utama: Gamer seluler dan pengguna trik injeksi operator yang membutuhkan latensi stabil tanpa lonjakan jitter
 * Prinsip Utama: Arsitektur native satu proses, minim alokasi memori di userspace, dan bebas beban perutean berlebih
2. Arsitektur Teknis & Tech Stack
| Komponen | Teknologi | Keterangan |
|---|---|---|
| Platform UI | Kotlin, Jetpack Compose | Responsif, modern, dan rendering antarmuka ringan |
| Sistem VPN | Android VpnService | Mengelola virtual interface TUN dan izin jaringan |
| Core Tunnel | Sing-box Core (Go via Gomobile) | Biner .aar dengan build-tags minimal |
| Interface Bridge | JNI / Direct FD Passing | File Descriptor TUN diserahkan langsung ke core tanpa loopback |
| Database Lokal | Room Database | Penyimpanan profil akun, log riwayat, dan preferensi aplikasi |
3. Ruang Lingkup Protokol & Transport
Aplikasi hanya memuat modul protokol yang esensial demi menjaga ukuran library biner tetap ramping dan runtime eksekusi tetap gesit:
 * VLESS: Transport HTTPUpgrade, WebSocket, dan gRPC (TLS & Non-TLS / Port 80 & 443)
 * VMess: Transport WebSocket dan HTTPUpgrade (AEAD enforced)
 * Trojan: Transport TCP murni dan WebSocket over TLS
 * Dukungan Bug Host: Pengaturan kustom untuk parameter Host/SNI, Path, dan port tujuan pada tiap profil
4. Fitur Utama (Functional Requirements)
Manajemen Profil & Akun
 * Import akun instan via URI / clipboard (vless://, vmess://, trojan://) dan scan QR Code
 * Editor profil manual untuk mengubah Server IP/Bug, Port, UUID/Password, SNI, dan Path
 * Ekspor profil ke clipboard atau bagikan format URL
Antarmuka & Dashboard Pengguna
 * Tampilan utama familier ala V2Ray/Netmod: tombol sakelar koneksi besar, indikator status, dan rincian server aktif
 * Tes latensi TCP/ICMP langsung dari kartu profil sebelum koneksi dinyalakan
 * Monitor kecepatan upload dan download secara real-time
 * Tab Live Log untuk memantau status jabat tangan koneksi dan mendiagnosis error
Per-App Proxy (App Filtering)
 * Fitur opsional dengan toggle aktif/nonaktif
 * Mode Whitelist (hanya aplikasi yang dicentang yang melewati tunnel)
 * Mode Blacklist (aplikasi yang dicentang dilepas langsung ke koneksi reguler)
 * Pencarian cepat aplikasi terpasang di HP pengguna
5. Arsitektur Latensi Rendah (Low-Latency Engine)
Fitur teknis yang wajib ditanam di balik layar untuk memastikan ping rata:
 * Direct FD Handover: File Descriptor dari VpnService.establish() dioper langsung ke inbound TUN Sing-box. Menghilangkan ketergantungan socket proxy lokal 127.0.0.1.
 * Kustomisasi MTU Default: Nilai MTU virtual interface dikunci pada rentang 1280 hingga 1340 byte untuk mencegah pemotongan paket data di level BTS operator.
 * Stack TUN Mode system / mixed: Menyerahkan pemrosesan paket IP langsung ke kernel Linux Android, bukan lewat emulasi gVisor di memori userspace.
 * Zero Routing & Zero Sniffing: Saat mode game aktif, modul sniffing domain dan aturan pemilah rute kompleks dimatikan total agar paket data meluncur tanpa analisis payload berulang.
 * Keepalive & TCP NoDelay: Memaksa socket outbound selalu mengaktifkan flag TCP_NODELAY dan interval keepalive agresif untuk menjaga modem HP tetap di status transmisi daya tinggi.
6. Rencana Build & Varian Distribusi
Aplikasi dirilis ke dalam 2 varian APK terpisah agar ukuran unduhan tetap kecil dan instruksi prosesor berjalan optimal:
Varian Modern (arm64-v8a)
 * Target: Android 9.0 ke atas (API 28+)
 * Fokus: Instruksi 64-bit penuh, efisiensi konsumsi daya chipset modern, dan scheduler CPU multi-cluster
Varian Legacy / Universal (armeabi-v7a & arm64)
 * Target: Android 5.0 hingga Android 8.1 (API 21+)
 * Fokus: Kompatibilitas perangkat lawas dengan konfigurasi fallback stack yang aman
7. Tahapan Pengembangan (Milestone MVP)
 * Fase 1 (Core Compiling): Setup repositori Sing-box, pangkas build tags hanya untuk VLESS, VMess, dan Trojan, lalu kompilasi library .aar via Gomobile.
 * Fase 2 (VPN Service & JNI): Buat background service Kotlin, implementasikan VpnService, dan uji oper File Descriptor ke library native.
 * Fase 3 (Parser & UI Dasar): Buat antarmuka input profil, parser URI, tombol connect/disconnect, dan live notification status.
 * Fase 4 (Tuning & Uji Lapangan): Implementasikan Per-App Proxy, pangkas buffer internal, dan uji kestabilan ping langsung di dalam match Free Fire.
