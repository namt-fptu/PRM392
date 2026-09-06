# EduSummarize

EduSummarize là ứng dụng Android hỗ trợ người học tóm tắt nội dung, tạo flashcard và quiz từ tài liệu học tập. Ứng dụng tích hợp Firebase để lưu trữ dữ liệu người dùng và Gemini API để tạo nội dung học tập tự động.

## Mục tiêu

- Tóm tắt văn bản và tài liệu học tập nhanh chóng
- Chuyển nội dung thành flashcard để ôn luyện
- Tạo quiz dựa trên bài tóm tắt để kiểm tra kiến thức
- Quản lý kho tài liệu trong ứng dụng
- Hỗ trợ người dùng đăng nhập, lưu trữ dữ liệu và theo dõi tiến độ học tập

## Công nghệ sử dụng

- Android Native (Java/Kotlin + Android SDK)
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Google ML Kit Text Recognition
- Retrofit + Gson
- ExoPlayer
- Apache POI (DOCX)
- Google Gemini API

## Cấu trúc dự án

```text
PRM392/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/
│       ├── main/
│       └── test/
├── gradle/
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

## Yêu cầu trước khi chạy

- Android Studio (khuyến nghị phiên bản mới nhất)
- JDK 17
- Android SDK 34
- Một tài khoản Firebase
- Một API key Gemini từ Google AI Studio

## Thiết lập Firebase

1. Tạo project trên Firebase.
2. Thêm ứng dụng Android với package name:
   - com.example.edusummarize
3. Tải file `google-services.json` và đặt vào thư mục:
   - `app/google-services.json`
4. Bật các dịch vụ cần thiết:
   - Authentication
   - Firestore
   - Storage

## Thiết lập Gemini API key

Project đang đọc API key từ Gradle properties. Cách thiết lập nhanh:

1. Mở file `gradle.properties`
2. Thêm hoặc cập nhật:

```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

> Lưu ý: không commit khóa API lên Git. Nên giữ bí mật và có thể đặt trong biến môi trường hoặc file local không đưa lên repository.

## Build và chạy

### 1. Clone repository

```bash
git clone https://github.com/namt-fptu/PRM392.git
cd PRM392
```

### 2. Mở project trong Android Studio

- Chọn Open an existing project
- Chọn thư mục `PRM392`
- Đồng bộ Gradle

### 3. Chạy ứng dụng

- Chọn thiết bị/emulator
- Chạy task `app` hoặc nhấn Run

### 4. Nếu cần build từ terminal

```bash
./gradlew assembleDebug
```

## Tính năng chính

- Đăng nhập và đăng ký người dùng
- Tạo bản tóm tắt từ văn bản hoặc tài liệu
- Quản lý danh sách summaries trong Library
- OCR từ hình ảnh hoặc tài liệu
- Tạo flashcard từ nội dung đã tóm tắt
- Tạo bài quiz đánh giá kiến thức
- Xem kết quả quiz và phân tích câu trả lời

## Lưu ý bảo mật

- Không commit file `google-services.json` nếu repo của bạn đang công khai.
- Không lưu trữ API key trực tiếp trong mã nguồn nếu đang dùng môi trường production.
- Nên thêm các file nhạy cảm vào `.gitignore` nếu đang phát triển nội bộ.

## Ghi chú

Dự án hiện đang có cấu hình Android với namespace `com.example.edusummarize`, `compileSdk = 34`, `minSdk = 26`. Nếu bạn fork hoặc tùy chỉnh package name, cần cập nhật lại Firebase và các file cấu hình tương ứng.

## Liên hệ / phát triển tiếp

Nếu bạn muốn mở rộng ứng dụng, các điểm có thể phát triển tiếp gồm:

- cải thiện prompt cho Gemini
- thêm phân loại tài liệu theo môn học
- thêm chức năng export PDF/Word
- tăng cường caching và offline mode
- tối ưu performance cho OCR và phân tích tài liệu lớn
