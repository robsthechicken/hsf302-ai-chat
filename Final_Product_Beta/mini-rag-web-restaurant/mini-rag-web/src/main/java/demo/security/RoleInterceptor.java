package demo.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.dto.SessionUser;
import demo.model.Role;
import demo.web.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.util.Map;
/**
 * Mỗi instance yêu cầu MỘT role và được gắn vào path pattern trong WebConfig.
 * Không tự đọc URL: Spring đã khớp path (bỏ qua matrix param như ";x=1") giống hệt lúc chọn controller,
 * nên kiểm quyền và routing không thể hiểu URL khác nhau.
 */
public class RoleInterceptor implements HandlerInterceptor {
    public static final String SESSION_KEY="currentUser";
    private final Role required;
    private final ObjectMapper mapper;
    public RoleInterceptor(Role required,ObjectMapper mapper){this.required=required;this.mapper=mapper;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws IOException {
        Object value=request.getSession(false)==null?null:request.getSession(false).getAttribute(SESSION_KEY);
        SessionUser user=value instanceof SessionUser s?s:null;
        // API = handler thuộc @RestController, không đoán theo chữ "/api/" trong URL
        boolean isApi=handler instanceof HandlerMethod hm&&hm.getBeanType().isAnnotationPresent(RestController.class);
        if(user==null){reject(response,isApi,401,Messages.NOT_LOGGED_IN,"/login");return false;}
        if(user.role()!=required){reject(response,isApi,403,Messages.FORBIDDEN,"/");return false;}
        return true;
    }
    private void reject(HttpServletResponse response,boolean api,int status,String message,String redirect) throws IOException {
        if(!api){response.sendRedirect(redirect);return;}
        response.setStatus(status);response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(),Map.of("error",message));
    }
}
