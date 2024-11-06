package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.UserSaveDto;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.UserAnalysisService;
import kr.ac.chungbuk.harmonize.service.UserService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    // 음역대 분석 요청 (임시)
    @PostMapping("/uasys/analyze")
    public Map<String, Object> analyze(@RequestParam String name) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 데이터 설정
        Map<String, String> requestData = new HashMap<>();
        requestData.put("name", name);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestData, headers);

        // Flask로 POST 요청
        ResponseEntity<Map> response = restTemplate.exchange("http://localhost:5000/process-name", HttpMethod.POST, requestEntity, Map.class);
        // 응답 데이터 출력

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null) {
            System.out.println("Response from Flask:");
            System.out.println("Percent: " + responseBody.get("percent"));
            System.out.println("singer: " + responseBody.get("singer"));
            System.out.println("Max Pitch: " + responseBody.get("max_pitch"));
            System.out.println("Min Pitch: " + responseBody.get("min_pitch"));
        }

        // 응답 처리
        return responseBody;
    }
}
