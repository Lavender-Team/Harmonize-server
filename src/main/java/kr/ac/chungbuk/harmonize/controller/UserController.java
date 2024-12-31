package kr.ac.chungbuk.harmonize.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateAdminDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateDto;
import kr.ac.chungbuk.harmonize.dto.response.UserDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.entity.UserAnalysis;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.service.EmailService;
import kr.ac.chungbuk.harmonize.service.PasswordResetService;
import kr.ac.chungbuk.harmonize.service.UserService;
import kr.ac.chungbuk.harmonize.exception.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.Security;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final PasswordResetService passwordResetService;

    // 사용자 생성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Map<String, Object> createUser(@Validated UserSaveDto userParam, BindingResult bindingResult)
            throws MethodArgumentNotValidException {

        // 아이디 및 이메일 중복 검사
        checkLoginIdAndEmailDuplicate(userParam, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new MethodArgumentNotValidException(null, bindingResult);
        }

        userService.create(userParam);

        /* 로그인 정보 전송 */
        String token = userService.tryLogin(userParam.getLoginId(), userParam.getPassword());
        User logginedUser = userService.getUserByLoginId(userParam.getLoginId());

        // 로그인 성공 응답
        HashMap<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", logginedUser.getRole());

        // 기타 정보 포함
        result.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        result.put("userId", logginedUser.getUserId());
        result.put("nickname", logginedUser.getNickname());

        return result;
    }

    // 사용자 수정 (사용자)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{userId}")
    public void updateUser(@PathVariable Long userId, @Validated UserUpdateDto userParam,
                                         BindingResult bindingResult/*, @AuthenticationPrincipal User user*/)
            throws MethodArgumentNotValidException {
        checkErrorsOnUpdate(userId, userParam, bindingResult);

        userService.update(userId, userParam);
    }

    // 사용자 수정 : (어드민 전용)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/admin/{userId}")
    public void updateUserByAdmin(@PathVariable Long userId, @Validated UserUpdateAdminDto userParam,
                                                BindingResult bindingResult) throws MethodArgumentNotValidException {
        checkErrorsOnUpdate(userId, userParam, bindingResult);

        userService.updateByAdmin(userId, userParam);
    }

    // 사용자 삭제
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
    }

    // 사용자 상세정보 조회 (본인 또는 어드민)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{userId}")
    public UserDto readByAdmin(@PathVariable Long userId, User user) throws IllegalAccessException {
        // 권한 검증
        if (!Objects.equals(user.getUserId(), userId) && user.getRole() != Role.ADMIN) {
            log.info("Unauthorized access attempt - User: {}, Requested userId: {}", user.getUserId(), userId);
            throw new IllegalAccessException();
        }

        User readUser = userService.read(userId);
        return UserDto.build(readUser);
    }

    // 사용자 목록 조회
    @ResponseBody
    @GetMapping
    public Page<UserDto> list(String query,
                              @PageableDefault(sort = "userId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> list;
        if (query == null || query.isEmpty())
            list = userService.list(pageable);
        else
            list = userService.search(query, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(UserDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity login(final HttpServletRequest req,
                                final HttpServletResponse res,
                                @RequestBody Map<String, String> request) throws Exception {
        try {
            String token = userService.tryLogin(request.get("loginId"), request.get("password"));
            Cookie tokenCookie = createTokenCookie(token, 168 * 60 * 60);
            res.addCookie(tokenCookie);

            User logginedUser = userService.getUserByLoginId(request.get("loginId"));

            // 로그인 성공 응답
            HashMap<String, Object> result = new HashMap<>();
            result.put("result", "로그인에 성공하였습니다.");
            result.put("token", token); // 토큰도 포함하여 응답
            result.put("role", logginedUser.getRole());

            // 기타 정보 포함
            result.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            result.put("userId", logginedUser.getUserId());
            result.put("nickname", logginedUser.getNickname());
            result.put("gender", Gender.toString(logginedUser.getGender()));
            result.put("age", logginedUser.getAge());
            result.put("genre", logginedUser.getGenre());

            Optional<UserAnalysis> userAnalysis = logginedUser.getLatestAnalysis();
            if (userAnalysis.isPresent()) {
                result.put("highestPitch", userAnalysis.get().getHighestPitch());
                result.put("lowestPitch", userAnalysis.get().getLowestPitch());
            }

            return new ResponseEntity(result, HttpStatus.OK);

        } catch(IllegalArgumentException e) {
            // 비밀번호가 틀렸을 때
            User user = userService.getUserByLoginId(request.get("loginId"));

            int failedAttempts = user.getAttempt().getAttempts();
            int remainingAttempts = 10 - failedAttempts;

            // 로그인 실패 응답
            HashMap<String, Object> result = new HashMap<>();
            result.put("result", "아이디 또는 비밀번호가 잘못되었습니다.");
            result.put("failedAttempts", failedAttempts);
            result.put("remainingAttempts", remainingAttempts);
            return new ResponseEntity(result, HttpStatus.BAD_REQUEST);

        } catch(Exception e) {
            // 그 외의 오류 처리
            Cookie tokenCookie = createTokenCookie(null, 0);
            res.addCookie(tokenCookie);

            HashMap<String, Object> result = new HashMap<>();
            result.put("result", "아이디 또는 비밀번호가 잘못되었습니다.");
            return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
        }
    }

    // 로그아웃
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/logout")
    public Map<String, Object> logout(final HttpServletRequest req, final HttpServletResponse res) {
        Cookie tokenCookie = createTokenCookie(null, 0);
        res.addCookie(tokenCookie);

        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "로그아웃에 성공하였습니다.");
        return result;
    }

    // 로그인된 사용자 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/auth/currentuser")
    public Map<String, Object> getCurrentUserData() {
        HashMap<String, Object> result = new HashMap<>();

        String loginId = Security.getCurrentloginId();

        result.put("loginId", loginId);
        result.put("Authorities", Security.getCurrentUserRole());

        try {
            User currentUser = (User)userService.loadUserByUsername(loginId);
            result.put("role", currentUser.getRole());
            result.put("email", currentUser.getEmail());
            result.put("loginId", currentUser.getLoginId());
            result.put("nickname", currentUser.getNickname());
        } catch (Exception e){
            // 로그인되지 않았거나 오류난 경우
        }

        return result;
    }


    private Cookie createTokenCookie(String token, int age) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(age);
        cookie.setPath("/");
        return cookie;
    }

    // 아이디 및 이메일 중복 검사
    private void checkLoginIdAndEmailDuplicate(UserSaveDto userParam, BindingResult bindingResult) {
        if (!userParam.getLoginId().isBlank() && userService.existsByLoginId(userParam.getLoginId()))
            bindingResult.rejectValue("loginId", "Duplicated.loginId");
        if (!userParam.getEmail().isBlank() && userService.existsByEmail(userParam.getEmail()))
            bindingResult.rejectValue("email", "Duplicated.email");
    }

    // 사용자 수정의 검증 결과 확인 메서드 (update와 updateByAdmin에 중복되어 분리)
    private void checkErrorsOnUpdate(Long userId, UserUpdateDto userParam, BindingResult bindingResult)
            throws MethodArgumentNotValidException {

        // 이메일 중복 검사
        if (!userParam.getEmail().isBlank() && userService.existsByEmail(userId, userParam.getEmail()))
            bindingResult.rejectValue("email", "duplicated.email");
        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            throw new MethodArgumentNotValidException(null, bindingResult);
        }
    }

    // 전체 사용자 수 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count")
    public Map<String, Integer> countUsers() {
        int count = userService.countByIsDeletedFalse();  // is_deleted = 0 인 회원 카운트
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return response;
    }

    // 아이디 찾기
    @PostMapping(value = "/find-id")
    public ResponseEntity<?> findId(@RequestParam Map<String, String> request) {
        String email = request.get("email");
        try {
            userService.sendIdByEmail(email);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "등록되지 않은 이메일입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "아이디 찾기 중 오류가 발생했습니다."));
        }
    }

    // 비밀번호 재설정 요청
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam Map<String, String> request) {
        String loginId = request.get("loginId");
        String email = request.get("email");

        try {
            User user = userService.findByLoginIdAndEmail(loginId, email); // 서비스에서 사용자 조회
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "아이디와 이메일이 일치하지 않습니다."));
            }

            // 토큰 생성 및 이메일 전송
            String token = passwordResetService.createToken(user);
            emailService.sendPasswordResetLink(email, token);

            return ResponseEntity.ok(Collections.singletonMap("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
        } catch (Exception e) {
            log.error("비밀번호 재설정 요청 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "비밀번호 재설정 요청 중 오류가 발생했습니다."));
        }
    }


    // 비밀번호 재설정 처리
    @PostMapping("/reset-password/confirm")
    public ResponseEntity<?> confirmResetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "비밀번호 재설정 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/get-user-by-token/{token}")
    public ResponseEntity<?> getUserByToken(@PathVariable String token) {
        try {
            User user = passwordResetService.getUserByToken(token);
            return ResponseEntity.ok(Collections.singletonMap("loginId", user.getLoginId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}
