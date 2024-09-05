package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.response.UserDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 사용자 생성
    @PostMapping(path = "/api/users")
    public ResponseEntity<String> create(String loginId, String password, String email, String nickname,
            String role, String gender, Integer age) {
        try {
            userService.create(loginId, password, email, nickname, role, gender, age);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 생성 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 사용자 수정
    @PutMapping(path = "/api/users/{userId}")
    public ResponseEntity<String> update(@PathVariable Long userId, String email, String nickname,
            String role, String gender, Integer age) {
        try {
            userService.update(userId, email, nickname, role, gender, age);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 수정 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 사용자 삭제
    @DeleteMapping(path = "/api/users/{userId}")
    public ResponseEntity<String> delete(@PathVariable Long userId) {
        try {
            userService.delete(userId);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("사용자 삭제 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 사용자 상세정보 조회 (어드민)
    @GetMapping("/api/users/{userId}")
    @ResponseBody
    public UserDto readByAdmin(@PathVariable Long userId) {
        try {
            User user = userService.readByAdmin(userId);
            return UserDto.build(user);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 사용자 목록 조회
    @GetMapping(path = "/api/users")
    @ResponseBody
    public Page<UserDto> list(String nickname,
                              @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
                              @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "userId"));

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
}
