package demo.web.page;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
// Không giới hạn basePackages: file quá lớn bị chặn lúc đọc multipart, TRƯỚC khi Spring chọn controller,
// nên advice gắn với package controller sẽ không bao giờ được gọi (kết quả là trang trắng 413).
@ControllerAdvice
public class PageExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String largeFile(RedirectAttributes flash){flash.addFlashAttribute("error","File phải nhỏ hơn 5 MB.");return "redirect:/owner/notes";}
}
