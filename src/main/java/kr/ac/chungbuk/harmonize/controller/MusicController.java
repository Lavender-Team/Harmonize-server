package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicDto;
import kr.ac.chungbuk.harmonize.dto.response.MusicListDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.service.MusicService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Locale;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Controller
@Slf4j
public class MusicController {

    private final MusicService musicService;
    private final MessageSource messageSource;

    @Autowired
    public MusicController(MusicService musicService, @Qualifier("messageSource") MessageSource messageSource) {
        this.musicService = musicService;
        this.messageSource = messageSource;
    }

    // 음악 생성
    @PostMapping(path = "/api/music")
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
    @PutMapping(path = "/api/music/{musicId}")
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
    @DeleteMapping(path = "/api/music/{musicId}")
    public ResponseEntity<Object> delete(@PathVariable Long musicId) {
        try {
            musicService.delete(musicId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.music", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("deleteFailed.music", messageSource, Locale.getDefault())
            );
        }
    }

    // 음악 벌크 업로드
    @PostMapping("/api/music/bulk")
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

    // 음악 상세정보 조회 (어드민)
    @GetMapping("/api/music/{musicId}")
    @ResponseBody
    public MusicDto readByAdmin(@PathVariable Long musicId) {
        try {
            Music music = musicService.readByAdmin(musicId);
            return MusicDto.build(music);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 음악 앨범커버 파일 다운로드
    @GetMapping("/api/music/albumcover/{filename}")
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

    // 음악 목록 조회
    @GetMapping(path = "/api/music")
    @ResponseBody
    public Page<MusicListDto> list(String title,
                                   @PageableDefault(sort = "musicId", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Music> list;

            if (title == null || title.isEmpty())
                list = musicService.list(pageable);
            else
                list = musicService.search(title, pageable);

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
    @GetMapping(path = "/api/music/theme")
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
    @GetMapping(path = "/api/music/theme/music")
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
}
