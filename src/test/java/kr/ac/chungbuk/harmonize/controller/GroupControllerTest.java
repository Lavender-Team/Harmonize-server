package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.dto.request.ArtistRequestDto;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.repository.GroupRepository;
import kr.ac.chungbuk.harmonize.service.ArtistService;
import kr.ac.chungbuk.harmonize.service.GroupService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ArtistService artistService;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Given
        Long artistId = createArtist("<테스트가수명>");
        Long artistId2 = createArtist("<테스트가수명2>");

        mvc.perform(
                multipart("/api/group")
                        .file("profileImage", getProfileImage().getBytes())
                        .param("groupName", "<테스트그룹명>")
                        .param("groupType", "GROUP")
                        .param("agency", "테스트엔터사")
                        .param("artistIds", artistId+","+artistId2)
        ).andExpect(status().isCreated());
    }

    @AfterEach
    void cleanUp() throws Exception {
        Optional<Group> group = groupRepository.findByGroupName("<테스트그룹명>");
        if (group.isPresent())
            groupService.delete(group.get().getGroupId());
        Optional<Artist> artist = artistRepository.findByArtistName("<테스트가수명>");
        if (artist.isPresent())
            artistService.delete(artist.get().getArtistId());
        Optional<Artist> artist2 = artistRepository.findByArtistName("<테스트가수명2>");
        if (artist.isPresent())
            artistService.delete(artist2.get().getArtistId());
    }

    @Test
    void create() {
        // When & Then
        Group group = groupRepository.findByGroupName("<테스트그룹명>").orElseThrow();
        assertThat(group.getGroupSize().equals(2));
    }

    @Test
    void update() throws Exception {
        // Given
        Long groupId = groupRepository.findByGroupName("<테스트그룹명>").orElseThrow().getGroupId();
        Long artistId = artistRepository.findByArtistName("<테스트가수명>").orElseThrow().getArtistId();

        // When
        mvc.perform(put(new URI("/api/group/" + groupId))
                .param("groupName", "<테스트그룹명>")
                .param("groupType", "SOLO")
                .param("artistIds", artistId.toString()))
        .andExpect(status().isAccepted());

        // Then
        Group group = groupRepository.findByGroupName("<테스트그룹명>").orElseThrow();
        assertThat(group.getGroupSize().equals(1));
    }

    @Test
    void list() throws Exception {
        // When & Then
        mvc.perform(get(new URI("/api/group?page=0&size=1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].groupName").value(Matchers.is("<테스트그룹명>")));
    }

    @Test
    void readByAdmin() throws Exception {
        // Given
        Long groupId = groupRepository.findByGroupName("<테스트그룹명>").orElseThrow().getGroupId();

        // When & Then
        mvc.perform(get(new URI("/api/group/" + groupId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.groupName").value(Matchers.is("<테스트그룹명>")));
    }


    private MockMultipartFile getProfileImage() throws IOException {
        final String filename = "albumcover.jpg";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile profileImage = new MockMultipartFile(
                "images",
                filename,
                "jpg",
                fileInputStream
        );
        return profileImage;
    }

    private Long createArtist(String name) throws Exception {
        ArtistRequestDto artistParam = new ArtistRequestDto();
        artistParam.setArtistName(name);
        artistParam.setGender("FEMALE");
        artistParam.setProfileImage(getProfileImage());

        return artistService.create(artistParam).getArtistId();
    }
}