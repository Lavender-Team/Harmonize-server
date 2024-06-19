package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.service.MusicService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.FileInputStream;
import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void create() throws Exception {
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

        // When & Then
        mvc.perform(
                multipart("/api/music")
                        .file("albumCover", albumCover.getBytes())
                        .param("title", "주저하는 연인들을 위해(테스트)")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
                        .param("themes", "부드러운 목소리, 비올 때")
        ).andExpect(status().isCreated());

        Music uploaded = musicRepository.findByTitle("주저하는 연인들을 위해(테스트)").orElseThrow();
        musicService.delete(uploaded.getMusicId());
    }

    @Test
    void delete() throws Exception {
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
                        .param("title", "주저하는 연인들을 위해(테스트)")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
                        .param("themes", "부드러운 목소리, 비올 때")
        ).andExpect(status().isCreated());

        // When
        Music uploaded = musicRepository.findByTitle("주저하는 연인들을 위해(테스트)").orElseThrow();

        mvc.perform(MockMvcRequestBuilders.delete(new URI("/api/music/" + uploaded.getMusicId())))
                .andExpect(status().isAccepted());

        // Then
        Assertions.assertThat(musicRepository.findByTitle("주저하는 연인들을 위해(테스트)").isEmpty());
    }

    @Test
    void list() throws Exception {
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
                        .param("title", "주저하는 연인들을 위해(테스트)_1")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
                        .param("themes", "부드러운 목소리, 비올 때")
        ).andExpect(status().isCreated());

        mvc.perform(
                multipart("/api/music")
                        .file("albumCover", albumCover.getBytes())
                        .param("title", "주저하는 연인들을 위해(테스트)_2")
                        .param("genre", "INDIE")
                        .param("karaokeNum", "TJ 53651")
                        .param("releaseDate", "2019-03-13T00:00:00")
                        .param("playLink", "https://youtu.be/1gmleC0dOYY?si=ZFejSnIAzEEZx7Xd")
        ).andExpect(status().isCreated());

        // When & Then
        MvcResult result = mvc.perform(get(new URI("/api/music?page=0&size=2")))
                .andExpect(status().isOk())
                .andReturn();

        // TODO result content를 테스트하도록 구체화

        Music uploaded1 = musicRepository.findByTitle("주저하는 연인들을 위해(테스트)_1").orElseThrow();
        Music uploaded2 = musicRepository.findByTitle("주저하는 연인들을 위해(테스트)_2").orElseThrow();
        musicService.delete(uploaded1.getMusicId());
        musicService.delete(uploaded2.getMusicId());
    }
}