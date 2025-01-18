package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.config.KafkaTopicConfig;
import kr.ac.chungbuk.harmonize.config.ScheduledTask;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.SearchRequestDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.*;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import kr.ac.chungbuk.harmonize.utility.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@MockBeans({
        @MockBean(KafkaTopicConfig.class),
        @MockBean(ReplyingKafkaTemplate.class),
        @MockBean(ScheduledTask.class)
})
class MusicServiceTest {

    @Autowired
    MusicService musicService;
    @Autowired
    MusicRepository musicRepository;
    @Autowired
    MusicAnalysisRepository musicAnalysisRepository;
    @Autowired
    GroupService groupService;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    FileHandler fileHandler;

    @Value("${file.dir}")
    String fileDir;


    @BeforeEach
    void setUp() throws IOException {
        Artist artist = artistRepository.save(createSampleArtist());
        groupService.create(createSampleGroupRequest(artist.getArtistId()));
        groupService.create(createSampleGroupRequest(artist.getArtistId()));
    }

    @AfterEach
    void tearDown() throws Exception {
        themeRepository.deleteAllInBatch();
        musicRepository.deleteAllInBatch();
        musicAnalysisRepository.deleteAllInBatch();

        List<Group> groups = groupRepository.findAll();
        for (Group group : groups) {
            groupService.delete(group.getGroupId());
        }
        artistRepository.deleteAllInBatch();

        FileUtils.deleteFolderContents(new File(fileDir));
    }

    @DisplayName("새로운 음악을 생성합니다.")
    @Test
    void createMusic() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        MusicRequestDto request = createMusicRequest("제목", groupId);

        // when
        Music music = musicService.create(request);

        // then
        Music result = musicRepository.findByTitle(music.getTitle()).orElseThrow();
        File albumcover = new File(fileHandler.getAlbumcoverDirectoryPath() + music.getMusicId() + ".jpg");

