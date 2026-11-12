package demo.web.page;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@ControllerAdvice(basePackages="demo.web.page")
public class PageExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String largeFile(RedirectAttributes flash){flash.addFlashAttribute("error","File phải nhỏ hơn 5 MB.");return "redirect:/owner/notes";}
}
