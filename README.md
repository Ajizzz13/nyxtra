# Nyxtra — Gaming-Optimized Android VPN Client

Nyxtra adalah aplikasi Android VPN client berbasis Kotlin dan Sing-box core yang berfokus pada stabilitas ping, latensi rendah, serta antarmuka yang dioptimalkan untuk game seluler (Free Fire, Mobile Legends, PUBG Mobile) dan pengguna trik injeksi operator.

---

## ⚡ Tech Stack & Arsitektur

| Komponen | Teknologi | Keterangan |
|---|---|---|
| **Platform UI** | Kotlin, Jetpack Compose | UI modern dark gaming HUD, Material 3 |
| **Sistem VPN** | Android `VpnService` | Virtual TUN interface & per-app routing |
| **Core Tunnel** | Sing-box Core (Go via Gomobile) | Biner `.aar` dengan build-tags minimal |
| **Interface Bridge** | JNI / Direct FD Passing | File Descriptor TUN langsung ke core tanpa loopback 127.0.0.1 |
| **Database Lokal** | Room Database | Penyimpanan profil akun & preferensi |
| **CI / CD** | GitHub Actions | Build APK otomatis untuk varian Modern dan Legacy |

---

## 🚀 Fitur Utama (Frontend Implemented)

1. **Dashboard & Gaming HUD**
   - Sakelar koneksi utama bertenaga animasi glowing pulse
   - Monitor kecepatan upload & download real-time
   - Indikator status engine: *Direct FD Handover*, *MTU 1280*, *Kernel TUN Stack*, *Zero Sniffing*
   - Kartu profil server aktif dengan uji latensi TCP/ICMP langsung

2. **Manajemen Profil & Bug Host**
   - Dukungan protokol: **VLESS**, **VMess**, dan **Trojan**
   - Dukungan transport: **WebSocket (WS)**, **HTTPUpgrade**, **gRPC**, dan **TCP Direct**
   - Import instan via URI / clipboard (`vless://`, `vmess://`, `trojan://`)
   - Editor profil manual untuk Server IP/Bug, Port, UUID/Password, SNI, Path, dan TLS
   - Ekspor profil kembali ke clipboard / URL
   - Pengujian ping latensi individual atau sekaligus (*Test All Ping*)

3. **Per-App Proxy (App Filtering)**
   - Master toggle aktif/nonaktif
   - Mode **Whitelist** (hanya game/aplikasi terpilih yang masuk tunnel)
   - Mode **Blacklist** (aplikasi terpilih melewati koneksi reguler operator)
   - Tombol seleksi instan khusus game (*Select Games*) & pencarian cepat aplikasi

4. **Live Logs & Diagnostik**
   - Terminal log status jabat tangan tunnel dan error
   - Filter berdasarkan level log: `ALL`, `INFO`, `WARN`, `ERROR`
   - Fitur copy log ke clipboard dan pembersihan log

5. **Low-Latency Engine Tuning**
   - Kustomisasi MTU virtual interface (1280 – 1340 bytes)
   - Pilihan Stack TUN (*System Linux Kernel* / *Mixed* / *gVisor*)
   - Toggle *Zero Routing & Zero Sniffing* (Gaming Mode)
   - Toggle *TCP NoDelay & Aggressive Keepalive*

---

## 📦 Varian Distribusi APK

Aplikasi dikonfigurasi ke dalam 2 varian APK terpisah via Gradle Flavor:

- **Modern Variant (`arm64-v8a`)**
  - Target: Android 9.0 ke atas (`minSdk 28`, `compileSdk 34`)
  - Target instruksi 64-bit murni & efisiensi daya chipset modern
- **Legacy / Universal Variant (`armeabi-v7a` & `arm64-v8a`)**
  - Target: Android 5.0 hingga Android 8.1 (`minSdk 21`, `compileSdk 34`)
  - Kompatibilitas perangkat lawas

Setiap push ke branch `main` secara otomatis memicu GitHub Actions untuk memvalidasi dan mengkompilasi kedua varian APK tersebut.

---

## 🛠️ Build Mandiri

> [!IMPORTANT]
> Repo ini **tidak** menyertakan native library sing-box. `app/libs/libbox-api.jar`
> hanya berisi binding Java (62 method `native`, **nol** `.so`), sehingga APK yang
> di-build langsung dari hasil clone akan gagal saat connect dengan pesan
> `Core runtime init failed`. Jalankan langkah 1 di bawah ini lebih dulu.

### 1. Build `libbox.aar`

Butuh Go 1.25+, Android NDK r28, dan `gomobile`.

```bash
export ANDROID_NDK_HOME=/path/to/ndk/28.x
export PATH="$PATH:$(go env GOPATH)/bin"

git clone --depth 1 --branch v1.14.2 https://github.com/SagerNet/sing-box.git
cd sing-box
go install github.com/sagernet/gomobile/cmd/gomobile \
            github.com/sagernet/gomobile/cmd/gobind
go run ./cmd/internal/build_libbox --target android --platform android/arm64,android/arm

cp libbox-legacy.aar /path/to/nyxtra/app/libs/libbox.aar
```

`app/build.gradle.kts` memakai `libbox.aar` bila ada, dan hanya jatuh ke
`libbox-api.jar` bila tidak — jadi menaruh file hasil build di `app/libs/`
otomatis mengambil alih.

### 2. Build APK

```bash
# Build varian Modern (arm64-v8a, minSdk 28)
./gradlew assembleModernRelease

# Build varian Legacy (Universal, minSdk 21)
./gradlew assembleLegacyRelease
```

APK output terletak pada:
- `app/build/outputs/apk/modern/release/`
- `app/build/outputs/apk/legacy/release/`

Kedua varian memakai `applicationId` berbeda (`.modern` / `.legacy`) sehingga
bisa terpasang berdampingan dan punya jalur upgrade sendiri. `versionCode`
diturunkan dari `BUILD_NUMBER` di CI.

### 3. Validasi konfigurasi core

Unit test hanya memeriksa bentuk JSON. Untuk memastikan config benar-benar
diterima core, jalankan validasi terhadap binary asli:

```bash
python3 tools/validate_singbox_config.py /path/to/sing-box
```

Skrip ini mereproduksi output generator untuk seluruh matriks setting
(MTU × stack × mode sniffing × protokol × transport) dan menjalankannya lewat
`sing-box check`. Langkah ini juga berjalan di CI.

### 4. Test

```bash
./gradlew :app:testModernDebugUnitTest
```