        assertThat(result)
                .extracting("title", "genre", "karaokeNum", "playLink", "view", "likes")
                .containsExactly("제목", Genre.KPOP, "12345", "link.com", 0L, 0L);
        assertThat(result.getGroup().getGroupId()).isEqualTo(groupId);
        assertThat(result.getThemes()).hasSize(2);
        assertThat(albumcover).exists();
    }

    @DisplayName("음악 정보를 수정합니다.")
    @Test
    void updateMusic() throws IOException {
        // given
        Long groupId1 = groupRepository.findAll().get(0).getGroupId();
        Long groupId2 = groupRepository.findAll().get(1).getGroupId();
        Music music = musicService.create(createMusicRequest("제목", groupId1));

        MusicRequestDto request = MusicRequestDto.builder()
                .title("새제목")
                .genre("BALLADE")
                .karaokeNum("67890")
                .releaseDate(LocalDateTime.of(
                        LocalDate.of(2010, 10, 10),
                        LocalTime.of(0, 0, 0)
                ))
                .playLink("newlink.com")
                .groupId(groupId2)
                .themes(List.of("특징1"))
                .albumCover(getAlbumcoverFile())
                .build();

        // when
        musicService.update(music.getMusicId(), request);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        File albumcover = new File(fileHandler.getAlbumcoverDirectoryPath() + music.getMusicId() + ".jpg");

        assertThat(result)
                .extracting("title", "genre", "karaokeNum", "playLink", "view", "likes")
                .containsExactly("새제목", Genre.BALLADE, "67890", "newlink.com", 0L, 0L);
        assertThat(result.getReleaseDate()).isEqualTo(LocalDateTime.of(
                LocalDate.of(2010, 10, 10),
                LocalTime.of(0, 0, 0)
        ));
        assertThat(result.getGroup().getGroupId()).isEqualTo(groupId2);
        assertThat(result.getThemes()).hasSize(1);
        assertThat(albumcover).exists();
    }

    @DisplayName("음악을 삭제합니다.")
    @Test
    void deleteMusic() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        Music music = musicService.create(createMusicRequest("제목", groupId));

        // when
        musicService.delete(music.getMusicId());

        // then
        Optional<Music> opMusic = musicRepository.findById(music.getMusicId());
        assertThat(opMusic.isPresent()).isFalse();
    }

    @DisplayName("음악 상세 정보를 조회합니다.")
    @Test
    void readMusic() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        Music music = musicService.create(createMusicRequest("제목", groupId));

        // when
        Music result = musicService.read(music.getMusicId(), true);

        // then
        assertThat(result.getView()).isEqualTo(1L);
    }

    @DisplayName("존재하지 않는 음악을 조회하면 예외가 발생합니다.")
    @Test
    void readMusicNotExists() {
        // when then
        assertThatThrownBy(() -> musicService.read(999999L, true))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("전체 음악 목록을 조회합니다.")
    @Test
    void listMusic() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("제목1", groupId));
        musicService.create(createMusicRequest("제목2", groupId));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Music> result = musicService.list(pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("제목1", "제목2");
    }

    @DisplayName("전체 테마 목록을 조회합니다.")
    @Test
    void listThemes() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("제목1", groupId));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Theme> result = musicService.listThemes(pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }

    @DisplayName("특정 테마의 음악 목록을 조회합니다.")
    @Test
    void listMusicOfTheme() throws IOException {
        // given
        String themeName = "특징1";

        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("제목1", groupId));
        musicService.create(createMusicRequest("제목2", groupId));
        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Music> result = musicService.listMusicOfTheme(themeName, pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("제목1", "제목2");
    }

    @DisplayName("노래 제목으로 음악을 검색합니다.")
    @Test
    void searchDetailWithTitle() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("APT.", groupId));
        musicService.create(createMusicRequest("HOME SWEET HOME", groupId));
        PageRequest pageRequest = PageRequest.of(0, 4);

        SearchRequestDto query = new SearchRequestDto();
        query.setQuery("HOME");

        // when
        Map<String, Page<Music>> result = musicService.searchDetail(query, pageRequest);

        // then
        assertThat(result.get("title").getTotalElements()).isEqualTo(1);
        assertThat(result.get("title").getContent()).hasSize(1)
                .extracting("title")
                .containsExactlyInAnyOrder("HOME SWEET HOME");
    }

    @DisplayName("그룹 이름으로 음악을 검색합니다.")
    @Test
    void searchDetailWithGroupName() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("APT.", groupId));
        musicService.create(createMusicRequest("HOME SWEET HOME", groupId));
        PageRequest pageRequest = PageRequest.of(0, 4);

        SearchRequestDto query = new SearchRequestDto();
        query.setQuery("그룹");

        // when
        Map<String, Page<Music>> result = musicService.searchDetail(query, pageRequest);

        // then
        assertThat(result.get("artist").getTotalElements()).isEqualTo(2);
        assertThat(result.get("artist").getContent()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("APT.", "HOME SWEET HOME");
    }

    @DisplayName("노래방 번호로 음악을 검색합니다.")
    @Test
    void searchDetailWithKaraokeNum() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("APT.", groupId));
        musicService.create(createMusicRequest("HOME SWEET HOME", groupId));
        PageRequest pageRequest = PageRequest.of(0, 4);

        SearchRequestDto query = new SearchRequestDto();
        query.setQuery("123");

        // when
        Map<String, Page<Music>> result = musicService.searchDetail(query, pageRequest);

        // then
        assertThat(result.get("karaokeNum").getTotalElements()).isEqualTo(2);
        assertThat(result.get("karaokeNum").getContent()).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("APT.", "HOME SWEET HOME");
    }

    @DisplayName("전체 음악 수를 조회합니다.")
    @Test
    void count() throws IOException {
        // given
        Long groupId = groupRepository.findAll().get(0).getGroupId();
        musicService.create(createMusicRequest("APT.", groupId));
        musicService.create(createMusicRequest("HOME SWEET HOME", groupId));

        // when then
        assertThat(musicService.count()).isEqualTo(2);
    }

    private MusicRequestDto createMusicRequest(String title, Long groupId) throws IOException {
        return MusicRequestDto.builder()
                .title(title)
                .genre("KPOP")
                .karaokeNum("12345")
                .releaseDate(LocalDateTime.of(
                        LocalDate.of(2000, 12, 21),
                        LocalTime.of(0, 0, 0)
                ))
                .playLink("link.com")
                .groupId(groupId)
                .themes(List.of("특징1", "특징2"))
                .albumCover(getAlbumcoverFile())
                .build();
    }

    private Artist createSampleArtist() {
        return Artist.builder()
                .artistName("가수명")
                .gender(Gender.MALE)
                .activityPeriod("활동년대")
                .nation("국적")
                .agency("회사")
                .build();
    }

    private GroupRequestDto createSampleGroupRequest(Long artistId) {
        return GroupRequestDto.builder()
                .groupName("그룹명")
                .groupType("SOLO")
                .agency("회사")
                .artistIds(List.of(artistId))
                .build();
    }

    private MockMultipartFile getAlbumcoverFile() throws IOException {
        final String filename = "albumcover.jpg";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        return new MockMultipartFile(
                "images",
                filename,
                "jpg",
                fileInputStream
        );
    }
}