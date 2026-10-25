# SỬA LỖI BUILD APK TRÊN GITHUB ACTIONS — APPBOST

## 1. Lỗi đã xác định

```text
Warning: Failed to find package 'tools'
Error: .../sdkmanager failed with exit code 1
```

Nguyên nhân: workflow cũ gọi `android-actions/setup-android@v3`, bản này yêu cầu gói Android SDK `tools` đã bị loại bỏ. Đây là lỗi **trước khi biên dịch mã Kotlin**, không phải lỗi SDK license, key hay code Android.

Cách sửa: `.github/workflows/build-apk.yml` dùng `android-actions/setup-android@v4`, đặt `packages: ''` để không tự yêu cầu `tools`, sau đó cài `platform-tools`, `platforms;android-34` và `build-tools;34.0.0`.

## 2. Đưa source mới lên GitHub

- Giải nén bản ZIP **mới nhất**, vào trong thư mục chứa `app/`, `gradle/`, `.github/`, `gradlew`, `build.gradle`.
- Upload/commit **tất cả file và thư mục ở cấp gốc repository**, không upload nguyên file ZIP và không lồng thêm thư mục bên ngoài.
- Kiểm tra trực tiếp trên GitHub rằng file `.github/workflows/build-apk.yml` chứa dòng `uses: android-actions/setup-android@v4`.
- Trong tab **Code** → `.github/workflows/`: nếu còn workflow *cũ khác* (ví dụ `android.yml`, `build.yml`) chứa `setup-android@v3`, hãy **sửa hoặc xóa workflow cũ** để tránh hai job khác nhau chạy, trong đó job cũ vẫn lỗi.
- GitHub tab **Actions** → **Build Android APK** → **Run workflow** → chọn nhánh có source mới (thường là `main`).
- Nếu thành công: mở lần chạy → phần **Artifacts** → `HAVANHHUAN-Booster-debug-apk` → giải nén để lấy `app-debug.apk`.

Nếu chạy bằng Git từ Windows ở thư mục source (đã `git init`, có `origin` trỏ repo hợp lệ):

```bash
git add -A
git commit -m "Fix Android SDK setup GitHub Actions"
git push origin main
```

Thay `main` bằng nhánh thực tế nếu repo dùng `master`. Với repository chưa liên kết local, có thể tải source vào GitHub bằng giao diện web hoặc clone repo trước rồi copy đè toàn bộ nội dung.

## 3. Nếu job vẫn failed

- Lỗi `Failed to find package 'tools'` một lần nữa: rất có thể đang chạy **workflow hoặc commit cũ**, hoặc repo có workflow khác vẫn gọi `setup-android@v3`. Kiểm tra file trong GitHub tại đúng nhánh.
- Lỗi Gradle Wrapper: bảo đảm upload cả `gradlew`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`. Workflow có `chmod +x gradlew`.
- Lỗi `SDK location not found`: kiểm tra bước **Set up Android SDK** và **Install required Android SDK packages**. Không thêm `local.properties` chứa đường dẫn máy Windows.
- Lỗi Kotlin / `AAPT` / `Manifest merger` chỉ có thể kết luận sau khi job chạy đến **Build debug APK**. Gửi 30–50 dòng có `FAILURE:` / `e:` hoặc `error:` trong bước đó để kiểm tra đúng lỗi mới.

## 4. Môi trường build đã cấu hình

| Thành phần | Phiên bản |
| --- | --- |
| JDK | Temurin 17 |
| Android Gradle Plugin | 8.2.2 |
| Gradle Wrapper | 8.4 |
| Android compile SDK | 34 |
| Android Build Tools | 34.0.0 |
| Min SDK | 24 |

**Quan trọng:** source được kiểm tra tĩnh nhưng **chưa được chạy biên dịch APK thành công** tại đây. Việc push source cũng không tự chỉnh sửa được repository của bạn. Phải kiểm tra lượt chạy mới trên GitHub để xác nhận cuối cùng.
