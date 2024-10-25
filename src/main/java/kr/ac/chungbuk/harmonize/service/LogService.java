package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.Log;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.EventType;
import kr.ac.chungbuk.harmonize.repository.LogRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;
    private final MusicRepository musicRepository;

    @Autowired
    public LogService(LogRepository logRepository, MusicRepository musicRepository) {
        this.logRepository = logRepository;
        this.musicRepository = musicRepository;
    }

    public void save(User user, Long musicId, EventType event) {
        Music music = musicRepository.findById(musicId).orElseThrow();
        Log log = new Log(user, music, event);
        logRepository.save(log);
    }
}
