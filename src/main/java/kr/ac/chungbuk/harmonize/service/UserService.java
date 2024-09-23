package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateAdminDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateDto;
import kr.ac.chungbuk.harmonize.entity.Attempt;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.repository.AttemptRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.security.JwtTokenProvider;
import kr.ac.chungbuk.harmonize.security.UserAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AttemptRepository attemptRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       AttemptRepository attemptRepository,
                       PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        User user = (User) userRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("User not present"));
        return user;
    }

    // 사용자 생성
    public User create(UserSaveDto userParam) {
        User user = new User();
        user.setLoginId(userParam.getLoginId());
        user.setPassword(passwordEncoder.encode(userParam.getPassword())); // 비밀번호 암호화
        user.setEmail(userParam.getEmail());
        user.setNickname(userParam.getNickname());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setIsDeleted(false);
        user.setIsBanned(false);
        user.setIsLocked(false);

        // 관리자 페이지에서 회원가입시에만 실행
        if (userParam.getGender() != null)
            user.setGender(Gender.fromString(userParam.getGender()));
        if (userParam.getAge() != null)
            user.setAge(userParam.getAge());

        user = userRepository.save(user);

        // Attempt 객체도 함께 생성
        Attempt attempt = new Attempt();
        attempt.setUser(user);  // Attempt와 User 간의 연관관계 설정
        user.setAttempt(attempt);

        attemptRepository.save(attempt);  // Attempt 저장

        return user;
    }

    // 사용자 수정
    @Transactional
    public void update(Long userId, UserUpdateDto userParam) {
        User user = userRepository.findById(userId).orElseThrow();
        if (userParam.getPassword() != null)
            user.setPassword(passwordEncoder.encode(userParam.getPassword()));
        if (userParam.getEmail() != null)
            user.setEmail(userParam.getEmail());
        if (userParam.getNickname() != null)
            user.setNickname(userParam.getNickname());
        if (userParam.getGender() != null)
            user.setGender(Gender.fromString(userParam.getGender()));
        if (userParam.getAge() != null)
            user.setAge(userParam.getAge());
        if (userParam.getGenre() != null) {
            user.getGenre().clear();
            List<Genre> genres = userParam.getGenre().stream().map(Genre::fromString).toList();
            for (Genre genre : genres)
                user.getGenre().add(genre);
        }

        userRepository.save(user);
    }

    // 사용자 수정 : 관리자 전용
    @Transactional
    public void updateByAdmin(Long userId, UserUpdateAdminDto userParam) {
        update(userId, userParam);

        User user = userRepository.findById(userId).orElseThrow();
        if (userParam.getRole() != null)
            user.setRole(Role.valueOf(userParam.getRole().toUpperCase()));
        if (userParam.getIsDeleted() != null) {
            user.setIsDeleted(userParam.getIsDeleted());
            if (userParam.getIsDeleted())
                user.setDeletedAt(LocalDateTime.now());
            else
                user.setDeletedAt(null);
        }
        if (userParam.getIsBanned() != null)
            user.setIsBanned(userParam.getIsBanned());
        if (userParam.getIsLocked() != null)
            user.setIsLocked(userParam.getIsLocked());

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

    // 사용자 상세정보 조회 (본인 또는 어드민)
    public User read(Long userId) {
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

    // 아이디 중복 여부 검사
    public Boolean existsByLoginId(String loginId) {
        if (loginId.equals("anonymousUser"))
            return true;
        return userRepository.existsByLoginId(loginId);
    }

    // 이메일 중복 여부 검사
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 본인 아이디를 제외한 이메일 중복 여부 검사
    public Boolean existsByEmail(Long userId, String email) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent())
            return !user.get().getEmail().equals(email) &&
                    userRepository.existsByEmail(email); // 본인의 이메일과 다르면서 이미 존재하면 true 반환
        else
            return false; // userId에 해당하는 사용자가 존재하지 않으면 false 반환
    }

    public String tryLogin(String loginId, String password) throws UsernameNotFoundException {
        // 유저 검색
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not present"));

        // 유저 상태 확인
        if (user.getIsBanned() || user.getIsLocked() || user.getIsDeleted()) {
            throw new UsernameNotFoundException("User not present");
        }

        // Attempt 객체가 null일 경우 새로 생성
        if (user.getAttempt() == null) {
            Attempt newAttempt = new Attempt();
            newAttempt.setUser(user);  // User와 Attempt 간의 연관 관계 설정
            user.setAttempt(newAttempt);
            attemptRepository.save(newAttempt);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.debug("Password not matched for user: " + loginId);

            int failed = user.getAttempt().getAttempts();

            // 실패 시도 증가
            user.getAttempt().setAttempts(failed + 1);

            // 10회 이상 시 계정 잠금
            if (user.getAttempt().getAttempts() >= 10) {
                this.lock(user);  // 계정 잠금
            }

            userRepository.save(user);
            attemptRepository.save(user.getAttempt());

            throw new IllegalArgumentException("Password not matched");
        }

        // 로그인 성공 시 실패 시도 초기화
        if (user.getAttempt().getAttempts() != 0) {
            user.getAttempt().setAttempts(0);
            userRepository.save(user);
            attemptRepository.save(user.getAttempt());
        }

        log.debug("User " + loginId + " logged in successfully.");

        Authentication authentication = new UserAuthentication(loginId, password, user.getAuthorities());
        return JwtTokenProvider.generateToken(authentication);
    }

    public User getUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not present"));
    }

    public boolean canUseAsloginId(String loginId) {
        return !userRepository.existsByLoginId(loginId) && !loginId.equals("anonymousUser");
    }

    public int countByIsDeletedFalse() {
        return (int) userRepository.countByIsDeletedFalse();
    }

    public void lock(User user) {
        user.setIsLocked(true);
        userRepository.save(user);
    }

    public void ban(User user) {
        user.setIsBanned(true);
        userRepository.save(user);
    }
}
