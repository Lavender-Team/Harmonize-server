package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.service.ArtistService;
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
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtistControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ArtistService artistService;
    @Autowired
    private ArtistRepository artistRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Given
        final String filename = "albumcover.jpg";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile profileImage = new MockMultipartFile(
                "images",
                filename,
                "jpg",
                fileInputStream
        );

        mvc.perform(
                multipart("/api/artist")
                        .file("profileImage", profileImage.getBytes())
                        .param("artistName", "<테스트가수명>")
                        .param("gender", "MALE")
                        .param("activityPeriod", "")
                        .param("nation", "대한민국")
                        .param("agency", "소속사엔터사")
        ).andExpect(status().isCreated());
    }

    @AfterEach
    void cleanUp() throws Exception {
        Optional<Artist> artist = artistRepository.findByArtistName("<테스트가수명>");
        if (artist.isPresent())
            artistService.delete(artist.get().getArtistId());
    }

    @Test
    void create() {
        // When & Then
        Artist artist = artistRepository.findByArtistName("<테스트가수명>").orElseThrow();
    }

    @Test
    void update() throws Exception {
        // Given
        Long artistId = artistRepository.findByArtistName("<테스트가수명>").orElseThrow().getArtistId();

        // When
        mvc.perform(put(new URI("/api/artist/" + artistId))
                        .param("artistName", "<테스트가수명>")
                        .param("agency", "새소속사엔터사")
                        .param("activityPeriod", "2010 년대"))
                .andExpect(status().isAccepted());

        // Then
        Artist artist = artistRepository.findByArtistName("<테스트가수명>").orElseThrow();
        assertThat(artist.getAgency().equals("새소속사엔터사"));
    }

    @Test
    void list() throws Exception {
        // When & Then
        mvc.perform(get(new URI("/api/artist?page=0&size=1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].artistName").value(Matchers.is("<테스트가수명>")));
    }

    @Test
    void readByAdmin() throws Exception {
        // Given
        Long artistId = artistRepository.findByArtistName("<테스트가수명>").orElseThrow().getArtistId();

        // When & Then
        mvc.perform(get(new URI("/api/artist/" + artistId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.artistName").value(Matchers.is("<테스트가수명>")));
    }
}