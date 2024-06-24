package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.MusicAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@Slf4j
public class MusicAnalysisController {

    private final MusicAnalysisService musicAnalysisService;

    @Autowired
    public MusicAnalysisController(MusicAnalysisService musicAnalysisService) {
        this.musicAnalysisService = musicAnalysisService;
    }


    // 음악 파일 및 가사 파일 업로드
    @PostMapping(path = "/api/music/{musicId}/files")
    public ResponseEntity<String> updateFiles(@PathVariable Long musicId, MultipartFile audioFile,
                                              MultipartFile lyricFile) {
        try {
            musicAnalysisService.updateFiles(musicId, audioFile, lyricFile);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 업로드 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 음악 파일 다운로드
    @GetMapping(path = "/api/music/audio/{filename}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/audio/" + filename;

        if (new File(path).exists()) {
            FileSystemResource resource = new FileSystemResource(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(Files.probeContentType(Path.of(path))))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                            new String(filename.getBytes("UTF-8"), "ISO-8859-1") + "\"")
                    .body(resource);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }



}
