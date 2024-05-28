package kr.ac.chungbuk.harmonize.controller;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.service.MusicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        ).andExpect(status().isCreated());

        Music uploaded = musicRepository.findByTitle("주저하는 연인들을 위해(테스트)").orElseThrow();
        musicService.delete(uploaded.getMusicId());
    }
}