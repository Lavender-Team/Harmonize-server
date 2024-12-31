package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.LogService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/log")
public class LogController {

    private final LogService logService;

    // 벌크 업로드 결과 조회
    @ResponseBody
    @GetMapping("/bulk")
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
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/bulk")
    public void clearBulkUploadLog() throws IOException {
        FileHandler.clearBulkUploadLog(false);
    }


    // 파일 벌크 업로드 결과 조회
    @ResponseBody
    @GetMapping("/bulk/files")
    public List<String> getBulkFileUploadLog() throws Exception {
        String path = System.getProperty("user.dir") + "/upload/bulk_file_log.txt";

        File log = new File(path);
        if (!log.exists()) {
            throw new NoSuchFileException("bulk_file_log.txt");
        }

        return Files.readAllLines(log.toPath());
    }

    // 파일 벌크 업로드 결과 로그 지우기
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/bulk/files")
    public void clearBulkFileUploadLog() throws IOException {
        FileHandler.clearBulkUploadLog(true);
    }

    // 금일 생성된 로그 수 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count")
    public Map<String, Long> countLogCreatedToday() {
        long count = logService.countCreatedToday();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return response;
    }
}
