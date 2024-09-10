package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateAdminDto;
import kr.ac.chungbuk.harmonize.dto.request.UserUpdateDto;
import kr.ac.chungbuk.harmonize.dto.response.UserDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.UserService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
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
}
