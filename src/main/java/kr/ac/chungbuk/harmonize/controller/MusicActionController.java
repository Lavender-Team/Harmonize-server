package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.response.MusicListDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.EventType;
import kr.ac.chungbuk.harmonize.service.LogService;
import kr.ac.chungbuk.harmonize.service.MusicActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static kr.ac.chungbuk.harmonize.exception.ErrorResult.SimpleErrorReturn;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/api/music")
// 북마크(좋아요), 추천 평가 등 음악과 관련된 사용자 행위를 담당하는 컨트롤러
public class MusicActionController {

    private final MusicActionService musicActionService;
    private final LogService logService;

    // 북마크(좋아요)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{musicId}/like")
    public void createBookmark(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        logService.save(user, musicId, EventType.bookmarkMusic);

        musicActionService.createBookmark(user.getUserId(), musicId);
    }

    // 북마크(좋아요) 취소
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/{musicId}/like")
    public void deleteBookmark(@PathVariable Long musicId, @AuthenticationPrincipal User user) {
        logService.save(user, musicId, EventType.unbookmarkMusic);

        musicActionService.deleteBookmark(user.getUserId(), musicId);
    }

    // 회원의 북마크한 음악 목록 조회
    @ResponseBody
    @GetMapping("/bookmarked")
    public Page<MusicListDto> listBookmarkedMusic(@PageableDefault(size = 16) Pageable pageable,
                                                      @AuthenticationPrincipal User user) {
        Page<Music> list = musicActionService.listBookmarkedMusic(user, pageable);

        return new PageImpl<>(
                list.getContent().stream().map(MusicListDto::build).toList(),
                pageable,
                musicActionService.countBookmarkedMusic(user));
    }

    // 추천에 대한 피드백
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/{musicId}/feedback")
    public void feedback(@PathVariable Long musicId, Boolean isPositive,
                                           @AuthenticationPrincipal User user) {
        if (user == null)
            throw new NoSuchElementException();
        if (isPositive == null)
            throw new IllegalArgumentException();

        if (isPositive)
            logService.save(user, musicId, EventType.feedbackPositive);
        else
            logService.save(user, musicId, EventType.feedbackNegative);
    }
}
