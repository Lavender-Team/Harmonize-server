package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.MusicAnalysis;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.MusicAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.ThemeRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MusicService {

    private final MusicRepository musicRepository;
    private final MusicAnalysisRepository musicAnalysisRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository, MusicAnalysisRepository musicAnalysisRepository,
                        ThemeRepository themeRepository) {
        this.musicRepository = musicRepository;
        this.musicAnalysisRepository = musicAnalysisRepository;
        this.themeRepository = themeRepository;
    }

    // 음악 생성
    public void create(String title, String genre, MultipartFile albumCover, String karaokeNum,
                       LocalDateTime releaseDate, String playLink, List<String> themes) throws Exception {
        // 음악 객체
        Music music = new Music();
        music.setTitle(title);
        music.setGenre(Genre.fromString(genre));
        music.setKaraokeNum(karaokeNum);
        music.setReleaseDate(releaseDate);
        music.setPlayLink(playLink);
        music.setView(0L);
        music.setLikes(0L);

        music = musicRepository.save(music);

        // 음악 분석 결과
        MusicAnalysis analysis = new MusicAnalysis(music.getMusicId());
        musicAnalysisRepository.save(analysis);

        // 음악 테마(특징)
        List<Theme> themeList = new ArrayList<>();
        for (String theme : themes) {
            Theme themeObj = new Theme(music, theme);
            themeList.add(themeObj);
            themeRepository.save(themeObj);
        }
        music.setThemes(themeList);

        // 앨범 커버 파일 저장
        try {
            String albumCoverPath = FileHandler.saveAlbumCoverFile(albumCover, music.getMusicId());
            music.setAlbumCover(albumCoverPath);
        } catch (IOException e) {
            musicRepository.delete(music);
            throw e;
        }
        musicRepository.save(music);
    }

    // 음악 수정
    @Transactional
    public void update(Long musicId, String title, String genre, MultipartFile albumCover, String karaokeNum,
                       LocalDateTime releaseDate, String playLink, List<String> themes) throws IOException {

        // 음악 객체
        Music music = musicRepository.findById(musicId).orElseThrow();
        if (title != null) music.setTitle(title);
        if (genre != null) music.setGenre(Genre.fromString(genre));
        if (karaokeNum != null) music.setKaraokeNum(karaokeNum);
        if (releaseDate != null) music.setReleaseDate(releaseDate);
        if (playLink != null) music.setPlayLink(playLink);

        if (themes != null) {
            // 기존에 저장된 음악 테마(특징) 삭제
            themeRepository.deleteAllByMusic(music);

            // 음악 테마(특징) 저장
            List<Theme> themeList = new ArrayList<>();
            for (String theme : themes) {
                Theme themeObj = new Theme(music, theme);
                themeList.add(themeObj);
                themeRepository.save(themeObj);
            }
            music.setThemes(themeList);
        }

        // 앨범 커버 파일 새로 업로드시 수정
        if (albumCover != null) {
            try {
                if (music.getAlbumCover() != null)
                    FileHandler.deleteAlbumCoverFile(music.getAlbumCover(), music.getMusicId()); // 기존 파일 삭제
                String albumCoverPath = FileHandler.saveAlbumCoverFile(albumCover, music.getMusicId()); // 새 파일 저장
                music.setAlbumCover(albumCoverPath);
            } catch (IOException e) {
                musicRepository.delete(music);
                throw e;
            }
        }
        musicRepository.save(music);
    }

    // 음악 삭제
    @Transactional
    public void delete(Long musicId) throws Exception {
        Music music = musicRepository.findById(musicId).orElseThrow();
        MusicAnalysis analysis = musicAnalysisRepository.findById(music.getMusicId()).orElseThrow();

        if (music.getAlbumCover() != null && !music.getAlbumCover().isEmpty())
            FileHandler.deleteAlbumCoverFile(music.getAlbumCover(), music.getMusicId());
        themeRepository.deleteAllByMusic(music);
        musicRepository.delete(music);
        musicAnalysisRepository.delete(analysis);
    }

    // 음악 상세정보 조회 (어드민)
    public Music readByAdmin(Long musicId) throws Exception {
        return musicRepository.findById(musicId).orElseThrow();
    }

    // 음악 목록 조회
    public Page<Music> list(Pageable pageable) {
        return musicRepository.findAll(pageable);
    }

    // 전체 테마 목록 조회
    public Page<Theme> listThemes(Pageable pageable) {
        return themeRepository.findUniqueThemeNames(pageable);
    }

    // 특정 테마의 음악 목록 조회
    public Page<Music> listMusicOfTheme(Pageable pageable, String themeName) {
        return musicRepository.findAllByTheme(pageable, themeName);
    }
}
