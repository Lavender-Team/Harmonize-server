package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 사용자 생성
    public User create(String loginId, String password, String email, String nickname,
            String role, String gender, Integer age) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(password)); // 비밀번호 암호화
        user.setEmail(email);
        user.setNickname(nickname);
        user.setRole(User.Role.valueOf(role.toUpperCase()));
        user.setGender(User.Gender.valueOf(gender.toUpperCase()));
        user.setAge(age);
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setIsDeleted(false);
        user.setIsBanned(false);
        user.setIsLocked(false);

        return userRepository.save(user);
    }

    // 사용자 수정
    @Transactional
    public void update(Long userId, String email, String nickname, String role, String gender, Integer age) {
        User user = userRepository.findById(userId).orElseThrow();
        if (email != null)
            user.setEmail(email);
        if (nickname != null)
            user.setNickname(nickname);
        if (role != null)
            user.setRole(User.Role.valueOf(role.toUpperCase()));
        if (gender != null)
            user.setGender(User.Gender.valueOf(gender.toUpperCase()));
        if (age != null)
            user.setAge(age);

        userRepository.save(user);
    }

    // 사용자 삭제
    @Transactional
    public void delete(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setIsDeleted(true);
        user.setDeletedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }

    // 사용자 상세정보 조회 (어드민)
    public User readByAdmin(Long userId) {
        return userRepository.findById(userId).orElseThrow();
    }

    // 사용자 목록 조회
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // 사용자 닉네임 검색
    public Page<User> search(String nickname, Pageable pageable) {
        return userRepository.findByNicknameContaining(nickname, pageable);
    }
}
