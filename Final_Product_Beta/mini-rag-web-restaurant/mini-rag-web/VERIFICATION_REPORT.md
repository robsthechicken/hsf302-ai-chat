# Báo cáo xác minh trước bàn giao

Ngày: 12/11/2026

## Đã kiểm tra được trong môi trường tạo source

| Kiểm tra | Kết quả |
|---|---|
| Cấu trúc 61 file Java (Java 17; sử dụng Java Compiler Tree API parse) | PASS — 0 lỗi cú pháp |
| `src/main/resources/static/js/chat.js` bằng `node --check` | PASS |
| `pom.xml` phân tích XML | PASS |
| Cú pháp cơ bản 6 trang HTML chính và cấu trúc `body`, `title`, ngôn ngữ Việt | PASS |
| `seed.json` parse JSON, 3 tài khoản, 12 món, 15 góp ý, 4 ghi chú | PASS |
| 11 món sẵn phục vụ và 1 món unavailable | PASS |
| Bất biến 15 góp ý có nhãn / chưa phân loại | PASS |
| Hằng `RAG_NOT_FOUND` và câu mặc định trong `rag.txt` khớp nhau | PASS |
| Sự tồn tại của 5 entity chính | PASS |

## Không thể kiểm chứng trong môi trường này

- Không có Apache Maven (`mvn` không tồn tại), không truy cập được Maven Central qua mạng, nên **chưa chạy `mvn -q -DskipTests compile`, `mvn test` hoặc `mvn spring-boot:run`**.
- Chưa tải được Maven dependencies để type-check Java/Spring Boot/LangChain4j và chạy test.
- Chưa kiểm thử tích hợp Groq / Gemini vì không có API key; các câu hỏi thủ công ở `AI_EVALUATION.md` để trống và chưa gắn PASS.
- Chưa chạy thử các template trong Thymeleaf lúc runtime; kiểm tra cấu trúc HTML không chứng minh template engine render thành công.

## Trước khi nghiệm thu

1. Mở project trên máy có JDK 17 + Maven và mạng.
2. Chạy `mvn clean test`, xử lý lỗi (nếu có).
3. Chạy `mvn spring-boot:run` để kiểm tra dữ liệu seed và toàn bộ trang web.
4. Đặt `GROQ_API_KEY`, `GEMINI_API_KEY`, khởi động lại và test `AI_EVALUATION.md`.
5. Nếu hai mục `mvn clean test` và test AI chưa đạt, không nên coi đây là bản sản phẩm đã nghiệm thu.

## Lưu ý đầu vào

Đặc tả nguồn kết thúc tại mục 10.3 nhưng tham chiếu các mục 11–17 không có trong file. Các giả định cho phần thiếu đã ghi đầy đủ trong `README.md`.
