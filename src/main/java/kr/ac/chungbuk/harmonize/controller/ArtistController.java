package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.ArtistRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.dto.response.ArtistDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import kr.ac.chungbuk.harmonize.service.GroupService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/artist")
public class ArtistController {

    private final ArtistService artistService;
    private final GroupService groupService;
    private final FileHandler fileHandler;

    // 가수 등록
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createArtist(@Validated ArtistRequestDto artistParam) throws IOException {
        Artist created = artistService.create(artistParam);

        // 솔로 그룹을 생성하도록 요청시
        if (BooleanUtils.isTrue(artistParam.getCreateSoloGroup())) {
            groupService.create(GroupRequestDto.convertFrom(created));
        }
    }

    // 가수 삭제
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/{artistId}")
    public void deleteArtist(@PathVariable Long artistId) throws IOException {
        artistService.delete(artistId);
    }

    // 가수 목록 조회
    @ResponseBody
    @GetMapping
    public Page<ArtistDto> listArtist(
            String artistName,
            @PageableDefault(sort = "artistId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Artist> list;
        if (artistName == null || artistName.isEmpty())
            list = artistService.list(pageable);
        else
            list = artistService.search(artistName, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(ArtistDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 가수 수정
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping("/{artistId}")
    public void updateArtist(@PathVariable Long artistId,
                                         @Validated ArtistRequestDto artistParam) throws IOException {
        artistService.update(artistId, artistParam);
    }

    // 가수 상세정보 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{artistId}")
    public ArtistDto readArtist(@PathVariable Long artistId) {
        Artist artist = artistService.read(artistId);
        return ArtistDto.build(artist);
    }

    // 가수 프로필 이미지 파일 다운로드
    @GetMapping("/profile/{filename}")
    public ResponseEntity<FileSystemResource> getProfileImage(@PathVariable String filename) throws Exception {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = fileHandler.getProfileDirectoryPath() + filename;

        if (new File(path).exists()) {
            return fileHandler.getFileSystemResource(filename, path);
        } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    // 전체 가수 수 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count")
    public Map<String, Integer> countArtists() {
        int count = artistService.count();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return response;
    }
}
