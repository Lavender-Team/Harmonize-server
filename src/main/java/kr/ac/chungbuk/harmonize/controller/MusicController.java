package kr.ac.chungbuk.harmonize.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.exceptions.CsvValidationException;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.SearchRequestDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicListDto;
import kr.ac.chungbuk.harmonize.dto.response.RecomMusicListDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.RecomMusic;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.EventType;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.service.LogService;
import kr.ac.chungbuk.harmonize.service.MusicActionService;
import kr.ac.chungbuk.harmonize.service.MusicService;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;
    private final MusicActionService musicActionService;
    private final LogService logService;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;


    // 음악 생성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void createMusic(@Validated MusicRequestDto musicParam) throws Exception {
        musicService.create(musicParam);
    }

    // 음악 수정
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(path = "/{musicId}")
    public void updateMusic(@PathVariable Long musicId, @Validated MusicRequestDto musicParam)
            throws Exception {

        musicService.update(musicId, musicParam);
    }

    // 음악 삭제
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping(path = "/{musicId}")
    public void deleteMusic(@PathVariable Long musicId) throws Exception {
        musicService.delete(musicId);
    }

    // 음악 벌크 업로드
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/bulk")
    public ResponseEntity<Object> uploadMusicBulk(MultipartFile bulkFile,
                                                  @RequestParam(value="charset", defaultValue="utf-8") String charset)
            throws CsvValidationException, IOException {

        musicService.createBulk(bulkFile, charset);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    // 음악 상세정보 조회
    @ResponseBody
    @GetMapping("/{musicId}")
    public MusicDto readMusic(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        boolean countView = user == null || user.getRole() != Role.ADMIN;

        if (user != null && user.getRole() != Role.ADMIN)
            logService.save(user, musicId, EventType.viewMusicDetail);

        Music music = musicService.read(musicId, countView);
        List<Music> similarMusics = musicService.readSimilarMusic(music);

        return MusicDto.build(music, objectMapper, similarMusics, musicActionService.getIsBookmarked(user, musicId));
    }

    // 음악 목록 조회
    @ResponseBody
    @GetMapping
    public Page<MusicListDto> listMusic(
            String title, String genre,
            @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Music> list;

        if (title != null || genre != null) {
            list = musicService.search(title, genre, pageable);
        }
        else {
            list = musicService.list(pageable);
        }

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 음악 상세 검색 (사용자)
    @ResponseBody
    @GetMapping("/search")
    public Map<String, Page> searchMusic(SearchRequestDto query,
            @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC, size = 50) Pageable pageable) {

        Map<String, Page> searchResultDto = new HashMap<>();
        Map<String, Page<Music>> searchResult = musicService.searchDetail(query, pageable);

        for (Map.Entry<String, Page<Music>> entry : searchResult.entrySet()) {
            Page<Music> result = entry.getValue();
            searchResultDto.put(entry.getKey(), new PageImpl<> (
                result.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    result.getTotalElements()
            ));
        }

        return searchResultDto;
    }

    // 개인 음악 추천 목록 조회
    @ResponseBody
    @GetMapping("/recommend")
    public Page<RecomMusicListDto> recommend(Long userId, String genre, Pageable pageable) {
        Page<RecomMusic> list;

        if (genre == null)
            list = musicService.recommend(userId, pageable);
        else
            list = musicService.recommend(userId, genre, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(RecomMusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 인기곡 순위
    @ResponseBody
    @GetMapping("/rank")
    public Page<MusicListDto> listByRank(@PageableDefault(size = 12) Pageable pageable) {
        Page<Music> list = musicService.listByRank(pageable);

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 최신 음악 (1년 이내)
    @ResponseBody
    @GetMapping("/recent")
    public Page<MusicListDto> listReleasedWithinOneYear(@PageableDefault(size = 6) Pageable pageable) {
        Page<Music> list = musicService.listReleasedWithinOneYear(pageable);

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 최초 추천 평가 노래 목록
    @ResponseBody
    @GetMapping("/first-feedback")
    public Page<MusicListDto> listFirstFeedback(@PageableDefault(size = 5) Pageable pageable) {
        Page<Music> list = musicService.listFirstFeedback(pageable);

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 전체 테마 목록 조회
    @ResponseBody
    @GetMapping("/theme")
    public Page<Theme> listThemes(String themeName, @PageableDefault Pageable pageable) {
        Page<Theme> list;
        if (themeName == null || themeName.isEmpty())
            list = musicService.listThemes(pageable);
        else
            list = musicService.searchThemes(themeName, pageable);

        return list;
    }

    // 특정 테마의 음악 목록 조회
    @ResponseBody
    @GetMapping("/theme/music")
    public Page<MusicListDto> listMusicOfTheme(String themeName, String title,
                                               @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Music> list;

        if (title == null || title.isEmpty())
            list = musicService.listMusicOfTheme(themeName, pageable);
        else
            list = musicService.searchMusicOfTheme(title, themeName, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 전체 음악 수 조회
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/count")
    public Map<String, Integer> countMusic() {
        int count = musicService.count();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return response;
    }

    // 음악 앨범커버 파일 다운로드
    @GetMapping("/albumcover/{filename}")
    public ResponseEntity<FileSystemResource> getAlbumCover(@PathVariable String filename) throws Exception {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/albumcover/" + filename;

        if (new File(path).exists()) {
            return FileHandler.getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }

}
