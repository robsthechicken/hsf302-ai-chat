package demo.service;
import demo.model.AppUser;
import demo.model.Role;
import demo.repository.AppUserRepository;
import demo.web.Messages;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthServiceTest {
    @Test void acceptsValidCredentialsAndTrimsUsername(){
        var repo=mock(AppUserRepository.class);var encoder=mock(PasswordEncoder.class);
        when(repo.findByUsername("owner")).thenReturn(Optional.of(new AppUser("owner","hash",Role.OWNER)));
        when(encoder.matches("pass","hash")).thenReturn(true);
        var logged=new AuthService(repo,encoder).login(" owner ","pass");
        assertEquals(Role.OWNER,logged.role());
        assertEquals("owner",logged.username());
    }
    @Test void identicalErrorForUnknownAndWrongPassword(){
        var repo=mock(AppUserRepository.class);var encoder=mock(PasswordEncoder.class);
        when(repo.findByUsername("owner")).thenReturn(Optional.of(new AppUser("owner","hash",Role.OWNER)));
        var auth=new AuthService(repo,encoder);
        assertEquals(Messages.LOGIN_FAILED,assertThrows(IllegalArgumentException.class,()->auth.login("invalid","x")).getMessage());
        assertEquals(Messages.LOGIN_FAILED,assertThrows(IllegalArgumentException.class,()->auth.login("owner","bad")).getMessage());
    }
}
