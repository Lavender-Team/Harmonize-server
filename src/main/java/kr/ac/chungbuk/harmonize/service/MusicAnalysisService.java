package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MusicAnalysisService {

    private final MusicRepository musicRepository;

    @Autowired
    public MusicAnalysisService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    // 음악 파일 및 가사 파일 업로드
    @Transactional
    public void updateFiles(Long musicId, MultipartFile audioFile, MultipartFile lyricFile) throws Exception {
        Music music = musicRepository.findById(musicId).orElseThrow();

        // 음악 파일
        if (audioFile != null) {
            try {
                if (music.getAudioFile() != null)
                    FileHandler.deleteAudioFile(music.getAudioFile(), music.getMusicId());

                String audioFilePath = FileHandler.saveAudioFile(audioFile, music.getMusicId());
                music.setAudioFile(audioFilePath);
            } catch (IOException e) {
                throw e;
            }
        }

        // TODO 가사 파일 읽어오기 및 저장 처리
    }
}
