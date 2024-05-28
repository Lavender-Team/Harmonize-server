package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class MusicController {

    private MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }


    // 음악 생성
    @PostMapping(path = "/api/music")
    public ResponseEntity<String> create(String title, String genre, String karaokeNum, String releaseDate,
                                         String playLink, MultipartFile albumCover) {
        try {
            musicService.create(title, genre, "", karaokeNum, LocalDateTime.parse(releaseDate), playLink);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 생성 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
