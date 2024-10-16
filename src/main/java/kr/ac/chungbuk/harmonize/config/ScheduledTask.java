package kr.ac.chungbuk.harmonize.config;

import kr.ac.chungbuk.harmonize.service.MusicAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    MusicAnalysisService musicAnalysisService;

    @Autowired
    public ScheduledTask(MusicAnalysisService musicAnalysisService) {
        this.musicAnalysisService = musicAnalysisService;
    }

    @Scheduled(cron = "0 30 * * * ?")
    public void runContentBasedFiltering() {
        // 매 시간 30분마다 배치 작업 실행
        musicAnalysisService.requestContentBasedRec();
    }

}
