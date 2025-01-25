package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.IntegrationTestSupport;
import kr.ac.chungbuk.harmonize.dto.request.ArtistRequestDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import kr.ac.chungbuk.harmonize.utility.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ArtistServiceTest extends IntegrationTestSupport {

    @Autowired
    ArtistService artistService;
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    FileHandler fileHandler;

    @Value("${file.dir}")
    String fileDir;

    @AfterEach
    void tearDown() {
        artistRepository.deleteAllInBatch();
        FileUtils.deleteFolderContents(new File(fileDir));
    }

    @DisplayName("새로운 가수를 생성합니다.")
    @Test
    void createArtist() throws IOException {
        // given
        ArtistRequestDto request = createArtistRequest("가수명");

        // when
        Artist artist = artistService.create(request);

        // then
        Artist result = artistRepository.findById(artist.getArtistId()).orElseThrow();
        File profileImage = new File(fileHandler.getProfileDirectoryPath() + result.getArtistId() + ".jpg");

        assertThat(result)
                .extracting("artistName", "gender", "activityPeriod", "nation", "agency")
                .containsExactly("가수명", Gender.MALE, "활동년대", "국적", "회사");
        assertThat(profileImage).exists();
    }

    @DisplayName("가수 정보를 수정합니다.")
    @Test
    void updateArtist() throws IOException {
        // given
        Artist artist = artistService.create(createArtistRequest("가수명"));

        ArtistRequestDto request = ArtistRequestDto.builder()
                .artistName("새가수명")
                .profileImage(getProfileImageFile())
                .gender("FEMALE")
                .activityPeriod("새활동년대")
                .nation("새국적")
                .agency("새회사")
                .build();

        // when
        artistService.update(artist.getArtistId(), request);

        // then
        Artist result = artistRepository.findByArtistName("새가수명").orElseThrow();
        File profileImage = new File(fileHandler.getProfileDirectoryPath() + result.getArtistId() + ".jpg");

        assertThat(result)
                .extracting("artistName", "gender", "activityPeriod", "nation", "agency")
                .containsExactly("새가수명", Gender.FEMALE, "새활동년대", "새국적", "새회사");
        assertThat(profileImage).exists();
    }

    @DisplayName("가수를 삭제합니다.")
    @Test
    void deleteArtist() throws IOException {
        // given
        Artist artist = artistService.create(createArtistRequest("가수명"));

        // when
        artistService.delete(artist.getArtistId());

        // then
        Optional<Artist> opArtist = artistRepository.findById(artist.getArtistId());
        assertThat(opArtist.isPresent()).isFalse();
    }

    @DisplayName("가수 상세 정보를 조회합니다.")
    @Test
    void readArtist() throws IOException {
        // given
        Artist artist = artistService.create(createArtistRequest("가수명"));

        // when
        Artist result = artistService.read(artist.getArtistId());

        // then
        assertThat(result)
                .extracting("artistName", "gender", "activityPeriod", "nation", "agency")
                .containsExactly("가수명", Gender.MALE, "활동년대", "국적", "회사");
    }

    @DisplayName("존재하지 않는 가수를 조회하면 예외가 발생합니다.")
    @Test
    void readArtistNotExists() {
        // when then
        assertThatThrownBy(() -> artistService.read(999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("전체 가수 목록을 조회합니다.")
    @Test
    void listArtist() throws IOException {
        // given
        artistService.create(createArtistRequest("가수1"));
        artistService.create(createArtistRequest("가수2"));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Artist> result = artistService.list(pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("artistName")
                .containsExactlyInAnyOrder("가수1", "가수2");
    }

    @DisplayName("가수 목록에서 이름으로 검색합니다.")
    @Test
    void searchArtist() throws IOException {
        // given
        artistService.create(createArtistRequest("가나다라"));
        artistService.create(createArtistRequest("다라마바"));
        artistService.create(createArtistRequest("아자차카"));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Artist> result = artistService.search("다라", pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("artistName")
                .containsExactlyInAnyOrder("가나다라", "다라마바");
    }

    private ArtistRequestDto createArtistRequest(String artistName) throws IOException {
        return ArtistRequestDto.builder()
                .artistName(artistName)
                .profileImage(getProfileImageFile())
                .gender("MALE")
                .activityPeriod("활동년대")
                .nation("국적")
                .agency("회사")
                .createSoloGroup(false)
                .build();
    }

    private MockMultipartFile getProfileImageFile() throws IOException {
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