package demo.web.page;
import demo.dto.SessionUser;
import demo.security.RoleInterceptor;
import demo.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller @RequestMapping("/customer")
public class CustomerPageController {
    private final DishService dishes;private final FeedbackService feedbacks;
    public CustomerPageController(DishService dishes,FeedbackService feedbacks){this.dishes=dishes;this.feedbacks=feedbacks;}
    @GetMapping("/chat") public String chatPage(Model model){model.addAttribute("dishes",dishes.listAvailable());return "customer/chat";}
    @PostMapping("/feedback") public String submit(@RequestParam String content,HttpSession session,RedirectAttributes flash){
        SessionUser user=(SessionUser)session.getAttribute(RoleInterceptor.SESSION_KEY);
        try{feedbacks.submit(user.id(),content);flash.addFlashAttribute("success","Đã gửi góp ý. Cảm ơn bạn!");}
        catch(IllegalArgumentException e){flash.addFlashAttribute("error",e.getMessage());}
        return "redirect:/customer/chat";
    }
}
