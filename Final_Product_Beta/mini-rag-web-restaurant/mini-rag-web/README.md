# Bếp Việt AI — Trợ lý ảo nhà hàng (Mini RAG Chat)

Dự án nâng cấp từ `mini-rag-web` theo tài liệu đặc tả được cung cấp. Java 17 / Spring Boot 3.3.5 / Thymeleaf / Bootstrap 5.3.3 / LangChain4j 1.11.0 / H2 file / PDFBox 3.0.3.

## Khởi chạy trên máy tính

Cần: **JDK 17** trở lên, **Apache Maven 3.9+**, internet để tải dependency lần đầu và dùng AI. Mở terminal tại thư mục chứa `pom.xml`:

```bash
mkdir -p data
mvn clean test
mvn spring-boot:run
```

Trên Windows PowerShell, có thể dùng `New-Item -ItemType Directory -Force data` thay `mkdir -p data`.

Truy cập http://localhost:8080/login. Dữ liệu mẫu tự nạp khi database chưa có user.

| Vai trò | Tài khoản demo | Mật khẩu demo |
|---|---|---|
| Chủ nhà hàng | `owner` | `owner123` |
| Khách | `customer1` | `customer123` |
| Khách | `customer2` | `customer123` |

Các tài khoản chỉ để demo; không sử dụng mật khẩu này ngoài môi trường học tập.

## Biến môi trường AI

Không ghi API key vào source. Thiết lập biến môi trường trước khi khởi động:

**PowerShell**
```powershell
$env:GROQ_API_KEY="YOUR_GROQ_KEY"
$env:GEMINI_API_KEY="YOUR_GEMINI_KEY"
mvn spring-boot:run
```

**Linux/macOS**
```bash
export GROQ_API_KEY="YOUR_GROQ_KEY"
export GEMINI_API_KEY="YOUR_GEMINI_KEY"
mvn spring-boot:run
```

- **Groq**: chat phía khách, trả lời RAG, phân loại feedback.
- **Gemini**: embedding, khởi tạo/tái tạo hai kho vector RAM cho góp ý và ghi chú.
- Không có API key, web và database **vẫn khởi động**; chat sẽ báo thiếu key. Không có key embedding, kho RAG bỏ qua nạp lúc khởi động, tuy nhiên vẫn gửi góp ý và quản lý ghi chú bình thường.
- Kho vector nằm trong RAM; khi restart với key embedding, ứng dụng tạo lại kho từ dữ liệu H2. `Nạp lại kho tìm kiếm` dùng khi index lỗi hoặc thêm dữ liệu lúc chưa có key.

## Đường dẫn và chức năng

| Method | Đường dẫn | Quyền | Mục đích |
|---|---|---|---|
| GET/POST | `/login` | Công khai | Đăng nhập |
| POST | `/logout` | Đã đăng nhập | Đăng xuất |
| GET | `/customer/chat` | CUSTOMER | Chat gợi ý món + gửi góp ý |
| POST | `/customer/api/chat` | CUSTOMER | JSON `{ "message": "..." }`, trả `{ "answer": "...", "sources": [] }` |
| POST | `/customer/feedback` | CUSTOMER | Lưu góp ý (form `content`) |
| GET | `/owner/rag` | OWNER | Hỏi đáp trên kho tìm kiếm |
| POST | `/owner/api/chat` | OWNER | RAG trả lời JSON kèm nguồn |
| POST | `/owner/reindex` | OWNER | Tạo lại kho vector |
| GET | `/owner/feedback` | OWNER | Góp ý và bộ lọc `filter` |
| POST | `/owner/feedback/classify` | OWNER | Phân loại góp ý chưa gắn nhãn bằng AI |
| POST | `/owner/feedback/{id}/label` | OWNER | Sửa nhãn bằng form `relevance`, `topic` |
| GET/POST | `/owner/notes` | OWNER | Xem và tạo ghi chú |
| POST | `/owner/notes/{id}` | OWNER | Cập nhật ghi chú |
| POST | `/owner/notes/{id}/delete` | OWNER | Xóa ghi chú |
| POST | `/owner/notes/import` | OWNER | Nhập `.txt`, `.md`, `.pdf` có chữ |
| GET/POST | `/owner/persona` | OWNER | Xem và cập nhật persona |

