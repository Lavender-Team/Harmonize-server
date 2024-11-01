package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.PasswordResetToken;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.repository.PasswordResetTokenRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 토큰 생성 메서드
    public String createToken(User user) {
        // 기존 토큰 삭제
        PasswordResetToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null) {
            tokenRepository.delete(existingToken);
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        tokenRepository.save(resetToken);
        return token;
    }

    // 비밀번호 재설정 메서드
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("유효하지 않은 또는 만료된 토큰입니다.");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 사용된 토큰은 삭제하여 폐기 처리
        tokenRepository.delete(resetToken);
    }

    // 토큰에 해당하는 사용자 조회
    public User getUserByToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("유효하지 않은 또는 만료된 토큰입니다.");
        }
        return resetToken.getUser();
    }
}
