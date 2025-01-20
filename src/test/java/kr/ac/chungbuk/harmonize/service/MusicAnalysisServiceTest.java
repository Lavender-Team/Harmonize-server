package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.config.KafkaTopicConfig;
import kr.ac.chungbuk.harmonize.config.ScheduledTask;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.repository.MusicAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import kr.ac.chungbuk.harmonize.utility.FileUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@MockBeans({
        @MockBean(KafkaTopicConfig.class),
        @MockBean(ReplyingKafkaTemplate.class),
        @MockBean(ScheduledTask.class)
})
class MusicAnalysisServiceTest {

    @Autowired
    MusicAnalysisService musicAnalysisService;
    @Autowired
    MusicAnalysisRepository musicAnalysisRepository;
    @Autowired
    MusicService musicService;
    @Autowired
    MusicRepository musicRepository;
    @Autowired
    FileHandler fileHandler;

    @Value("${file.dir}")
    String fileDir;

    @MockBean
    ReplyingKafkaTemplate<String, String, String> kafkaTemplate;

    @AfterEach
    void tearDown() {
        musicAnalysisRepository.deleteAllInBatch();
        musicRepository.deleteAllInBatch();

        FileUtils.deleteFolderContents(new File(fileDir));
    }

    @DisplayName("음악 오디오 파일을 업로드합니다.")
    @Test
    void updateFilesAudio() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("음악");
        Music music = musicService.create(musicRequest);

        MultipartFile audioFile = getAudioFile();

        // when
        musicAnalysisService.updateFiles(music.getMusicId(), audioFile, null);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        File audioFileSaved = new File(fileHandler.getAudioDirectoryPath() + music.getMusicId() + ".mp3");

        assertThat(result.getAudioFile()).isEqualTo("/api/music/audio/"+music.getMusicId()+".mp3");
        assertThat(audioFileSaved).exists();
    }

    @DisplayName("가사 텍스트 파일을 업로드합니다.")
    @Test
    void updateFilesLyric() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("음악");
        Music music = musicService.create(musicRequest);

        MultipartFile lyricFile = getLyricFile("lyrics.txt");

        // when
        musicAnalysisService.updateFiles(music.getMusicId(), null, lyricFile);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        assertThat(result.getLyrics()).isEqualTo("가사 업로드");
    }

    @DisplayName("가사 파일의 용량이 10kb를 초과하면 예외가 발생합니다.")
    @Test
    void updateFilesLyricSizeExceeded() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("음악");
        Music music = musicService.create(musicRequest);

        MultipartFile lyricFile = getLyricFile("lyricsSizeExceeded.txt");

        // when then
        assertThatThrownBy(() -> musicAnalysisService.updateFiles(music.getMusicId(), null, lyricFile))
                .isInstanceOf(SizeLimitExceededException.class)
                .hasMessage("Too heavy lyricFile");
    }

    @DisplayName("(음악 벌크) 파일 이름과 일치하는 음악에 음악 파일을 업로드합니다.")
    @Test
    void updateAudioFile() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);

        MultipartFile audioFile = getAudioFile();

        // when
        musicAnalysisService.updateAudioFile(audioFile);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        File audioFileSaved = new File(fileHandler.getAudioDirectoryPath() + music.getMusicId() + ".mp3");

        assertThat(result.getAudioFile()).isEqualTo("/api/music/audio/"+music.getMusicId()+".mp3");
        assertThat(audioFileSaved).exists();
    }

    @DisplayName("(음악 벌크) 파일 이름과 일치하는 음악이 두 개 이상이면 예외가 발생합니다.")
    @Test
    void updateAudioFileDuplicateTitle() throws IOException {
        // given
        MusicRequestDto musicRequest1 = createMusicRequest("audio");
        Music music1 = musicService.create(musicRequest1);
        MusicRequestDto musicRequest2 = createMusicRequest("audio");
        Music music2 = musicService.create(musicRequest2);

        MultipartFile audioFile = getAudioFile();

        // when then
        assertThatThrownBy(() -> musicAnalysisService.updateAudioFile(audioFile))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("(음악 벌크) 파일 이름과 일치하는 음악이 존재하지 않으면 예외가 발생합니다.")
    @Test
    void updateAudioFileNotExists() throws IOException {
        // given
        MultipartFile audioFile = getAudioFile();

        // when then
        assertThatThrownBy(() -> musicAnalysisService.updateAudioFile(audioFile))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("(가사 벌크) 파일 이름과 일치하는 음악에 가사를 업로드합니다.")
    @Test
    void updateLyricFile() throws Exception {
        // given
        MusicRequestDto musicRequest = createMusicRequest("lyrics");
        Music music = musicService.create(musicRequest);

        MultipartFile lyricFile = getLyricFile("lyrics.txt");

        // when
        musicAnalysisService.updateLyricFile(lyricFile);

        // then
        Music result = musicRepository.findById(music.getMusicId()).orElseThrow();
        assertThat(result.getLyrics()).isEqualTo("가사 업로드");
    }

    @DisplayName("(가사 벌크) 파일 이름과 일치하는 음악이 두 개 이상이면 예외가 발생합니다.")
    @Test
    void updateLyricFileDuplicateTitle() throws IOException {
        // given
        MusicRequestDto musicRequest1 = createMusicRequest("lyrics");
        Music music1 = musicService.create(musicRequest1);
        MusicRequestDto musicRequest2 = createMusicRequest("lyrics");
        Music music2 = musicService.create(musicRequest2);

        MultipartFile lyricFile = getLyricFile("lyrics.txt");

        // when then
        assertThatThrownBy(() -> musicAnalysisService.updateLyricFile(lyricFile))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("(가사 벌크) 파일 이름과 일치하는 음악이 존재하지 않으면 예외가 발생합니다.")
    @Test
    void updateLyricFileNotExists() throws IOException {
        // given
        MultipartFile lyricFile = getLyricFile("lyrics.txt");

        // when then
        assertThatThrownBy(() -> musicAnalysisService.updateLyricFile(lyricFile))
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("음악 분석 모델로 분석 요청을 전송합니다.")
    @Test
    void analyze() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        double confidence = 0.8;

        // when
        musicAnalysisService.analyze(music.getMusicId(), confidence);

        // then
        verify(kafkaTemplate).send(
                eq("musicAnalysis"),
                contains("\"command\": \"analysis\"")
        );
    }

    @DisplayName("음악 분석 요청시 음악 파일이 업로드되지 않았으면 예외가 발생합니다.")
    @Test
    void analyzeNoAudioFile() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("음악");
        Music music = musicService.create(musicRequest);

        double confidence = 0.8;

        // when then
        assertThatThrownBy(() -> musicAnalysisService.analyze(music.getMusicId(), confidence))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("Audio file not uploaded");
    }


    private MusicRequestDto createMusicRequest(String title) {
        return MusicRequestDto.builder()
                .title(title)
                .genre("KPOP")
                .karaokeNum("12345")
                .releaseDate(LocalDateTime.of(
                        LocalDate.of(2000, 12, 21),
                        LocalTime.of(0, 0, 0)
                ))
                .playLink("link.com")
                .build();
    }

    private MockMultipartFile getAudioFile() throws IOException {
        final String filename = "audio.mp3";
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        return new MockMultipartFile(
                "audio",
                filename,
                "audio/mpeg",
                fileInputStream
        );
    }

    private MockMultipartFile getLyricFile(String filename) throws IOException {
        final String filePath = "src/test/resources/" + filename;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        return new MockMultipartFile(
                "file",
                filename,
                "text/plain",
                fileInputStream
        );
    }
}