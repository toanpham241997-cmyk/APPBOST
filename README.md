# HAVANHHUAN Booster 1.1 — GitHub Actions build

Ứng dụng Android Kotlin/XML, Android 7.0+ (minSdk 24), Android Gradle Plugin 8.2.2, Gradle 8.4, JDK 17. Không cần Android Studio hoặc cài SDK trên máy để build APK: GitHub Actions lo việc đó.

## Cách build trên GitHub (không cần máy tính cài công cụ Android)

1. Giải nén ZIP và **upload tất cả tệp ở thư mục gốc** (`app/`, `gradle/`, `gradlew`, `.github/`...), không chỉ upload thư mục `app/`.
2. Vào GitHub repository → **Add file → Upload files** → tải toàn bộ thư mục/file lên → **Commit changes** tại nhánh `main` (hoặc `master`). Cách dễ bảo toàn các thư mục ẩn như `.github/` là dùng Git hoặc giao diện VS Code để push toàn bộ project.
3. Chọn **Actions → Build Android APK → Run workflow** (hoặc workflow tự chạy sau khi push).
4. Mở workflow thành công → mục **Artifacts** → tải **HAVANHHUAN-Booster-debug-apk** → giải nén để lấy `app-debug.apk` cài lên Android.

**Lưu ý:** artifact có định dạng ZIP bọc file APK, không phải file APK trực tiếp. APK debug được ký tự động bằng Android debug key, dành cho kiểm thử/phân phối nội bộ. Để phát hành chính thức, cần tạo signing key bảo mật và quy trình release riêng.

## Thay đổi bản 1.1

- Thêm `.github/workflows/build-apk.yml`, `gradlew`, `gradlew.bat`, `.gitignore`, `gradle.properties` cho GitHub Actions.
- Giao diện dashboard mới: dark glass, thẻ thống kê hiện đại, biểu đồ RAM, nút làm mới, thẻ game và điều hướng cài đặt hệ thống.
- **Thay dữ liệu giả:** RAM, dung lượng bộ nhớ, nhiệt độ **pin** và mức pin được lấy từ API Android. Gauge hiện % **RAM còn trống**, không phải điểm tối ưu tưởng tượng.
- Nút kiểm tra hiệu năng chỉ làm mới số liệu, **không giả vờ ép Android dọn RAM / làm mát CPU**. Thẻ lưu trữ mở cài đặt bộ nhớ, tiết kiệm pin mở cài đặt hệ thống, game mở trình chọn ứng dụng.
- Giữ 3 màn hình splash → kích hoạt → dashboard, giao diện neon, âm thanh và hiệu ứng sẵn có.
- Bổ sung cải thiện animation / tiết kiệm tài nguyên hạt sáng, tắt backup dữ liệu app.

## Kiểm thử và giới hạn

- Build: workflow chạy `./gradlew :app:assembleDebug --no-daemon --stacktrace`. File xuất: `app/build/outputs/apk/debug/app-debug.apk`.
- Môi trường yêu cầu: Java 17, Android SDK Platform 34, Build Tools 34.0.0. GitHub runner tải tự động.
- Chỉ có **key demo chạy tại máy** trong `Common.kt`: `KeyConfig.ACTIVE_KEY = "havanhuan"`. Key hardcode **không bảo mật** và **không kết nối với API**. Để kinh doanh/giao key thực cần backend HTTPS và xác minh có thời hạn phía máy chủ; không sử dụng đoạn demo như hệ thống cấp phép thật.
- Không có API kết nối mạng trong bản gốc, bản mới không thêm endpoint giả hoặc làm thay đổi kết nối.
- Ứng dụng Android bình thường không thể tùy ý can thiệp tài nguyên app khác, dọn toàn bộ rác hay hạ nhiệt CPU; thông số nhiệt độ trong app là **nhiệt độ pin** (khi thiết bị hỗ trợ).
- Trạng thái build thực tế phải xác nhận tại trang Actions sau khi push. Chưa cung cấp APK biên dịch sẵn.

## Cấu trúc quan trọng

```text
.github/workflows/build-apk.yml       GitHub Actions, chạy tự động khi push
app/build.gradle                      Android app, minSdk 24, targetSdk 34
app/src/main/java/com/havanhuan/booster/
    MainActivity.kt                   Dashboard + các công cụ Android
    DeviceSnapshot.kt                 Đọc thông số thực của thiết bị
    KeyActivity.kt                    Màn nhập key demo
    SplashActivity.kt                 Intro
    GaugeView.kt                      Đồng hồ RAM tùy biến
    ParticleView.kt                   Hiệu ứng hạt
app/src/main/res/                    Layout, vector icon, màu, drawable
```

Nếu báo lỗi build, mở bước **Build debug APK** trên GitHub Actions và kiểm tra 20–40 dòng lỗi phía cuối. Không tải các tệp `build/`, `.gradle/` hoặc `local.properties` lên GitHub.
