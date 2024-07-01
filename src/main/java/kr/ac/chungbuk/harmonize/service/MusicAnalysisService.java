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
            if (music.getAudioFile() != null)
                FileHandler.deleteAudioFile(music.getAudioFile(), music.getMusicId());

            String audioFilePath = FileHandler.saveAudioFile(audioFile, music.getMusicId());
            music.setAudioFile(audioFilePath);
        }

        // TODO 가사 파일 읽어오기 및 저장 처리
    }

    // 앨범 커버 파일 업로드 (벌크 업로드)
    @Transactional
    public void updateAlbumCover(MultipartFile albumCover) throws Exception {

        String originalFilename = albumCover.getOriginalFilename();
        assert originalFilename != null;
        String musicTitle = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        // TODO 중복된 음악 이름 처리를 위해 [가수명]을 제목에 포함하고 있으면 음악 조회시 사용하기

        Music music = musicRepository.findByTitle(musicTitle).orElseThrow();

        if (music.getAlbumCover() != null)
            FileHandler.deleteAlbumCoverFile(music.getAlbumCover(), music.getMusicId()); // 기존 파일 삭제
        String albumCoverPath = FileHandler.saveAlbumCoverFile(albumCover, music.getMusicId()); // 새 파일 저장
        music.setAlbumCover(albumCoverPath);

        FileHandler.writeBulkUploadLog("[앨범 커버] " + musicTitle, "업로드 성공", true);
    }

    // 음악 파일 업로드 (벌크 업로드)
    @Transactional
    public void updateAudioFile(MultipartFile audioFile) throws Exception {

        String originalFilename = audioFile.getOriginalFilename();
        assert originalFilename != null;
        String musicTitle = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        // TODO 중복된 음악 이름 처리를 위해 [가수명]을 제목에 포함하고 있으면 음악 조회시 사용하기

        Music music = musicRepository.findByTitle(musicTitle).orElseThrow();

        if (music.getAudioFile() != null)
            FileHandler.deleteAudioFile(music.getAudioFile(), music.getMusicId());

        String audioFilePath = FileHandler.saveAudioFile(audioFile, music.getMusicId());
        music.setAudioFile(audioFilePath);

        FileHandler.writeBulkUploadLog("[음악] " + musicTitle, "업로드 성공", true);
    }

    // 가사 파일 업로드 (벌크 업로드)
    @Transactional
    public void updateLyricFile(MultipartFile lyricFile) throws Exception {

        String originalFilename = lyricFile.getOriginalFilename();
        assert originalFilename != null;
        String musicTitle = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        // TODO 중복된 음악 이름 처리를 위해 [가수명]을 제목에 포함하고 있으면 음악 조회시 사용하기

        // TODO 가사 파일 읽기
        FileHandler.writeBulkUploadLog("[가사] " + musicTitle, "아직 구현 안됨", true);
        return;
    }
}
