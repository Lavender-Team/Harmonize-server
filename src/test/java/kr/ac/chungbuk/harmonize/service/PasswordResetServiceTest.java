package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.PasswordResetToken;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.repository.PasswordResetTokenRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 초기 User와 PasswordResetToken 설정
        testUser = new User("testLoginId", "testPassword", "test@example.com", "testNickname", null, null, 25);
        testToken = new PasswordResetToken();
        testToken.setToken(UUID.randomUUID().toString());
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
    }

    @Test
    public void testCreateToken() {
        when(tokenRepository.findByUser(any(User.class))).thenReturn(null);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        String token = passwordResetService.createToken(testUser);

        assertNotNull(token);
        assertEquals(testToken.getToken(), token);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    public void testResetPassword_Success() {
        String newPassword = "newPassword123";
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(testToken);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        passwordResetService.resetPassword(testToken.getToken(), newPassword);

        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(testUser);
        assertEquals("encodedPassword", testUser.getPassword());
        verify(tokenRepository, times(1)).delete(testToken);
    }

    @Test
    public void testResetPassword_InvalidToken() {
        String invalidToken = "invalidToken";
        when(tokenRepository.findByToken(invalidToken)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.resetPassword(invalidToken, "newPassword"));
    }

    @Test
    public void testResetPassword_ExpiredToken() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(testToken);

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.resetPassword(testToken.getToken(), "newPassword"));
    }

    @Test
    public void testGetUserByToken_Success() {
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(testToken);

        User user = passwordResetService.getUserByToken(testToken.getToken());

        assertNotNull(user);
        assertEquals(testUser, user);
    }

    @Test
    public void testGetUserByToken_InvalidToken() {
        String invalidToken = "invalidToken";
        when(tokenRepository.findByToken(invalidToken)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.getUserByToken(invalidToken));
    }

    @Test
    public void testGetUserByToken_ExpiredToken() {
        testToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(testToken.getToken())).thenReturn(testToken);

        assertThrows(IllegalArgumentException.class, () ->
                passwordResetService.getUserByToken(testToken.getToken()));
    }
}
