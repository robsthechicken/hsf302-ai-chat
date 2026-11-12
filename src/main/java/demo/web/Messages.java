package demo.web;
/** Thông báo dùng chung. Phần 13.1 của đặc tả không có trong file được cung cấp. */
public final class Messages {
    private Messages() {}
    public static final String LOGIN_FAILED="Tên đăng nhập hoặc mật khẩu không đúng.";
    public static final String NOT_LOGGED_IN="Vui lòng đăng nhập để tiếp tục.";
    public static final String FORBIDDEN="Bạn không có quyền truy cập chức năng này.";
    public static final String CHAT_KEY_MISSING="Chưa cấu hình API key cho chat. Hãy đặt biến môi trường GROQ_API_KEY.";
    public static final String EMBEDDING_KEY_MISSING="Chưa cấu hình API key embedding. Hãy đặt biến môi trường GEMINI_API_KEY.";
    public static final String EMPTY_CONTENT="Nội dung không được để trống.";
    public static final String EMPTY_MESSAGE="Vui lòng nhập câu hỏi.";
    public static final String MESSAGE_TOO_LONG="Câu hỏi không được dài quá 4000 ký tự.";
    public static final String MENU_EMPTY="Hiện tại nhà hàng chưa có món ăn để gợi ý.";
    public static final String AI_EMPTY_ANSWER="Trợ lý chưa thể trả lời lúc này. Vui lòng thử lại.";
    public static final String RAG_EMPTY_STORE="Kho tìm kiếm hiện chưa có dữ liệu. Vui lòng nạp lại kho.";
    public static final String RAG_NOT_FOUND="Tôi không tìm thấy thông tin này trong tài liệu.";
    public static final String FEEDBACK_NOT_FOUND="Không tìm thấy góp ý.";
    public static final String FEEDBACK_EMPTY="Vui lòng nhập nội dung góp ý.";
    public static final String FEEDBACK_TOO_LONG="Góp ý không được dài quá %d ký tự.";
    public static final String FEEDBACK_RATE_LIMIT="Bạn đã gửi quá nhiều góp ý. Vui lòng thử lại sau.";
    public static final String USER_NOT_FOUND="Không tìm thấy tài khoản.";
    public static final String INVALID_LABEL="Nhãn góp ý không hợp lệ.";
    public static final String FILE_EMPTY="Vui lòng chọn file có nội dung.";
    public static final String FILE_UNSUPPORTED="Định dạng file không được hỗ trợ: %s";
    public static final String FILE_UNREADABLE="Không thể đọc file: %s";
    public static final String FILE_NO_TEXT="File không có nội dung chữ: %s";
    public static final String NOTE_TITLE_INVALID="Tiêu đề ghi chú phải dài từ 1 đến 150 ký tự.";
    public static final String NOTE_CONTENT_INVALID="Nội dung ghi chú phải dài từ 1 đến 50000 ký tự.";
    public static final String NOTE_NOT_FOUND="Không tìm thấy ghi chú.";
    public static final String PERSONA_INVALID="Giọng điệu phải dài từ 1 đến 2000 ký tự.";
}