Lỗi API theo dạng JSON `{ "error": "..." }`. Session: `currentUser`; quyền chặn bằng interceptor. Không có chức năng đăng ký hay quản lý thực đơn bằng UI. Chat từng lượt độc lập, lịch sử chỉ hiển thị phía trình duyệt.

## Test và nghiệm thu

```bash
mvn -q -DskipTests compile
mvn test
mvn spring-boot:run
```

Test trong `src/test/java/demo`: parse label, đăng nhập, prompt khách, đọc file, session/role và HTTP controller với H2 test. Bảng kiểm thử AI thủ công tại `AI_EVALUATION.md`. **Chạy thử tích hợp Groq/Gemini với key thật là bước bắt buộc để xác nhận hành vi AI; test tự động không bảo đảm chất lượng câu trả lời model.**

## Đổi H2 sang MySQL (tùy chọn)

Thêm dependency `com.mysql:mysql-connector-j` scope `runtime`, đổi `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` trong `application.properties`, và bỏ driver H2. Theo hướng dẫn mục 4.2 của đặc tả, ví dụ URL `jdbc:mysql://localhost:3306/restaurant?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8`.

## Giả định và khác biệt do đặc tả bị thiếu

**Tệp đặc tả gửi kèm thực tế kết thúc ở mục 10.3**. Những phần được viện dẫn trong tài liệu nhưng không tồn tại trong tệp: 11 (controller), 12 (giao diện), 13.1 (chuỗi thông báo), 14 (test), 16 (tiêu chí hoàn thành), 17 (các bước triển khai). Những phần này được chọn theo phương án nhỏ nhất, thống nhất và công khai ở đây:

1. Quy ước URL như bảng trên, không có bản gốc để đối chiếu.
2. Chuỗi lỗi/feedback được tập trung trong `demo.web.Messages`, vì danh sách chính xác mục 13.1 không được cung cấp. Chuỗi `RAG_NOT_FOUND` được xác định theo **ghi chú mục 10.2** và dùng đúng `Tôi không tìm thấy thông tin này trong tài liệu.`.
3. UI là giao diện Thymeleaf/Bootstrap đáp ứng các luồng, không có hình tham chiếu hay ID HTML bắt buộc từ mục 12.
4. Unit/integration test và bảng đánh giá thủ công do người triển khai tạo, không tự nhận là tương ứng với mục 14 chưa được cung cấp.
5. Chỉnh hai ký tự bị lỗi mã hóa trong dữ liệu mẫu: `ƒÉn kèm` thành `ăn kèm` và `khƒÉn` thành `khăn`; các nội dung khác giữ theo tài liệu.
6. `AiModelProvider.chat(system,user)` truyền `SystemMessage` và `UserMessage` riêng biệt thông qua API của LangChain4j 1.11.0; vẫn cần xác nhận lúc compile trong môi trường có Maven.

## Hạn chế quan trọng

- Demo cục bộ, một tiến trình; **không có CSRF protection**, chống brute-force, đổi mật khẩu, HTTPS, triển khai server.
- Quy tắc prompt giúp giảm prompt injection/hallucination nhưng không triệt tiêu rủi ro. Các kết quả AI phải được đánh giá bằng bộ câu hỏi thủ công.
- RAG dùng top-K truy hồi, không đủ cơ sở để thống kê tổng số/tỷ lệ toàn bộ góp ý.
- Lịch sử chat không được chuyển lại cho AI; mỗi lượt độc lập.
- Kho vector RAM không bền vững; embedding có thể gặp hạn mức/quota 429. Khi có key, tải lại từ DB và cố gắng tái sử dụng embedding nội phiên.
- File PDF dạng ảnh scan không có OCR; chỉ đọc PDF có chữ.
- Chưa có quy trình build chạy tự động trên môi trường triển khai này vì môi trường dựng file không có Maven và không thể tải dependency từ Maven Central. **Bản mã nguồn cần được kiểm chứng bằng Maven trong môi trường có mạng trước khi công bố là chạy hoàn chỉnh.**
