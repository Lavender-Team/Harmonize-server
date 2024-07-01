package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Controller
@Slf4j
public class LogController {

    // 벌크 업로드 결과 조회
    @GetMapping("/api/log/bulk")
    @ResponseBody
    public List<String> getBulkUploadLog() throws Exception {
        String path = System.getProperty("user.dir") + "/upload/bulk_log.txt";
        File log = new File(path);
        if (log.exists()) {
            return Files.readAllLines(log.toPath());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    // 벌크 업로드 결과 로그 지우기
    @DeleteMapping("/api/log/bulk")
    public ResponseEntity<String> clearBulkUploadLog() {
        try {
            FileHandler.clearBulkUploadLog(false);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("벌크 업로드 로그 삭제 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }


    // 파일 벌크 업로드 결과 조회
    @GetMapping("/api/log/bulk/files")
    @ResponseBody
    public List<String> getBulkFileUploadLog() throws Exception {
        String path = System.getProperty("user.dir") + "/upload/bulk_file_log.txt";
        File log = new File(path);
        if (log.exists()) {
            return Files.readAllLines(log.toPath());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

    // 파일 벌크 업로드 결과 로그 지우기
    @DeleteMapping("/api/log/bulk/files")
    public ResponseEntity<String> clearBulkFileUploadLog() {
        try {
            FileHandler.clearBulkUploadLog(true);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("벌크 파일 업로드 로그 삭제 중 오류가 발생하였습니다.");
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
}
