# AI Chat Web (Spring MVC + Thymeleaf + LangChain4j)

Trang chat với 2 chế độ tách biệt:
- **AI thường**: hỏi gì trả lời đó.
- **AI RAG**: trả lời dựa trên tài liệu `.txt` bạn upload.

Hai model, hai key riêng:
| Việc | Model | Biến môi trường |
|---|---|---|
| Chat (trả lời) | Groq `llama-3.3-70b-versatile` | `GROQ_API_KEY` |
| Embedding (tìm đoạn liên quan) | Gemini `gemini-embedding-001` | `GEMINI_API_KEY` |

## Chạy trong IntelliJ
1. File > Open > chọn `pom.xml` > Open as Project. Đợi Maven tải dependency.
2. Mở `AiChatApplication.java`, bấm Run một lần (sẽ báo thiếu key, không sao).
3. Run > Edit Configurations > Environment variables:
   `GROQ_API_KEY=...;GEMINI_API_KEY=...`
4. Run lại, mở http://localhost:8080/chat

## Cách thử
1. Tab **AI thường**: hỏi "Thủ đô của Nhật Bản là gì?" -> trả lời bình thường.
2. Tab **AI RAG**: upload 2 file trong thư mục `sample-docs/`, rồi hỏi:
   - Mua bằng MoMo thì bao lâu được hoàn tiền?
   - Chủ nhật cửa hàng mở đến mấy giờ?
   - Cửa hàng có bán laptop không? (không có trong tài liệu -> AI phải nói không biết)
3. Chuyển qua lại giữa hai tab: lịch sử chat của mỗi chế độ tách riêng.

## Cấu trúc (giống shop-dieuao: controller -> service -> impl)
```
controller/ChatController          điều phối, không chứa logic AI
service/NormalChatService (+impl)  AI thường  -> chỉ dùng ChatModel (Groq)
service/RagChatService    (+impl)  AI RAG     -> EmbeddingModel (Gemini) + ChatModel (Groq)
service/DocumentService   (+impl)  chia nhỏ + embedding + lưu tài liệu
config/AiConfig                    tạo 2 model + kho vector (RAM)
dto/                               ChatMode, ChatMessage, RagAnswer, DocumentInfo
templates/chat.html                1 trang duy nhất
```

Chỉnh tham số (model, chunk, top-k, ngưỡng điểm) trong `application.properties`.

## Giới hạn hiện tại
- Chỉ nhận `.txt`; kho vector lưu RAM nên tắt app là mất tài liệu.
- Mỗi câu hỏi độc lập (AI không nhớ câu trước). Tài liệu dùng chung cho mọi người truy cập.
- Trang tải lại sau mỗi lần gửi (không có JavaScript), nên có thể chờ vài giây.

## Lỗi thường gặp
- `Thiếu biến môi trường ...`: chưa đặt key trong Run Configuration.
- 429: hết hạn mức free, chờ một lát.
- 404 / model not found: đổi tên model trong `application.properties`.
- Không tải được dependency: đổi `langchain4j.version` trong `pom.xml` sang bản mới hơn.
