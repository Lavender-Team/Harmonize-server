package kr.ac.chungbuk.harmonize.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateAdminDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateDto;
import kr.ac.chungbuk.harmonize.dto.response.UserDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.UserService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.entity.Attempt;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.repository.AttemptRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.security.JwtTokenProvider;
import kr.ac.chungbuk.harmonize.security.UserAuthentication;
import kr.ac.chungbuk.harmonize.utility.Security;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.*;

@Slf4j
@Controller
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    // 사용자 생성
    @PostMapping
    public ResponseEntity<Object> create(@Validated UserSaveDto userParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        } else {
            // 아이디 및 이메일 중복 검사
            checkLoginIdAndEmailDuplicate(userParam, bindingResult);
            if (bindingResult.hasErrors()) {
                ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
            }
        }

        try {
            userService.create(userParam);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("createFailed.user", messageSource, Locale.getDefault())
            );
        }
    }

    // 사용자 수정 (사용자)
    @PutMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId, @Validated UserUpdateDto userParam,
                                         BindingResult bindingResult) {

        ResponseEntity<Object> BAD_REQUEST = checkErrorsOnUpdate(userId, userParam, bindingResult);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        try {
                userService.update(userId, userParam);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.user", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("updateFailed.user", messageSource, Locale.getDefault())
            );
        }
    }

    // 사용자 수정 : (어드민 전용)
    @PutMapping("/admin/{userId}")
    public ResponseEntity<Object> updateByAdmin(@PathVariable Long userId, @Validated UserUpdateAdminDto userParam,
                                                BindingResult bindingResult) {

        ResponseEntity<Object> BAD_REQUEST = checkErrorsOnUpdate(userId, userParam, bindingResult);
        if (BAD_REQUEST != null) return BAD_REQUEST;

        try {
            userService.updateByAdmin(userId, userParam);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.user", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("updateFailed.user", messageSource, Locale.getDefault())
            );
        }
    }

    // 사용자 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        try {
            userService.delete(userId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.user", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("deleteFailed.user", messageSource, Locale.getDefault())
            );
        }
    }

    // 사용자 상세정보 조회 (본인 또는 어드민)
    @GetMapping("/{userId}")
    @ResponseBody
    public UserDto readByAdmin(@PathVariable Long userId) {
        try {
            User user = userService.read(userId);
            return UserDto.build(user);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 사용자 목록 조회
    @GetMapping
    @ResponseBody
    public Page<UserDto> list(String nickname,
                              @PageableDefault(sort = "userId", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<User> list;

            if (nickname == null || nickname.isEmpty())
                list = userService.list(pageable);
            else
                list = userService.search(nickname, pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(UserDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity login(final HttpServletRequest req,
                                final HttpServletResponse res,
                                @RequestBody Map<String, String> request) throws Exception {
        try {
            String token = userService.tryLogin(request.get("loginId"), request.get("password"));
            Cookie tokenCookie = createTokenCookie(token, 168 * 60 * 60);
            res.addCookie(tokenCookie);

            // 로그인 성공 응답
            HashMap<String, Object> result = new HashMap<>();
            result.put("result", "로그인에 성공하였습니다.");
            result.put("token", token); // 토큰도 포함하여 응답
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

    @GetMapping(path = "/logout")
    public ResponseEntity logout(final HttpServletRequest req, final HttpServletResponse res) {
        Cookie tokenCookie = createTokenCookie(null, 0);
        res.addCookie(tokenCookie);

        HashMap<String, Object> result = new HashMap<>();
        result.put("result", "로그아웃에 성공하였습니다.");
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping(path = "/auth/currentuser")
    public ResponseEntity getCurrentUserData() {
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

        return new ResponseEntity(result, HttpStatus.OK);
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
        if (userService.existsByLoginId(userParam.getLoginId()))
            bindingResult.rejectValue("loginId", "duplicated.loginId");
        if (userService.existsByEmail(userParam.getEmail()))
            bindingResult.rejectValue("email", "duplicated.email");
    }

    // 사용자 수정의 검증 결과 확인 메서드 (update와 updateByAdmin에 중복되어 분리)
    private ResponseEntity<Object> checkErrorsOnUpdate(Long userId, UserUpdateDto userParam, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        } else {
            // 이메일 중복 검사
            if (userParam.getEmail() != null && userService.existsByEmail(userId, userParam.getEmail()))
                bindingResult.rejectValue("email", "duplicated.email");
            if (bindingResult.hasErrors()) {
                ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
            }
        }
        return null;
    }

    private UserRepository userRepository;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> countUsers() {
        int count = userRepository.countByIsDeletedFalse();  // is_deleted = 0 인 회원 카운트
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

}
