package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.IntegrationTestSupport;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.repository.BookmarkRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MusicActionServiceTest extends IntegrationTestSupport {

    @Autowired
    MusicActionService musicActionService;
    @Autowired
    BookmarkRepository bookmarkRepository;
    @Autowired
    MusicRepository musicRepository;
    @Autowired
    UserRepository userRepository;

    @AfterEach
    void tearDown() {
        bookmarkRepository.deleteAllInBatch();
        musicRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("음악을 북마크(좋아요) 처리합니다.")
    @Test
    void createBookmark() {
        // given
        Music music = musicRepository.save(createMusic("음악"));
        User user = userRepository.save(createUser());

        // when
        musicActionService.createBookmark(user.getUserId(), music.getMusicId());

        // then
        Boolean result = bookmarkRepository.existsByUserAndMusic(user, music);
        assertThat(result).isTrue();
    }

    @DisplayName("음악 북마크 처리시 존재하지 않는 음악 선택시 예외가 발생합니다.")
    @Test
    void createBookmarkMusicNotExists() {
        // given
        User user = userRepository.save(createUser());

        // when then
        assertThatThrownBy(() -> musicActionService.createBookmark(user.getUserId(), 999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("음악 북마크 처리시 존재하지 않는 회원 요청시 예외가 발생합니다.")
    @Test
    void createBookmarkUserNotExists() {
        // given
        Music music = musicRepository.save(createMusic("음악"));

        // when then
        assertThatThrownBy(() -> musicActionService.createBookmark(999999L, music.getMusicId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("이미 북마크(좋아요) 상태의 음악에 북마크 처리시 예외가 발생합니다.")
    @Test
    void createBookmarkDuplicate() {
        // given
        Music music = musicRepository.save(createMusic("음악"));
        User user = userRepository.save(createUser());
        musicActionService.createBookmark(user.getUserId(), music.getMusicId());

        // when then
        assertThatThrownBy(() -> musicActionService.createBookmark(user.getUserId(), music.getMusicId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("duplicated bookmark");
    }

    @DisplayName("음악 북마크(좋아요)를 취소합니다.")
    @Test
    void deleteBookmark() {
        // given
        Music music = musicRepository.save(createMusic("음악"));
        User user = userRepository.save(createUser());
        musicActionService.createBookmark(user.getUserId(), music.getMusicId());

        // when
        musicActionService.deleteBookmark(user.getUserId(), music.getMusicId());

        // then
        Boolean result = bookmarkRepository.existsByUserAndMusic(user, music);
        assertThat(result).isFalse();
    }

    @DisplayName("음악 북마크 취소시 존재하지 않는 음악 선택시 예외가 발생합니다.")
    @Test
    void deleteBookmarkMusicNotExists() {
        // given
        User user = userRepository.save(createUser());

        // when then
        assertThatThrownBy(() -> musicActionService.deleteBookmark(user.getUserId(), 999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("음악 북마크 취소시 존재하지 않는 회원 요청시 예외가 발생합니다.")
    @Test
    void deleteBookmarkUserNotExists() {
        // given
        Music music = musicRepository.save(createMusic("음악"));

        // when then
        assertThatThrownBy(() -> musicActionService.deleteBookmark(999999L, music.getMusicId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("음악의 북마크 상태를 조회합니다.")
    @Test
    void getIsBookmarked() {
        // given
        Music music = musicRepository.save(createMusic("음악"));
        User user = userRepository.save(createUser());
        musicActionService.createBookmark(user.getUserId(), music.getMusicId());

        // when
        boolean isBookmarked = musicActionService.getIsBookmarked(user, music.getMusicId());

        // then
        assertThat(isBookmarked).isTrue();
    }

    @DisplayName("회원이 북마크한 음악 목록을 조회합니다.")
    @Test
    void listBookmarkedMusic() {
        // given
        Music music1 = musicRepository.save(createMusic("음악1"));
        Music music2 = musicRepository.save(createMusic("음악2"));
        User user = userRepository.save(createUser());
        musicActionService.createBookmark(user.getUserId(), music1.getMusicId());
        musicActionService.createBookmark(user.getUserId(), music2.getMusicId());

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Music> result = musicActionService.listBookmarkedMusic(user, pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("음악1", "음악2");
    }

    @DisplayName("로그인되지 않은 회원이 북마크 음악 목록 요청시 예외가 발생합니다.")
    @Test
    void listBookmarkedMusicUserNotExists() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 4);

        // when then
        assertThatThrownBy(() -> musicActionService.listBookmarkedMusic(null, pageRequest))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("회원이 북마크한 음악 목록 개수를 조회합니다.")
    @Test
    void countBookmarkedMusic() {
        // given
        Music music1 = musicRepository.save(createMusic("음악1"));
        Music music2 = musicRepository.save(createMusic("음악2"));
        User user = userRepository.save(createUser());
        musicActionService.createBookmark(user.getUserId(), music1.getMusicId());
        musicActionService.createBookmark(user.getUserId(), music2.getMusicId());

        // when
        Long result = musicActionService.countBookmarkedMusic(user);

        // then
        assertThat(result).isEqualTo(2);
    }

    @DisplayName("로그인되지 않은 회원이 북마크 음악 수 조회시 예외가 발생합니다.")
    @Test
    void countBookmarkedMusicUserNotExists() {
        // when then
        assertThatThrownBy(() -> musicActionService.countBookmarkedMusic(null))
                .isInstanceOf(NoSuchElementException.class);
    }

    private Music createMusic(String title) {
        return Music.builder()
                .title(title)
                .genre(Genre.BALLADE)
                .karaokeNum("12345")
                .releaseDate(LocalDateTime.of(
                        LocalDate.of(2000, 12, 21),
                        LocalTime.of(0, 0, 0)
                ))
                .playLink("link.com")
                .view(0L)
                .likes(0L)
                .build();
    }

    private User createUser() {
        return User.builder()
                .loginId("loginId")
                .password("password")
                .email("email@email.com")
                .nickname("홍길동")
                .role(Role.USER)
                .build();
    }

}