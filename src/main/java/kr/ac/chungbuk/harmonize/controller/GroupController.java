package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.response.GroupDto;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.service.GroupService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Locale;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Controller
@Slf4j
public class GroupController {

    private final GroupService groupService;
    private final MessageSource messageSource;

    @Autowired
    public GroupController(GroupService groupService, MessageSource messageSource) {
        this.groupService = groupService;
        this.messageSource = messageSource;
    }

    // 그룹 생성
    @PostMapping("/api/group")
    public ResponseEntity<Object> create(@Validated GroupRequestDto groupParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            groupService.create(groupParam);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("createFailed.group", messageSource, Locale.getDefault())
            );
        }
    }

    // 그룹 수정
    @PutMapping("/api/group/{groupId}")
    public ResponseEntity<Object> create(@PathVariable Long groupId, @Validated GroupRequestDto groupParam,
                                         BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            groupService.update(groupId, groupParam);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("notFound.group", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("updateFailed.group", messageSource, Locale.getDefault())
            );
        }
    }
    
    // 그룹 삭제
    @DeleteMapping("/api/group/{groupId}")
    public ResponseEntity<Object> delete(@PathVariable Long groupId) {
        try {
            groupService.delete(groupId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("notFound.group", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("deleteFailed.group", messageSource, Locale.getDefault())
            );
        }
    }
    
    // 그룹 목록 조회
    @GetMapping(path = "/api/group")
    @ResponseBody
    public Page<GroupDto> list(String groupName,
                               @PageableDefault(sort = "groupId", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Group> list;
            if (groupName == null || groupName.isEmpty())
                list = groupService.list(pageable);
            else
                list = groupService.search(groupName, pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(GroupDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 그룹 상세정보 조회
    @GetMapping("/api/group/{groupId}")
    @ResponseBody
    public GroupDto readByAdmin(@PathVariable Long groupId) {
        try {
            Group group = groupService.findById(groupId);
            return GroupDto.build(group);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 그룹 프로필 이미지 파일 다운로드
    @GetMapping("/api/group/profile/{filename}")
    public ResponseEntity<FileSystemResource> getProfileImage(@PathVariable String filename) throws Exception {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/group/profile/" + filename;

        if (new File(path).exists()) {
            return FileHandler.getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }
}
