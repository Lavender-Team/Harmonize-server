package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.service.MusicService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MusicControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MusicService musicService;
    @Autowired
    private MusicRepository musicRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Given
        final String filename = "albumcover.jpg";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile albumCover = new MockMultipartFile(
                "images",
                filename,
                "jpg",
                fileInputStream
        );

        mvc.perform(
                multipart("/api/music")
                        .file("albumCover", albumCover.getBytes())
                        .param("title", "<테스트 노래명>")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
                        .param("themes", "부드러운 목소리, 테스트 테마!")
        ).andExpect(status().isCreated());

        mvc.perform(
                multipart("/api/music")
                        .file("albumCover", albumCover.getBytes())
                        .param("title", "<테스트 노래명2>")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
        ).andExpect(status().isCreated());
    }

    @AfterEach
    void cleanUp() throws Exception {
        Optional<Music> uploaded1 = musicRepository.findByTitle("<테스트 노래명>");
        if (uploaded1.isPresent())
            musicService.delete(uploaded1.get().getMusicId());
        Optional<Music> uploaded2 = musicRepository.findByTitle("<테스트 노래명2>");
        if (uploaded2.isPresent())
            musicService.delete(uploaded2.get().getMusicId());
    }

    @Test
    void create() throws Exception {
        // When & Then
        Music uploaded = musicRepository.findByTitle("<테스트 노래명>").orElseThrow();
    }

    @Test
    void update() throws Exception {
        // Given
        Long musicId = musicRepository.findByTitle("<테스트 노래명>").orElseThrow().getMusicId();

        // When
        mvc.perform(put(new URI("/api/music/"+musicId))
                        .param("title", "<테스트 노래명>")
                        .param("genre", "ROCK"))
                .andExpect(status().isAccepted());

        // Then
        Music music = musicRepository.findByTitle("<테스트 노래명>").orElseThrow();
        assertThat(music.getGenre() == Genre.ROCK);
    }

    @Test
    void delete() throws Exception {
        // When
        Music uploaded = musicRepository.findByTitle("<테스트 노래명>").orElseThrow();

        mvc.perform(MockMvcRequestBuilders.delete(new URI("/api/music/" + uploaded.getMusicId())))
                .andExpect(status().isAccepted());

        // Then
        assertThat(musicRepository.findByTitle("<테스트 노래명>").isEmpty());
    }

    @Test
    void list() throws Exception {
        // When & Then
        mvc.perform(get(new URI("/api/music?page=0&size=2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value(Matchers.is("<테스트 노래명2>")))
                .andExpect(jsonPath("$.content[1].title").value(Matchers.is("<테스트 노래명>")));
    }

    @Test
    void listThemes() throws Exception {
        // When & Then
        mvc.perform(get(new URI("/api/music/theme")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()", Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void listMusicOfTheme() throws Exception {
        // When & Then
        mvc.perform(get(new URI("/api/music/theme/music")).param("themeName", "테스트 테마!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].themes").isArray())
                .andExpect(jsonPath("$.content[0].themes").value(Matchers.hasItem("테스트 테마!")));
    }

}