package demo.web.page;
import demo.dto.SessionUser;
import demo.security.RoleInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
@ControllerAdvice(basePackages="demo.web.page")
public class PageAdvice {
    @ModelAttribute("currentUser") public SessionUser currentUser(HttpServletRequest request){
        var session=request.getSession(false);
        return session!=null&&session.getAttribute(RoleInterceptor.SESSION_KEY) instanceof SessionUser user?user:null;
    }
    /** Đường dẫn hiện tại để thanh menu đánh dấu trang đang mở (Thymeleaf 3.1 không cho đọc request trong template). */
    @ModelAttribute("currentPath") public String currentPath(HttpServletRequest request){
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
