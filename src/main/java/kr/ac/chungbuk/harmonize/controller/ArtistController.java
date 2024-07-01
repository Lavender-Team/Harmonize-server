package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
public class ArtistController {

    private final ArtistService artistService;

    @Autowired
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    // 가수 등록
    @PostMapping(path = "/api/artist")
    public ResponseEntity<String> create(@RequestParam String artistName,
            @RequestParam String gender,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam String activityPeriod,
            @RequestParam String nation,
            @RequestParam String agency) {
        try {
            artistService.create(artistName, gender, profileImage, activityPeriod, nation, agency);
            return ResponseEntity.status(HttpStatus.CREATED).body("Artist created successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create artist");
        }
    }

    // 가수 삭제
    @DeleteMapping(path = "/api/artist/{artistId}")
    public ResponseEntity<String> delete(@PathVariable Long artistId) {
        Optional<Artist> artist = artistService.findById(artistId);
        if (artist.isPresent()) {
            try {
                artistService.delete(artistId);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Artist deleted successfully");
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete artist");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artist not found");
        }
    }

    // 가수 목록 조회
    @GetMapping(path = "/api/artist")
    @ResponseBody
    public Page<Artist> list(@RequestParam(required = false, defaultValue = "0", value = "page") int pageNo,
            @RequestParam(required = false, defaultValue = "10", value = "size") int pageSize) {
        return artistService.list(pageNo, pageSize);
    }

    // 가수 수정
    @PutMapping(path = "/api/artist/{artistId}")
    public ResponseEntity<String> update(@PathVariable Long artistId,
            @RequestParam(required = false) String artistName,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) MultipartFile profileImage,
            @RequestParam(required = false) String activityPeriod,
            @RequestParam(required = false) String nation,
            @RequestParam(required = false) String agency) {
        try {
            artistService.update(artistId, artistName, gender, profileImage, activityPeriod, nation, agency);
            return ResponseEntity.status(HttpStatus.OK).body("Artist updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update artist");
        }
    }
}
