package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<String> create(@RequestParam("artistName") String artistName,
            @RequestParam("gender") String gender,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam("activityPeriod") String activityPeriod,
            @RequestParam("nation") String nation,
            @RequestParam("agency") String agency) {
        try {
            artistService.create(artistName, gender, profileImage, activityPeriod, nation, agency);
            return ResponseEntity.status(HttpStatus.CREATED).body("Artist created successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create artist: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete artist: " + e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
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
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return artistService.list(pageable);
    }

    // 가수 수정
    @PutMapping(path = "/api/artist/{artistId}")
    public ResponseEntity<String> update(@PathVariable Long artistId,
            @RequestParam(value = "artistName", required = false) String artistName,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "activityPeriod", required = false) String activityPeriod,
            @RequestParam(value = "nation", required = false) String nation,
            @RequestParam(value = "agency", required = false) String agency) {
        try {
            artistService.update(artistId, artistName, gender, profileImage, activityPeriod, nation, agency);
            return ResponseEntity.status(HttpStatus.OK).body("Artist updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update artist: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
        }
    }
}
