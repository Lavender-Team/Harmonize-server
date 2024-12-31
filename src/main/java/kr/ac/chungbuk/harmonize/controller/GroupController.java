package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.response.GroupDto;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.service.GroupService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createGroup(@Validated GroupRequestDto groupParam) throws IOException {
        groupService.create(groupParam);
    }

    // 그룹 수정
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{groupId}")
    public void updateGroup(@PathVariable Long groupId, @Validated GroupRequestDto groupParam)
            throws IOException {

        groupService.update(groupId, groupParam);
    }
    
    // 그룹 삭제
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/{groupId}")
    public void deleteGroup(@PathVariable Long groupId) throws Exception {
        groupService.delete(groupId);
    }
    
    // 그룹 목록 조회
    @ResponseBody
    @GetMapping
    public Page<GroupDto> listGroup(
            String groupName,
            @PageableDefault(sort = "groupId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Group> list;
        if (groupName == null || groupName.isEmpty())
            list = groupService.list(pageable);
        else
            list = groupService.search(groupName, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(GroupDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 그룹 상세정보 조회
    @ResponseBody
    @GetMapping("/{groupId}")
    public GroupDto readGroup(@PathVariable Long groupId) {
        Group group = groupService.findById(groupId);
        return GroupDto.build(group);
    }

    // 그룹 프로필 이미지 파일 다운로드
    @GetMapping("/profile/{filename}")
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
