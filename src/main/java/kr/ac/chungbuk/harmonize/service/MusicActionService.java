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
import org.springframework.stereotype.Service;


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
}
