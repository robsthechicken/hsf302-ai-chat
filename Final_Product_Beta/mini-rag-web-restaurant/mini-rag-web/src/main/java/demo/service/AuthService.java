package demo.service;
import demo.dto.SessionUser;
import demo.repository.AppUserRepository;
import demo.web.Messages;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder passwords;
    public AuthService(AppUserRepository users, PasswordEncoder passwords){this.users=users;this.passwords=passwords;}
    public SessionUser login(String username, String password){
        String name=username==null?"":username.trim();
        if(name.isBlank()||password==null||password.isBlank())throw new IllegalArgumentException(Messages.LOGIN_FAILED);
        var user=users.findByUsername(name).orElseThrow(()->new IllegalArgumentException(Messages.LOGIN_FAILED));
        if(!passwords.matches(password,user.getPasswordHash()))throw new IllegalArgumentException(Messages.LOGIN_FAILED);
        return new SessionUser(user.getId(),user.getUsername(),user.getRole());
    }
}
