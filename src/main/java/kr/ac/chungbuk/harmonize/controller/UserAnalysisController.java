package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.UserAnalysisService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/user")
public class UserAnalysisController {

    private final UserAnalysisService userAnalysisService;

    // 음역대 분석 결과 업로드
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/analysis")
    public void uploadResult(@PathVariable Long userId, Double highestPitch, Double lowestPitch) {
        userAnalysisService.save(userId, highestPitch, lowestPitch);
    }

    // 음역대 분석 요청
    @ResponseBody
    @PostMapping("/uasys/analyze")
    public Object analyze(Long userId, MultipartFile file) throws IOException {

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
            log.debug("Max Pitch: " + responseBody.get("maxPitch"));
            log.debug("Min Pitch: " + responseBody.get("minPitch"));
        }

        // 응답 처리
        return responseBody;
    }
}
