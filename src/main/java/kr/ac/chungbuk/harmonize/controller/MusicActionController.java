package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.response.MusicListDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.service.MusicActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

@Slf4j
@Controller
@RequestMapping("/api/music")
// 북마크(좋아요), 추천 평가 등 음악과 관련된 사용자 행위를 담당하는 컨트롤러
public class MusicActionController {

    private final MusicActionService musicActionService;
    private final MessageSource messageSource;

    @Autowired
    public MusicActionController(MusicActionService musicActionService, MessageSource messageSource) {
        this.musicActionService = musicActionService;
        this.messageSource = messageSource;
    }

    // 북마크(좋아요)
    @PostMapping("/{musicId}/like")
    public ResponseEntity<Object> createBookmark(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        try {
            musicActionService.createBookmark(user.getUserId(), musicId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.bookmark", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("createFailed.bookmark", messageSource, Locale.getDefault())
            );
        }
    }

    // 북마크(좋아요) 취소
    @DeleteMapping("/{musicId}/like")
    public ResponseEntity<Object> deleteBookmark(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        try {
            musicActionService.deleteBookmark(user.getUserId(), musicId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("notFound.bookmark", messageSource, Locale.getDefault())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    SimpleErrorReturn("deleteFailed.bookmark", messageSource, Locale.getDefault())
            );
        }
    }

    // 회원의 북마크한 음악 목록 조회
    @GetMapping("/bookmarked")
    @ResponseBody
    public Page<MusicListDto> listBookmarkedMusic(@PageableDefault(size = 16) Pageable pageable,
                                                      @AuthenticationPrincipal User user) {
        try {
            Page<Music> list = musicActionService.listBookmarkedMusic(user, pageable);

            return new PageImpl<>(
                    list.getContent().stream().map(MusicListDto::build).toList(),
                    pageable,
                    musicActionService.countBookmarkedMusic(user));
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
