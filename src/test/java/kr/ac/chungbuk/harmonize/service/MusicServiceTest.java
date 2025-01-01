package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.config.KafkaTopicConfig;
import kr.ac.chungbuk.harmonize.config.ScheduledTask;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.entity.Music;
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
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
    ArtistRepository artistRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    FileHandler fileHandler;

    @MockBean
    KafkaTopicConfig kafkaTopicConfig;
    @MockBean
    ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    @MockBean
    ScheduledTask scheduledTask;

    @Value("${file.dir}")
    String fileDir;
    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() throws IOException {
        Artist artist = artistRepository.save(createSampleArtist());
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
        MusicRequestDto request = createMusicRequest(groupId);

        // when
        Music music = musicService.create(request);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        File albumcover = new File(fileHandler.getAlbumcoverDirectoryPath() + music.getMusicId() + ".jpg");

        assertThat(result)
                .extracting("title", "genre", "karaokeNum", "playLink", "view", "likes")
                .containsExactly("제목", Genre.KPOP, "12345", "link.com", 0L, 0L);
        assertThat(result.getGroup().getGroupId()).isEqualTo(groupId);
        assertThat(result.getThemes()).hasSize(2);
        assertThat(albumcover).exists();
    }

    private MusicRequestDto createMusicRequest(Long groupId) throws IOException {
        return MusicRequestDto.builder()
                .title("제목")
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