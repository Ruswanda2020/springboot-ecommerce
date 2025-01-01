# E-Commerce API

API ini adalah backend untuk aplikasi e-commerce yang dibangun menggunakan **Spring Boot**, dengan basis data **PostgreSQL**, caching menggunakan **Redis**, dan containerized menggunakan **Docker**. API ini juga mendukung pembayaran melalui **Xendit**, serta menggunakan **JWT** untuk autentikasi.

## Fitur
- Manajemen Produk (CRUD)
- Manajemen Kategori Produk
- Keranjang Belanja
- Pemesanan (Order)
- Pembayaran (Terintegrasi dengan Xendit)
- Manajemen Pengguna dan Peran
- Sistem autentikasi menggunakan JWT
- Logging dan analisis kode menggunakan SonarQube
- Deployable menggunakan Docker

## Teknologi yang Digunakan
- **Java 23**
- **Spring Boot**
- **PostgreSQL**
- **Redis**
- **Docker**
- **SonarQube**
- **Xendit Payment Gateway**

## Daftar Entity
Struktur entity yang digunakan dalam aplikasi:
- **Cart**: Mengelola keranjang belanja pengguna.
- **CartItem**: Item dalam keranjang belanja.
- **Category**: Kategori produk.
- **Order**: Informasi pemesanan.
- **OrdersItems**: Detil item dalam sebuah pesanan.
- **Product**: Informasi produk.
- **ProductCategory**: Relasi antara produk dan kategori.
- **Role**: Peran pengguna (misal: admin, user).
- **User**: Informasi pengguna.
- **UserAddresses**: Alamat yang dimiliki pengguna.
- **UserRole**: Relasi antara pengguna dan peran.

## Instalasi
Berikut adalah langkah-langkah untuk menjalankan aplikasi ini:

1. Clone repositori:
   ```bash
   git clone https://github.com/Ruswanda2020/springboot-ecommerce.git
   cd springboot-ecommerce
   ```

2. Pastikan Docker dan Docker Compose telah terinstal.

3. Jalankan aplikasi menggunakan Docker Compose:
   ```bash
   docker-compose up --build
   ```

4. Akses aplikasi di http://localhost:8080.

## Konfigurasi
### Environment Variables
Konfigurasikan variabel environment berikut sebelum menjalankan aplikasi:

- `DB_HOST`: Host PostgreSQL
- `DB_PORT`: Port PostgreSQL
- `DB_USER`: Username PostgreSQL
- `DB_PASSWORD`: Password PostgreSQL
- `REDIS_HOST`: Host Redis
- `XENDIT_SECRET_KEY`: API key Xendit

## Penggunaan
Gunakan tools seperti Postman atau Swagger untuk menguji endpoint API. Dokumentasi API tersedia di http://localhost:8080/swagger-ui.

## Entity-Relationship Diagram (ERD)
Berikut adalah diagram ERD untuk struktur database aplikasi ini:

![erd-ecommerce.png](../../Downloads/erd-ecommerce.png)

## Dokumentasi Teknis
### Struktur Folder
- `entity/`: Entity utama aplikasi.
- `repository/`: Layer untuk mengakses database.
- `service/`: Layer untuk logika bisnis.
- `controller/`: Endpoint API.
- `config/`: Konfigurasi aplikasi seperti JWT, Redis, dll.

## Kontribusi
Kami menerima kontribusi untuk meningkatkan API ini. Ikuti langkah berikut:

1. Fork repositori ini.
2. Buat branch baru (`feature/nama-fitur`).
3. Commit perubahan dan buat Pull Request.

