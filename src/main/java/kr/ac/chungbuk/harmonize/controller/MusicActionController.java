package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.service.MusicActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Locale;
import java.util.NoSuchElementException;

import static kr.ac.chungbuk.harmonize.utility.ErrorResult.SimpleErrorReturn;

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
    public ResponseEntity<Object> createBookmark(@PathVariable Long musicId) {
        // TODO 유저 ID는 로그인된 정보로 부터 가져오도록 해야함
        Long userId = 1L;

        try {
            musicActionService.createBookmark(userId, musicId);
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
    public ResponseEntity<Object> deleteBookmark(@PathVariable Long musicId) {
        // TODO 유저 ID는 로그인된 정보로 부터 가져오도록 해야함
        Long userId = 1L;

        try {
            musicActionService.deleteBookmark(userId, musicId);
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
}
