package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.MusicAnalysisService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/music")
public class MusicAnalysisController {

    private final MusicAnalysisService musicAnalysisService;
    private final FileHandler fileHandler;

    // 음악 파일 및 가사 파일 업로드
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/{musicId}/files")
    public void updateFiles(@PathVariable Long musicId, MultipartFile audioFile,
                                              MultipartFile lyricFile) throws IOException, SizeLimitExceededException {
        musicAnalysisService.updateFiles(musicId, audioFile, lyricFile);
    }

    // 앨범 커버, 음악, 가사 파일 업로드 (벌크 업로드: 파일 이름으로 음악 조회)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(path = "/bulk/files")
    public void updateFiles(MultipartFile albumCover, MultipartFile audioFile,
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
            fileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "제목이 일치하는 곡이 없음", true);
            throw e;
        } catch (IncorrectResultSizeDataAccessException e) {
            fileHandler.writeBulkUploadLog("[이름오류] " + musicTitle, "같은 제목 곡 두 개 이상", true);
            throw e;
        } catch (SizeLimitExceededException e) {
            fileHandler.writeBulkUploadLog(musicTitle, "가사 용량 너무 큼", true);
            throw e;
        } catch (Exception e) {
            fileHandler.writeBulkUploadLog(musicTitle, "파일 관련 오류 발생", true);
            throw e;
        }
    }

    private String getMusicTitle(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        return originalFilename.substring(0, originalFilename.lastIndexOf("."));
    }

    // 음악 분석 요청 전송
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{musicId}/analyze")
    public void analyze(@PathVariable Long musicId, Double confidence,
                                          @RequestParam(defaultValue = "false") boolean analyzeWithoutModel)
            throws FileNotFoundException {

        if (analyzeWithoutModel) {
            // 직접 분석 결과 xlsx 파일 업로드 후 분석만 실행
            musicAnalysisService.analyzeWithoutModel(musicId);
        } else {
            // 모델을 통해 Pitch Estimation 진행 및 분석
            musicAnalysisService.analyze(musicId, confidence);
        }
    }

    // 음악 분석 특정 Pitch 값 제거 요청 전송
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{musicId}/delete", params = "action=value")
    public void deletePitch(@PathVariable Long musicId, Double time) throws Exception {
        musicAnalysisService.deletePitch(musicId, time);
    }

    // 음악 분석 특정 Pitch 범위 제거 요청 전송
    @PutMapping(path = "/{musicId}/delete", params = "action=range")
    public void deletePitch(@PathVariable Long musicId, Double time, String range) throws Exception {
        if (!range.equals("upper") && !range.equals("lower")) {
            throw new IllegalArgumentException();
        }

        musicAnalysisService.deletePitchRange(musicId, time, range);
    }

    // 음악 파일 다운로드
    @GetMapping(path = "/audio/{filename}")
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = fileHandler.getAudioDirectoryPath() + filename;

        if (!(new File(path).exists())) {
            throw new FileNotFoundException(filename);
        }

        return fileHandler.getFileSystemResource(filename, path);
    }

    // Pitch 그래프 파일 다운로드
    @GetMapping(path = "/pitch/{musicId}")
    public ResponseEntity<FileSystemResource> getPitchGraphFile(@PathVariable Long musicId) throws IOException {

        String path = fileHandler.getAudioDirectoryPath() + musicId + "/pitch.xlsx";

        if (!(new File(path).exists())) {
            throw new FileNotFoundException("pitch.xlsx");
        }

        return fileHandler.getFileSystemResource("pitch.xlsx", path);
    }

    // Pitch 오디오 파일 다운로드
    @GetMapping(path = "/pitch/audio/{musicId}")
    public ResponseEntity<FileSystemResource> getPitchAudioFile(@PathVariable Long musicId) throws IOException {

        String path = fileHandler.getAudioDirectoryPath() + musicId + "/output_audio.wav";

        if (!(new File(path).exists())) {
            throw new FileNotFoundException("output_audio.wav");
        }

        return fileHandler.getFileSystemResource("output_audio.wav", path);
    }

    // 콘텐츠 기반 추천 결과 업데이트 요청
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/recsys/content-based")
    public void requestContentBasedRec() {
        musicAnalysisService.requestContentBasedRec();
    }

    // 회원 대상 추천 결과 업데이트 요청
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/recsys/collaborative")
    public ResponseEntity<Object> requestCollaborativeRec(Long userId)
            throws TimeoutException, ExecutionException, InterruptedException {

        if (userId == null)
            musicAnalysisService.requestCollaborativeRec();
        else
            musicAnalysisService.requestCollaborativeRecOne(userId);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // 모델 상태 확인
    @ResponseBody
    @GetMapping(path = "/status")
    public Map<String, Boolean> checkModelStatus() {
        Map<String, Boolean> response = new HashMap<>();
        try {
            response = musicAnalysisService.checkSystemStatus();
        } catch (Exception ignored) { }
        return response;
    }
}
