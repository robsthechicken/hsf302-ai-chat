package demo.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.dto.SessionUser;
import demo.model.Role;
import demo.web.Messages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.IOException;
import java.util.Map;
@Component
public class RoleInterceptor implements HandlerInterceptor {
    public static final String SESSION_KEY="currentUser";
    private final ObjectMapper mapper;
    public RoleInterceptor(ObjectMapper mapper){this.mapper=mapper;}
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws IOException {
        Object value=request.getSession(false)==null?null:request.getSession(false).getAttribute(SESSION_KEY);
        SessionUser user=value instanceof SessionUser s?s:null;
        String path=request.getRequestURI().substring(request.getContextPath().length());
        boolean isApi=path.contains("/api/");
        if(user==null){reject(response,isApi,401,Messages.NOT_LOGGED_IN,"/login");return false;}
        if(path.startsWith("/owner/")&&user.role()!=Role.OWNER||path.startsWith("/customer/")&&user.role()!=Role.CUSTOMER){
            reject(response,isApi,403,Messages.FORBIDDEN,"/");return false;
        }
        return true;
    }
    private void reject(HttpServletResponse response,boolean api,int status,String message,String redirect) throws IOException {
        if(!api){response.sendRedirect(redirect);return;}
        response.setStatus(status);response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(),Map.of("error",message));
    }
}
