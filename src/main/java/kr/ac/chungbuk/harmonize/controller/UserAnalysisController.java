package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.UserAnalysisService;
import kr.ac.chungbuk.harmonize.service.UserService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Slf4j
@Controller
@RequestMapping("/api/user")
public class UserAnalysisController {

    private final UserAnalysisService userAnalysisService;
    private final MessageSource messageSource;

    @Autowired
    public UserAnalysisController(UserAnalysisService userAnalysisService, MessageSource messageSource) {
        this.userAnalysisService = userAnalysisService;
        this.messageSource = messageSource;
    }

    // 음역대 분석 결과 업로드
    @PostMapping("/{userId}/analysis")
    public ResponseEntity<Object> create(@PathVariable Long userId, Double highestPitch, Double lowestPitch) {

        try {
            userAnalysisService.save(userId, highestPitch, lowestPitch);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.userAnalysis", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("createFailed.userAnalysis", messageSource, Locale.getDefault())
            );
        }
    }

    // 음역대 분석 요청
    @PostMapping("/uasys/analyze")
    @ResponseBody
    public Object analyze(Long userId, MultipartFile file) {

        try {
            FileHandler.saveVoiceRecordingFile(file, userId);

        RestTemplate restTemplate = new RestTemplate();

        // 요청 데이터 설정
        Map<String, String> requestData = new HashMap<>();
        requestData.put("name", userId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestData, headers);

        // Flask로 POST 요청 (임시)
        ResponseEntity<Map> response = restTemplate.exchange("http://localhost:5000/process-name", HttpMethod.POST, requestEntity, Map.class);

        // 응답
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            log.debug("Response from Flask:");
            log.debug("Percent: " + responseBody.get("percent"));
            log.debug("singer: " + responseBody.get("singer"));
            log.debug("Max Pitch: " + responseBody.get("max_pitch"));
            log.debug("Min Pitch: " + responseBody.get("min_pitch"));
        }

        // 응답 처리
        return responseBody;
        
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("io.saveFailed.userAnalysis", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("analyzeFailed.userAnalysis", messageSource, Locale.getDefault())
            );
        }
    }
}
