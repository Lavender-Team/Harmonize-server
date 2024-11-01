package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.PasswordResetToken;
import kr.ac.chungbuk.harmonize.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
}
