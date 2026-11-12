package demo.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RoleInterceptor roleInterceptor;
    public WebConfig(RoleInterceptor interceptor){this.roleInterceptor=interceptor;}
    @Override public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(roleInterceptor).addPathPatterns("/owner/**","/customer/**");
    }
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
}
