package demo.web.api;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.Map;
@RestControllerAdvice(basePackages="demo.web.api")
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> validation(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String,String>> configuration(IllegalStateException e){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error",e.getMessage()));}
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String,String>> invalidJson(HttpMessageNotReadableException e){return ResponseEntity.badRequest().body(Map.of("error","Yêu cầu JSON không hợp lệ."));}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String,String>> bigFile(MaxUploadSizeExceededException e){return ResponseEntity.badRequest().body(Map.of("error","File phải nhỏ hơn 5 MB."));}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> unexpected(Exception e){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Có lỗi xảy ra. Vui lòng thử lại."));}
}
