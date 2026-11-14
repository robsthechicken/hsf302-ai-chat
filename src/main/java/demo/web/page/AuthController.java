package demo.web.page;
import demo.dto.SessionUser;
import demo.model.Role;
import demo.security.RoleInterceptor;
import demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    @GetMapping("/") public String home(HttpServletRequest request){
        var s=request.getSession(false);
        if(s==null||!(s.getAttribute(RoleInterceptor.SESSION_KEY) instanceof SessionUser user))return "redirect:/login";
        return user.role()==Role.OWNER?"redirect:/owner/rag":"redirect:/customer/chat";
    }
    @GetMapping("/login") public String loginPage(HttpServletRequest req){
        if(req.getSession(false)!=null&&req.getSession(false).getAttribute(RoleInterceptor.SESSION_KEY)!=null)return "redirect:/";
        return "login";
    }
    @PostMapping("/login") public String login(@RequestParam String username,@RequestParam String password,HttpServletRequest req,Model model){
        try{
            SessionUser user=auth.login(username,password);
            req.getSession(true);
            req.changeSessionId();
            req.getSession().removeAttribute(demo.web.api.ChatHistory.SESSION_KEY); // không lộ chat của user trước
            req.getSession().setAttribute(RoleInterceptor.SESSION_KEY,user);
            return "redirect:/";
        }catch(IllegalArgumentException e){model.addAttribute("error",e.getMessage());return "login";}
    }
    @PostMapping("/logout") public String logout(HttpServletRequest req){
        var session=req.getSession(false);if(session!=null)session.invalidate();return "redirect:/login";
    }
}
