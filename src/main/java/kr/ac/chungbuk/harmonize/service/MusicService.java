package kr.ac.chungbuk.harmonize.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.SearchRequestDto;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.MusicAnalysis;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import kr.ac.chungbuk.harmonize.repository.GroupRepository;
import kr.ac.chungbuk.harmonize.repository.MusicAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.ThemeRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MusicService {

    private final MusicRepository musicRepository;
    private final MusicAnalysisRepository musicAnalysisRepository;
    private final GroupRepository groupRepository;
    private final ThemeRepository themeRepository;

    Validator validator;

    @Autowired
    public MusicService(MusicRepository musicRepository, MusicAnalysisRepository musicAnalysisRepository,
            GroupRepository groupRepository, ThemeRepository themeRepository, Validator validator) {
        this.musicRepository = musicRepository;
        this.musicAnalysisRepository = musicAnalysisRepository;
        this.groupRepository = groupRepository;
        this.themeRepository = themeRepository;
        this.validator = validator;
    }

    // 음악 생성
    public Music create(MusicRequestDto musicParam) throws Exception {
        // 음악 객체
        Music music = new Music();
        music.setTitle(musicParam.getTitle());
        music.setGenre(Genre.fromString(musicParam.getGenre()));
        music.setKaraokeNum(musicParam.getKaraokeNum());
        music.setReleaseDate(musicParam.getReleaseDate());
        music.setPlayLink(musicParam.getPlayLink());
        music.setView(0L);
        music.setLikes(0L);

        music = musicRepository.save(music);

        // 음악 분석 결과
        MusicAnalysis analysis = new MusicAnalysis(music.getMusicId());
        musicAnalysisRepository.save(analysis);

        // 가수(그룹)
        if (musicParam.getGroupId() != null) {
            Optional<Group> group = groupRepository.findById(musicParam.getGroupId());
            group.ifPresent(music::setGroup);
        }

        // 음악 테마(특징)
        if (musicParam.getThemes() != null)
            saveThemes(musicParam.getThemes(), music);

        // 앨범 커버 파일 저장
        if (musicParam.getAlbumCover() != null) {
            try {
                String albumCoverPath = FileHandler.saveAlbumCoverFile(musicParam.getAlbumCover(), music.getMusicId());
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
    public void update(Long musicId, MusicRequestDto musicParam) throws IOException {

        // 음악 객체
        Music music = musicRepository.findById(musicId).orElseThrow();
        music.setTitle(musicParam.getTitle());
        if (musicParam.getGenre() != null)
            music.setGenre(Genre.fromString(musicParam.getGenre()));
        if (musicParam.getKaraokeNum() != null)
            music.setKaraokeNum(musicParam.getKaraokeNum());
        if (musicParam.getReleaseDate() != null)
            music.setReleaseDate(musicParam.getReleaseDate());
        if (musicParam.getPlayLink() != null)
            music.setPlayLink(musicParam.getPlayLink());

        // 가수(그룹)
        if (musicParam.getGroupId() != null) {
            Optional<Group> group = groupRepository.findById(musicParam.getGroupId());
            group.ifPresent(music::setGroup);
        }

        // 음악 테마(특징)
        if (musicParam.getThemes() != null) {
            themeRepository.deleteAllByMusic(music);
            saveThemes(musicParam.getThemes(), music);
        }

        // 앨범 커버 파일 새로 업로드시 수정
        if (musicParam.getAlbumCover() != null) {
            try {
                if (music.getAlbumCover() != null)
                    FileHandler.deleteAlbumCoverFile(music.getAlbumCover(), music.getMusicId()); // 기존 파일 삭제
                String albumCoverPath = FileHandler.saveAlbumCoverFile(musicParam.getAlbumCover(), music.getMusicId()); // 새 파일 저장
                music.setAlbumCover(albumCoverPath);
            } catch (IOException e) {
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
    public void createBulk(MultipartFile bulkFile, String charset) throws IOException, CsvValidationException {
        Reader reader = new InputStreamReader(bulkFile.getInputStream(), charset);
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

                MusicRequestDto musicParam = new MusicRequestDto();
                musicParam.setTitle(line[0]);
                musicParam.setGenre(Genre.fromString(line[2]).name());
                musicParam.setKaraokeNum(line[3]);
                musicParam.setReleaseDate(releaseDate);
                musicParam.setPlayLink(line[5]);
                musicParam.setThemes(themes);

                // 검증
                Errors errors = validator.validateObject(musicParam);
                if (errors.hasFieldErrors()) {
                    throw new CsvValidationException(errors.getFieldError().getField() + " 검증 문제");
                }

                Music created = create(musicParam);
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

    // 음악 상세정보 조회
    @Transactional
    public Music read(Long musicId, boolean countView) throws Exception {
        Music music = musicRepository.findById(musicId).orElseThrow();

        // 조회수를 올려야 하면 (일반 사용자 조회시)
        if (countView)
            music.countView();

        return music;
    }

    // 음악 목록 조회
    public Page<Music> list(Pageable pageable) {
        return musicRepository.findAll(pageable);
    }

    // 전체 테마 목록 조회
    public Page<Theme> listThemes(Pageable pageable) {
        return themeRepository.findUniqueThemeNames(pageable);
    }

    // 전체 테마 목록 검색
    public Page<Theme> searchThemes(String themeName, Pageable pageable) {
        return themeRepository.findUniqueThemeNamesContaining(themeName, pageable);
    }

    // 특정 테마의 음악 목록 조회
    public Page<Music> listMusicOfTheme(String themeName, Pageable pageable) {
        return musicRepository.findAllByTheme(themeName, pageable);
    }

    // 특정 테마의 음악 목록 검색
    public Page<Music> searchMusicOfTheme(String title, String themeName, Pageable pageable) {
        return musicRepository.findAllByThemeTitleContaining(title, themeName, pageable);
    }

    // 음악 제목 검색
    public Page<Music> search(String title, Pageable pageable) {
        return musicRepository.findByTitleContaining(title, pageable);
    }

    // 음악 상세 검색
    public Map<String, Page<Music>> searchDetail(SearchRequestDto query, Pageable pageable) {
        Map<String, Page<Music>> searchResult = new HashMap<>();

        GroupType groupType = query.getGroupType() != null ? GroupType.fromString(query.getGroupType()) : null;
        Genre genre = query.getGenre() != null ? Genre.fromString(query.getGenre()) : null;

        searchResult.put("all", musicRepository.searchAll(query.getQuery(), groupType, genre, pageable));
        searchResult.put("title", musicRepository.searchTitle(query.getQuery(), groupType, genre, pageable));
        searchResult.put("artist", musicRepository.searchGroupName(query.getQuery(), groupType, genre, pageable));
        searchResult.put("karaokeNum", musicRepository.searchKaraokeNum(query.getQuery(), groupType, genre, pageable));

        return searchResult;
    }
    
    // 인기곡 순위
    public Page<Music> listByRank(Pageable pageable) {
        return musicRepository.findAllOrderByRank(pageable);
    }

    // 최신 음악 (1년 이내)
    public Page<Music> listReleasedWithinOneYear(Pageable pageable) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime oneYearAgo = today.minusYears(1);
        return musicRepository.findReleasedWithinOneYear(oneYearAgo, today, pageable);
    }

    public int count() {
        return (int) musicRepository.count();
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
