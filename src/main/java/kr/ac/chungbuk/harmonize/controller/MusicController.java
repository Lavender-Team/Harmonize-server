package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.MusicListDTO;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
                                         String playLink, MultipartFile albumCover) {
        try {
            musicService.create(title, genre, albumCover, karaokeNum, LocalDateTime.parse(releaseDate), playLink);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 생성 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
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
                    list.getTotalElements()
            );
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
