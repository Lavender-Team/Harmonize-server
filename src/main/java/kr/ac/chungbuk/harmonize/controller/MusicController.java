package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.SearchRequestDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicListDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.service.MusicActionService;
import kr.ac.chungbuk.harmonize.service.MusicService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.*;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Slf4j
@Controller
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;
    private final MusicActionService musicActionService;
    private final MessageSource messageSource;

    @Autowired
    public MusicController(MusicService musicService, MusicActionService musicActionService,
                           @Qualifier("messageSource") MessageSource messageSource) {
        this.musicService = musicService;
        this.musicActionService = musicActionService;
        this.messageSource = messageSource;
    }

    // 음악 생성
    @PostMapping
    public ResponseEntity<Object> create(@Validated MusicRequestDto musicParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            musicService.create(musicParam);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("createFailed.music", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 수정
    @PutMapping(path = "/{musicId}")
    public ResponseEntity<Object> update(@PathVariable Long musicId,
                                         @Validated MusicRequestDto musicParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            musicService.update(musicId, musicParam);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        }
        catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.music", messageSource, Locale.getDefault())
            );
        }
        catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("updateFailed.music", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 삭제
    @DeleteMapping(path = "/{musicId}")
    public ResponseEntity<Object> delete(@PathVariable Long musicId) {
        try {
            musicService.delete(musicId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.music", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("deleteFailed.music", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 벌크 업로드
    @PostMapping("/bulk")
    public ResponseEntity<Object> createBulk(MultipartFile bulkFile,
                                             @RequestParam(value="charset", defaultValue="utf-8") String charset) {
        try {
            musicService.createBulk(bulkFile, charset);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);

        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("bulkUploadFailed.music", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 상세정보 조회
    @GetMapping("/{musicId}")
    @ResponseBody
    public MusicDto read(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        try {
            boolean countView = true;
            if (user != null && user.getRole() == Role.ADMIN)
                countView = false;

            Music music = musicService.read(musicId, countView);
            List<Music> similarMusics = musicService.readSimilarMusic(music);

            return MusicDto.build(music, similarMusics, musicActionService.getIsBookmarked(user, musicId));
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 음악 목록 조회
    @GetMapping
    @ResponseBody
    public Page<MusicListDto> list(String title, String genre,
                                   @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Music> list;

            if (title != null || genre != null)
                list = musicService.search(title, genre, pageable);
            else
                list = musicService.list(pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 음악 상세 검색 (사용자)
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Object> search(SearchRequestDto query, BindingResult bindingResult,
            @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC, size = 50) Pageable pageable) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
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

            return ResponseEntity.status(HttpStatus.OK).body(searchResultDto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 인기곡 순위
    @GetMapping("/rank")
    @ResponseBody
    public Page<MusicListDto> listByRank(@PageableDefault(size = 12) Pageable pageable) {
        try {
            Page<Music> list = musicService.listByRank(pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 최신 음악 (1년 이내)
    @GetMapping("/recent")
    @ResponseBody
    public Page<MusicListDto> listReleasedWithinOneYear(@PageableDefault(size = 6) Pageable pageable) {
        try {
            Page<Music> list = musicService.listReleasedWithinOneYear(pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 전체 테마 목록 조회
    @GetMapping("/theme")
    @ResponseBody
    public Page<Theme> listThemes(String themeName, @PageableDefault Pageable pageable) {
        try {
            Page<Theme> list;
            if (themeName == null || themeName.isEmpty())
                list = musicService.listThemes(pageable);
            else
                list = musicService.searchThemes(themeName, pageable);

            return list;
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 특정 테마의 음악 목록 조회
    @GetMapping("/theme/music")
    @ResponseBody
    public Page<MusicListDto> listMusicOfTheme(String themeName, String title,
                                               @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Music> list;

            if (title == null || title.isEmpty())
                list = musicService.listMusicOfTheme(themeName, pageable);
            else
                list = musicService.searchMusicOfTheme(title, themeName, pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    list.getTotalElements());
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 전체 음악 수 조회
    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> countMusic() {
        int count = musicService.count();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
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
