package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.ArtistRequestDto;
import kr.ac.chungbuk.harmonize.dto.response.ArtistDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import kr.ac.chungbuk.harmonize.utility.ErrorResult;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Slf4j
@Controller
@RequestMapping("/api/artist")
public class ArtistController {

    private final ArtistService artistService;
    private final MessageSource messageSource;

    @Autowired
    public ArtistController(ArtistService artistService, MessageSource messageSource) {
        this.artistService = artistService;
        this.messageSource = messageSource;
    }

    // 가수 등록
    @PostMapping
    public ResponseEntity<Object> create(@Validated ArtistRequestDto artistParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            artistService.create(artistParam);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    SimpleErrorReturn("io.createFailed.artist", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("createFailed.artist", messageSource, Locale.getDefault())
            );
        }
    }

    // 가수 삭제
    @DeleteMapping("/{artistId}")
    public ResponseEntity<Object> delete(@PathVariable Long artistId) {
        Optional<Artist> artist = artistService.findById(artistId);
        if (artist.isPresent()) {
            try {
                artistService.delete(artistId);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        SimpleErrorReturn("io.deleteFailed.artist", messageSource, Locale.getDefault())
                );
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        SimpleErrorReturn("deleteFailed.artist", messageSource, Locale.getDefault())
                );
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.artist", messageSource, Locale.getDefault())
            );
        }
    }

    // 가수 목록 조회
    @GetMapping
    @ResponseBody
    public Page<ArtistDto> list(String artistName, @PageableDefault Pageable pageable) {
        Page<Artist> list;

        if (artistName == null || artistName.isEmpty())
            list = artistService.list(pageable);
        else
            list = artistService.search(artistName, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(ArtistDto::build).toList(),
                pageable,
                list.getTotalElements());
    }

    // 가수 수정
    @PutMapping("/{artistId}")
    public ResponseEntity<Object> update(@PathVariable Long artistId,
                                         @Validated ArtistRequestDto artistParam, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorResult errorResult = new ErrorResult(bindingResult, messageSource, Locale.getDefault());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResult);
        }

        try {
            artistService.update(artistId, artistParam);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    SimpleErrorReturn("io.updateFailed.artist", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    SimpleErrorReturn("updateFailed.artist", messageSource, Locale.getDefault())
            );
        }
    }

    // 가수 상세정보 조회
    @GetMapping("/{artistId}")
    @ResponseBody
    public ArtistDto readByAdmin(@PathVariable Long artistId) {
        try {
            Artist artist = artistService.findById(artistId).orElseThrow();
            return ArtistDto.build(artist);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 가수 프로필 이미지 파일 다운로드
    @GetMapping("/profile/{filename}")
    public ResponseEntity<FileSystemResource> getProfileImage(@PathVariable String filename) throws Exception {

        if (filename.contains(".."))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Filename cannot contains \"..\"");

        String path = System.getProperty("user.dir") + "/upload/profile/" + filename;

        if (new File(path).exists()) {
            return FileHandler.getFileSystemResource(filename, path);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
        }
    }
}
