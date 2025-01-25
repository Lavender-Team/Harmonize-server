package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.IntegrationTestSupport;
import kr.ac.chungbuk.harmonize.dto.request.ArtistRequestDto;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.repository.GroupRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import kr.ac.chungbuk.harmonize.utility.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class GroupServiceTest extends IntegrationTestSupport {

    @Autowired
    GroupService groupService;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    ArtistService artistService;
    @Autowired
    ArtistRepository artistRepository;
    @Autowired
    FileHandler fileHandler;

    @Value("${file.dir}")
    String fileDir;

    @BeforeEach
    void setUp() throws IOException {
        artistService.create(createArtistRequest("가수1"));
        artistService.create(createArtistRequest("가수2"));
        artistService.create(createArtistRequest("가수3"));
    }

    @AfterEach
    void tearDown() throws Exception {
        List<Group> groups = groupRepository.findAll();
        for (Group group : groups) {
            groupService.delete(group.getGroupId());
        }
        artistRepository.deleteAllInBatch();
        FileUtils.deleteFolderContents(new File(fileDir));
    }

    @DisplayName("새로운 그룹을 생성합니다.")
    @Test
    void createGroup() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        GroupRequestDto request = createGroupRequest("그룹명", List.of(ids.get(0), ids.get(1)));

        // when
        Group group = groupService.create(request);

        // then
        Group result = groupRepository.findById(group.getGroupId()).orElseThrow();
        File profileImage = new File(fileHandler.getGroupProfileDirectoryPath() + result.getGroupId() + ".jpg");

        assertThat(result)
                .extracting("groupName", "groupType", "groupSize", "agency")
                .containsExactly("그룹명", GroupType.GROUP, 2, "회사");
        assertThat(result.getMembers()).hasSize(2);
        assertThat(profileImage).exists();
    }

    @DisplayName("가수 생성시 솔로 그룹을 동시에 생성합니다.")
    @Test
    void createGroupWhileArtistCreating() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        Artist artist = artistRepository.findById(ids.get(0)).orElseThrow();
        GroupRequestDto request = GroupRequestDto.convertFrom(artist);

        // when
        Group group = groupService.create(request);

        // then
        Group result = groupRepository.findById(group.getGroupId()).orElseThrow();
        File profileImage = new File(fileHandler.getGroupProfileDirectoryPath() + result.getGroupId() + ".jpg");

        assertThat(result)
                .extracting("groupName", "groupType", "groupSize", "agency")
                .containsExactly("가수1", GroupType.SOLO, 1, "회사");
        assertThat(result.getMembers()).hasSize(1);
        assertThat(profileImage).exists();
    }

    @DisplayName("그룹 정보를 수정합니다.")
    @Test
    void updateGroup() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        Group group = groupService.create(createGroupRequest("그룹명", List.of(ids.get(0), ids.get(1))));

        GroupRequestDto request = GroupRequestDto.builder()
                .groupName("새그룹명")
                .groupType("GROUP")
                .agency("새회사")
                .profileImage(getProfileImageFile())
                .artistIds(List.of(ids.get(0), ids.get(1), ids.get(2)))
                .build();

        // when
        groupService.update(group.getGroupId(), request);

        // then
        Group result = groupRepository.findById(group.getGroupId()).orElseThrow();
        File profileImage = new File(fileHandler.getGroupProfileDirectoryPath() + result.getGroupId() + ".jpg");

        assertThat(result)
                .extracting("groupName", "groupType", "groupSize", "agency")
                .containsExactly("새그룹명", GroupType.GROUP, 3, "새회사");
        assertThat(result.getMembers()).hasSize(3);
        assertThat(profileImage).exists();
    }

    @DisplayName("그룹을 삭제합니다.")
    @Test
    void deleteGroup() throws Exception {
        // given
        List<Long> ids = getArtistIds();
        Group group = groupService.create(createGroupRequest("그룹명", List.of(ids.get(0), ids.get(1))));

        // when
        groupService.delete(group.getGroupId());

        // then
        Optional<Group> opGroup = groupRepository.findById(group.getGroupId());
        assertThat(opGroup.isPresent()).isFalse();
    }

    @DisplayName("그룹 상세 정보를 조회합니다.")
    @Test
    void readGroup() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        Group group = groupService.create(createGroupRequest("그룹명", List.of(ids.get(0), ids.get(1))));

        // when
        Group result = groupService.read(group.getGroupId());

        // then
        assertThat(result)
                .extracting("groupName", "groupType", "groupSize", "agency")
                .containsExactly("그룹명", GroupType.GROUP, 2, "회사");
    }

    @DisplayName("존재하지 않는 그룹을 조회하면 예외가 발생합니다.")
    @Test
    void readGroupNotExists() {
        // when then
        assertThatThrownBy(() -> groupService.read(999999L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("전체 그룹 목록을 조회합니다.")
    @Test
    void listGroup() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        groupService.create(createGroupRequest("그룹1", List.of(ids.get(0), ids.get(1))));
        groupService.create(createGroupRequest("그룹2", List.of(ids.get(1), ids.get(2))));
        groupService.create(createGroupRequest("그룹3", List.of(ids.get(1))));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Group> result = groupService.list(pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3)
                .extracting("groupName")
                .containsExactlyInAnyOrder("그룹1", "그룹2", "그룹3");
    }

    @DisplayName("그룹 목록에서 이름으로 검색합니다.")
    @Test
    void searchGroup() throws IOException {
        // given
        List<Long> ids = getArtistIds();
        groupService.create(createGroupRequest("가나다라", List.of(ids.get(0), ids.get(1))));
        groupService.create(createGroupRequest("다라마바", List.of(ids.get(1), ids.get(2))));
        groupService.create(createGroupRequest("아자차카", List.of(ids.get(1))));

        PageRequest pageRequest = PageRequest.of(0, 4);

        // when
        Page<Group> result = groupService.search("다라", pageRequest);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2)
                .extracting("groupName")
                .containsExactlyInAnyOrder("가나다라", "다라마바");
    }

    private GroupRequestDto createGroupRequest(String groupName, List<Long> artistIds) throws IOException {
        return GroupRequestDto.builder()
                .groupName(groupName)
                .groupType("GROUP")
                .agency("회사")
                .profileImage(getProfileImageFile())
                .artistIds(artistIds)
                .build();
    }

    private ArtistRequestDto createArtistRequest(String artistName) throws IOException {
        return ArtistRequestDto.builder()
                .artistName(artistName)
                .gender("MALE")
                .activityPeriod("활동년대")
                .nation("국적")
                .agency("회사")
                .profileImage(getProfileImageFile())
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

    private List<Long> getArtistIds() {
        return artistRepository.findAll().stream()
                .map(Artist::getArtistId)
                .toList();
    }
}