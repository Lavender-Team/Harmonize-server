package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.GroupDTO;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.service.GroupService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

@Controller
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 그룹 생성
    @PostMapping("/api/group")
    public ResponseEntity<String> create(String groupName, String groupType, String agency, MultipartFile profileImage,
                                         @RequestParam(value = "artistIds", defaultValue = "") List<Long> artistIds) {
        try {
            groupService.create(groupName, groupType, agency, artistIds, profileImage);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("그룹 생성 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 그룹 수정
    @PutMapping("/api/group/{groupId}")
    public ResponseEntity<String> create(@PathVariable Long groupId, String groupName, String groupType, String agency,
                                         MultipartFile profileImage,
                                         @RequestParam(value = "artistIds", defaultValue = "") List<Long> artistIds) {
        try {
            groupService.update(groupId, groupName, groupType, agency, artistIds, profileImage);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("그룹 수정 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
    
    // 그룹 삭제
    @DeleteMapping("/api/group/{groupId}")
    public ResponseEntity<String> delete(@PathVariable Long groupId) {
        try {
            groupService.delete(groupId);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("그룹 삭제 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
    
    // 그룹 목록 조회
    @GetMapping(path = "/api/group")
    @ResponseBody
    public Page<GroupDTO> list(@RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
                               @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "groupId"));

            Page<Group> list = groupService.list(pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(GroupDTO::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
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
