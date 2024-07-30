package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.service.MusicAnalysisService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

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
        } catch (SizeLimitExceededException e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("가사 파일의 용량이 너무 큽니다.");
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 업로드 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    // 앨범 커버, 음악, 가사 파일 업로드 (벌크 업로드: 파일 이름으로 음악 조회)
    @PostMapping(path = "/api/music/bulk/files")
    public ResponseEntity<String> updateFiles(MultipartFile albumCover, MultipartFile audioFile,
                                              MultipartFile lyricFile) throws Exception {
        String musicTitle = "";

        try {
            if (albumCover != null) {
                musicTitle = getMusicTitle(albumCover);
                musicAnalysisService.updateAlbumCover(albumCover);
            }
            else if (audioFile != null) {
                musicTitle = getMusicTitle(audioFile);
                musicAnalysisService.updateAudioFile(audioFile);
            }
            else if (lyricFile != null) {
                musicTitle = getMusicTitle(lyricFile);
                musicAnalysisService.updateLyricFile(lyricFile);
            }

        } catch (NoSuchElementException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "제목이 일치하는 곡이 없음", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 이름과 제목이 일치하는 곡이 없습니다.");
        } catch (IncorrectResultSizeDataAccessException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "같은 제목 곡 두 개 이상", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 이름과 제목이 일치하는 곡이 두 개 이상 존재합니다.");
        } catch (SizeLimitExceededException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog(musicTitle, "가사 용량 너무 큼", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("가사 파일의 용량이 너무 큽니다.");
        } catch (Exception e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog(musicTitle, "파일 관련 오류 발생", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("파일 업로드 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    private String getMusicTitle(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        return originalFilename.substring(0, originalFilename.lastIndexOf("."));
    }

    // 음악 분석 요청 전송
    @PostMapping(path = "/api/music/{musicId}/analyze")
    public ResponseEntity<String> analyze(@PathVariable Long musicId, Double confidence) {
        try {
            musicAnalysisService.analyze(musicId, confidence);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("음악 분석 요청 전송 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // 음악 분석 특정 Pitch 값 제거 요청 전송
    @PostMapping(path = "/api/music/{musicId}/delete")
    public ResponseEntity<String> deletePitch(@PathVariable Long musicId, Double time) {
        try {
            musicAnalysisService.deletePitch(musicId, time);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pitch 삭제 요청 전송 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // 음악 파일 다운로드
    @GetMapping(path = "/api/music/audio/{filename}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/audio/" + filename;

        if (new File(path).exists()) {
            return getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    // Pitch 그래프 파일 다운로드
    @GetMapping(path = "/api/music/pitch/{musicId}")
    public ResponseEntity<FileSystemResource> getPitchGraphFile(@PathVariable Long musicId) throws IOException {

        String path = System.getProperty("user.dir") + "/upload/audio/" + musicId + "/pitch.xlsx";

        if (new File(path).exists()) {
            return getFileSystemResource("pitch.xlsx", path);
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

}
