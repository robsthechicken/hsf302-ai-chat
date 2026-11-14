package demo.web.api;
import demo.web.Messages;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.ModelNotFoundException;
import dev.langchain4j.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
/**
 * Lỗi của API chat -> JSON {"error": ...}.
 * Lỗi từ nhà cung cấp AI luôn được GHI LOG đầy đủ (Spring không tự log exception đã được handler bắt),
 * còn người dùng chỉ thấy câu ngắn, không lộ chi tiết cấu hình.
 */
@RestControllerAdvice(basePackages="demo.web.api")
public class ApiExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> validation(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> configuration(IllegalStateException e){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error",e.getMessage()));}
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> invalidJson(HttpMessageNotReadableException e){return ResponseEntity.badRequest().body(Map.of("error","Yêu cầu JSON không hợp lệ."));}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String,String>> bigFile(MaxUploadSizeExceededException e){return ResponseEntity.badRequest().body(Map.of("error","File phải nhỏ hơn 5 MB."));}

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Map<String,String>> rateLimit(RateLimitException e){
        log.warn("AI provider rate limit: {}",e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error",Messages.AI_RATE_LIMIT));
    }
    /** Sai key hoặc model không còn tồn tại: lỗi cấu hình phía server, người dùng không tự sửa được. */
    @ExceptionHandler({AuthenticationException.class, ModelNotFoundException.class})
    public ResponseEntity<Map<String,String>> aiMisconfigured(RuntimeException e){
        log.error("AI provider misconfigured (kiểm tra API key và ai.chat.model / ai.embedding.model): {}",e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error",Messages.AI_UNAVAILABLE));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> unexpected(Exception e){
        log.error("Lỗi không mong đợi ở API chat",e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",Messages.UNEXPECTED_ERROR));
    }
}
