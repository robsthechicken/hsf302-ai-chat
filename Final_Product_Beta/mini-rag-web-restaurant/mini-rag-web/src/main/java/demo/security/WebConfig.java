package demo.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ObjectMapper mapper;
    public WebConfig(ObjectMapper mapper){this.mapper=mapper;}
    @Override public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new RoleInterceptor(Role.OWNER,mapper)).addPathPatterns("/owner/**");
        registry.addInterceptor(new RoleInterceptor(Role.CUSTOMER,mapper)).addPathPatterns("/customer/**");
    }
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
}
