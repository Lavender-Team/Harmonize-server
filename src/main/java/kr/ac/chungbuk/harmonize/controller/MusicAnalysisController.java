package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.MusicAnalysisService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Controller
@Slf4j
public class MusicAnalysisController {

    private final MusicAnalysisService musicAnalysisService;
    private final MessageSource messageSource;

    @Autowired
    public MusicAnalysisController(MusicAnalysisService musicAnalysisService, MessageSource messageSource) {
        this.musicAnalysisService = musicAnalysisService;
        this.messageSource = messageSource;
    }


    // 음악 파일 및 가사 파일 업로드
    @PostMapping(path = "/api/music/{musicId}/files")
    public ResponseEntity<Object> updateFiles(@PathVariable Long musicId, MultipartFile audioFile,
                                              MultipartFile lyricFile) {
        try {
            musicAnalysisService.updateFiles(musicId, audioFile, lyricFile);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (SizeLimitExceededException e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("sizeLimitFailed.lyricFile", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("uploadFailed.file", messageSource, Locale.getDefault())
            );
        }
    }

    // 앨범 커버, 음악, 가사 파일 업로드 (벌크 업로드: 파일 이름으로 음악 조회)
    @PostMapping(path = "/api/music/bulk/files")
    public ResponseEntity<Object> updateFiles(MultipartFile albumCover, MultipartFile audioFile,
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
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (NoSuchElementException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "제목이 일치하는 곡이 없음", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("noMatchedName.file", messageSource, Locale.getDefault())
            );
        } catch (IncorrectResultSizeDataAccessException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "같은 제목 곡 두 개 이상", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("duplicatedName.file", messageSource, Locale.getDefault())
            );
        } catch (SizeLimitExceededException e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog(musicTitle, "가사 용량 너무 큼", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("sizeLimitFailed.lyricFile", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.debug(e.getMessage());
            FileHandler.writeBulkUploadLog(musicTitle, "파일 관련 오류 발생", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("uploadFailed.file", messageSource, Locale.getDefault())
            );
        }
    }

    private String getMusicTitle(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        return originalFilename.substring(0, originalFilename.lastIndexOf("."));
    }

    // 음악 분석 요청 전송
    @PostMapping(path = "/api/music/{musicId}/analyze")
    public ResponseEntity<Object> analyze(@PathVariable Long musicId, Double confidence) {
        try {
            musicAnalysisService.analyze(musicId, confidence);
            return ResponseEntity.status(HttpStatus.OK).body(null);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleErrorReturn("requestFailed.analysis", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 분석 특정 Pitch 값 제거 요청 전송
    @PostMapping(path = "/api/music/{musicId}/delete")
    public ResponseEntity<Object> deletePitch(@PathVariable Long musicId, Double time) {
        try {
            musicAnalysisService.deletePitch(musicId, time);
            return ResponseEntity.status(HttpStatus.OK).body(null);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("deletePitchFailed.analysis", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 파일 다운로드
    @GetMapping(path = "/api/music/audio/{filename}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/audio/" + filename;

        if (new File(path).exists()) {
            return FileHandler.getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    // Pitch 그래프 파일 다운로드
    @GetMapping(path = "/api/music/pitch/{musicId}")
    public ResponseEntity<FileSystemResource> getPitchGraphFile(@PathVariable Long musicId) throws IOException {

        String path = System.getProperty("user.dir") + "/upload/audio/" + musicId + "/pitch.xlsx";

        if (new File(path).exists()) {
            return FileHandler.getFileSystemResource("pitch.xlsx", path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

}
