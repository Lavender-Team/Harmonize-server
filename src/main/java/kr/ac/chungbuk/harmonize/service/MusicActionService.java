package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Bookmark;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.repository.BookmarkRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;


@Service
@Slf4j
// 북마크(좋아요), 추천 평가 등 음악과 관련된 사용자 행위를 담당하는 서비스
public class MusicActionService {

    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final BookmarkRepository bookmarkRepository;

    @Autowired
    public MusicActionService(UserRepository userRepository, MusicRepository musicRepository,
                              BookmarkRepository bookmarkRepository) {
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    // 북마크(좋아요 버튼)
    @Transactional
    public void createBookmark(Long userId, Long musicId) {
        User user = userRepository.findById(userId).orElseThrow();
        Music music = musicRepository.findById(musicId).orElseThrow();

        if (bookmarkRepository.existsByUserAndMusic(user, music)) {
            // 이미 북마크된 상태로 요청시
            throw new IllegalStateException("duplicated bookmark");
        }

        Bookmark bookmark = new Bookmark(user, music);
        bookmarkRepository.save(bookmark);

        music.setLikes(bookmarkRepository.countByMusic(music));
    }

    // 북마크(좋아요 버튼) 취소
    @Transactional
    public void deleteBookmark(Long userId, Long musicId) {
        User user = userRepository.findById(userId).orElseThrow();
        Music music = musicRepository.findById(musicId).orElseThrow();

        Bookmark bookmark = bookmarkRepository.findByUserAndMusic(user, music);
        bookmarkRepository.delete(bookmark);

        music.setLikes(bookmarkRepository.countByMusic(music));
    }

    // 음악 상세정보 조회시 북마크 여부 조회
    public boolean getIsBookmarked(User user, Long musicId) {
        if (user == null)
            return false;

        Music music = musicRepository.findById(musicId).orElseThrow();
        return bookmarkRepository.existsByUserAndMusic(user, music);
    }

    // 회원의 북마크한 음악 목록 조회
    public Page<Music> listBookmarkedMusic(User user, Pageable pageable) {
        if (user == null)
            throw new NoSuchElementException("user is null");

        return musicRepository.findAllBookmarkedMusic(user.getUserId(), pageable);
    }

    // 회원의 북마크한 음악 목록 개수 조회
    public Long countBookmarkedMusic(User user) {
        if (user == null)
            throw new NoSuchElementException("user is null");

        return musicRepository.countAllBookmarkedMusic(user.getUserId());
    }
}
