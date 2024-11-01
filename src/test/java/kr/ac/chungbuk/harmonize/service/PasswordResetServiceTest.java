package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.PasswordResetToken;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.repository.PasswordResetTokenRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.service.EmailService;
import kr.ac.chungbuk.harmonize.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PasswordResetServiceTest {

    @MockBean
    private PasswordResetTokenRepository tokenRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("testUser", "password", "test@example.com", "nickname", Role.USER, Gender.MALE, 30);
        testToken = new PasswordResetToken();
        testToken.setToken("sampleToken");
        testToken.setUser(testUser);
    }

    @Test
    public void testCreateToken() {
        when(tokenRepository.findByUser(any(User.class))).thenReturn(null);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        String token = passwordResetService.createToken(testUser);

        assertNotNull(token);
        assertEquals("sampleToken", token);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    public void testResetPassword() {
        when(tokenRepository.findByToken("sampleToken")).thenReturn(testToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        passwordResetService.resetPassword("sampleToken", "newPassword");

        verify(userRepository, times(1)).save(testUser);
        verify(tokenRepository, times(1)).delete(testToken);
    }
}
