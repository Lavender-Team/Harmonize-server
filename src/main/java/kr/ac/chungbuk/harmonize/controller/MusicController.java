package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.MusicDTO;
import kr.ac.chungbuk.harmonize.dto.MusicListDTO;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
public class MusicController {

    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    // 음악 생성
    @PostMapping(path = "/api/music")
    public ResponseEntity<String> create(String title, String genre, String karaokeNum, String releaseDate,
            String playLink, MultipartFile albumCover,
            @RequestParam(value = "themes", defaultValue = "") List<String> themes) {
        try {
            musicService.create(title, genre, albumCover, karaokeNum, LocalDateTime.parse(releaseDate), playLink,
                    themes);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 생성 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 음악 수정
    @PutMapping(path = "/api/music/{musicId}")
    public ResponseEntity<String> update(@PathVariable Long musicId, String title, String genre, String karaokeNum,
            String releaseDate, String playLink, MultipartFile albumCover,
            @RequestParam(value = "themes", required = false) List<String> themes) {
        try {
            musicService.update(musicId, title, genre, albumCover, karaokeNum,
                    (releaseDate != null) ? LocalDateTime.parse(releaseDate) : null, playLink, themes);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 편집 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 음악 삭제
    @DeleteMapping(path = "/api/music/{musicId}")
    public ResponseEntity<String> delete(@PathVariable Long musicId) {
        try {
            musicService.delete(musicId);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 삭제 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 음악 벌크 업로드
    @PostMapping("/api/music/bulk")
    public ResponseEntity<String> createBulk(MultipartFile bulkFile) {
        try {
            musicService.createBulk(bulkFile);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 벌크 업로드 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 음악 상세정보 조회 (어드민)
    @GetMapping("/api/music/{musicId}")
    @ResponseBody
    public MusicDTO readByAdmin(@PathVariable Long musicId) {
        try {
            Music music = musicService.readByAdmin(musicId);
            return MusicDTO.build(music);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 음악 앨범커버 파일 다운로드
    @GetMapping("/api/music/albumcover/{filename}")
    public ResponseEntity<FileSystemResource> getAlbumcover(@PathVariable String filename) throws Exception {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/albumcover/" + filename;

        if (new File(path).exists()) {
            return getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    private ResponseEntity<FileSystemResource> getFileSystemResource(String filename, String path) throws IOException {
        FileSystemResource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(Path.of(path))))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                        new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"")
                .body(resource);
    }

    // 음악 목록 조회
    @GetMapping(path = "/api/music")
    @ResponseBody
    public Page<MusicListDTO> list(@RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "musicId"));

            Page<Music> list = musicService.list(pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDTO::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 전체 테마 목록 조회
    @GetMapping(path = "/api/music/theme")
    @ResponseBody
    public Page<Theme> listThemes(@RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize);

            return musicService.listThemes(pageable);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 특정 테마의 음악 목록 조회
    @GetMapping(path = "/api/music/theme/music")
    @ResponseBody
    public Page<MusicListDTO> listMusicOfTheme(
            @RequestParam(required = true) String themeName,
            @RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "musicId"));

            Page<Music> list = musicService.listMusicOfTheme(pageable, themeName);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDTO::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
