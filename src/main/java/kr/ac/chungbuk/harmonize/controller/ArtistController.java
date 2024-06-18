package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.MusicListDTO;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ArtistController {

    private final ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    // 가수 등록
    @PostMapping(path = "/api/artist")
    public ResponseEntity<String> create() {
        // TODO 가수 등록 기능

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 가수 삭제
    @DeleteMapping(path = "/api/artist/{artistId}")
    public ResponseEntity<String> delete() {
        // TODO 가수 삭제 기능

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 가수 목록 조회
    @GetMapping(path = "/api/artist")
    @ResponseBody
    public Page<MusicListDTO> list(@RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
                                   @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        // TODO 가수 목록 조회 기능

        return null;
    }
}
