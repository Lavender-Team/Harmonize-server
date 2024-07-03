package kr.ac.chungbuk.harmonize.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.MusicAnalysis;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.MusicAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.ThemeRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
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
    public Music create(String title, String genre, MultipartFile albumCover, String karaokeNum,
            LocalDateTime releaseDate, String playLink, List<String> themes) throws Exception {
        // 음악 객체
        Music music = new Music();
        music.setTitle(title);
        music.setGenre(Genre.fromString(genre));
        if (!karaokeNum.isEmpty())
            music.setKaraokeNum(karaokeNum);
        music.setReleaseDate(releaseDate);
        if (!playLink.isEmpty())
            music.setPlayLink(playLink);
        music.setView(0L);
        music.setLikes(0L);

        music = musicRepository.save(music);

        // 음악 분석 결과
        MusicAnalysis analysis = new MusicAnalysis(music.getMusicId());
        musicAnalysisRepository.save(analysis);

        // 음악 테마(특징)
        saveThemes(themes, music);

        // 앨범 커버 파일 저장
        if (albumCover != null) {
            try {
                String albumCoverPath = FileHandler.saveAlbumCoverFile(albumCover, music.getMusicId());
                music.setAlbumCover(albumCoverPath);
            } catch (IOException e) {
                musicRepository.delete(music);
                throw e;
            }
        }

        return musicRepository.save(music);
    }

    // 음악 수정
    @Transactional
    public void update(Long musicId, String title, String genre, MultipartFile albumCover, String karaokeNum,
            LocalDateTime releaseDate, String playLink, List<String> themes) throws IOException {

        // 음악 객체
        Music music = musicRepository.findById(musicId).orElseThrow();
        if (title != null)
            music.setTitle(title);
        if (genre != null)
            music.setGenre(Genre.fromString(genre));
        if (karaokeNum != null)
            music.setKaraokeNum(karaokeNum);
        if (releaseDate != null)
            music.setReleaseDate(releaseDate);
        if (playLink != null)
            music.setPlayLink(playLink);

        if (themes != null) {
            // 기존에 저장된 음악 테마(특징) 삭제
            themeRepository.deleteAllByMusic(music);

            // 음악 테마(특징) 저장
            saveThemes(themes, music);
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

    // 음악 벌크 업로드
    public void createBulk(MultipartFile bulkFile) throws IOException, CsvValidationException {
        Reader reader = new InputStreamReader(bulkFile.getInputStream());
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

        String[] line;

        while ((line = csvReader.readNext()) != null) {
            try {
                for (int i = 0; i < line.length; i++)
                    line[i] = line[i].trim();

                if (line[0].isEmpty())
                    continue;

                // TODO 가수 관련 처리 : line[1]
                LocalDateTime releaseDate = LocalDateTime.parse(
                        (line[4].contains(".") ? line[4].replace('.', '-') : line[4])
                                + "T00:00:00");
                List<String> themes = new ArrayList<>();
                if (!line[7].isEmpty()) {
                    String[] themeArray = line[7].split(",");
                    themes = List.of(themeArray);
                }

                Music created = create(line[0], line[2], null, line[3], releaseDate, line[5], themes);
                if (!line[6].isEmpty())
                    created.setLyrics(line[6]);
                musicRepository.save(created);

                FileHandler.writeBulkUploadLog(line[0], "업로드 성공", false);
            } catch (Exception e) {
                FileHandler.writeBulkUploadLog(line[0], e.getMessage(), false);
                log.debug(e.getMessage());
            }
        }

        csvReader.close();
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

    private void saveThemes(List<String> themes, Music music) {
        List<Theme> themeList = new ArrayList<>();
        for (String theme : themes) {
            Theme themeObj = new Theme(music, theme);
            themeList.add(themeObj);
            themeRepository.save(themeObj);
        }
        music.setThemes(themeList);
    }
}
