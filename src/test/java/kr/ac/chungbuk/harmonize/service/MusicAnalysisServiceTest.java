package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.IntegrationTestSupport;
import kr.ac.chungbuk.harmonize.dto.request.MusicRequestDto;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.MusicAnalysis;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Role;
import kr.ac.chungbuk.harmonize.enums.Status;
import kr.ac.chungbuk.harmonize.repository.MusicAnalysisRepository;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.repository.UserRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import kr.ac.chungbuk.harmonize.utility.FileUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.kafka.requestreply.RequestReplyMessageFuture;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@SuppressWarnings("unchecked")
class MusicAnalysisServiceTest extends IntegrationTestSupport {

    @Autowired
    MusicAnalysisService musicAnalysisService;
    @Autowired
    MusicAnalysisRepository musicAnalysisRepository;
    @Autowired
    MusicService musicService;
    @Autowired
    MusicRepository musicRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    FileHandler fileHandler;

    @Value("${file.dir}")
    String fileDir;

    @AfterEach
    void tearDown() {
        musicAnalysisRepository.deleteAllInBatch();
        musicRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

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

    @DisplayName("음악 분석 모델로 모델을 돌리지 않고 기존 값만 재분석 요청합니다.")
    @Test
    void analyzeWithoutModel() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        copySampleTestResultXlsxFile(music);

        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        // when
        musicAnalysisService.analyzeWithoutModel(music.getMusicId());

        // then
        verify(kafkaTemplate).send(
                eq("musicAnalysis"),
                contains("\"command\": \"analysis_offline\"")
        );
    }

    @DisplayName("음악 재분석 요청시 모델 결과 파일이 없으면 예외가 발생합니다.")
    @Test
    void analyzeWithoutModelNoXlsxFile() throws IOException {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        // when then
        assertThatThrownBy(() -> musicAnalysisService.analyzeWithoutModel(music.getMusicId()))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage(music.getMusicId() + "번 음악 xlsx 파일이 존재하지 않음");
    }

    @DisplayName("음악 분석 결과에서 특정 Pitch 값을 제거합니다.")
    @Test
    void deletePitch() throws Exception {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        copySampleTestResultXlsxFile(music);
        MusicAnalysis analysis = musicAnalysisRepository.save(new MusicAnalysis(music.getMusicId(), Status.COMPLETE));
        music.setAnalysis(analysis);
        musicRepository.save(music);

        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        // when
        musicAnalysisService.deletePitch(music.getMusicId(), 0.123);

        // then
        verify(kafkaTemplate).send(
                eq("musicAnalysis"),
                contains("\"command\": \"delete\"")
        );
    }

    @DisplayName("음악 분석 결과에서 특정 범위 Pitch 값 전체를 제거합니다.")
    @Test
    void deletePitchRange() throws Exception {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        copySampleTestResultXlsxFile(music);
        MusicAnalysis analysis = musicAnalysisRepository.save(new MusicAnalysis(music.getMusicId(), Status.COMPLETE));
        music.setAnalysis(analysis);
        musicRepository.save(music);

        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        // when
        musicAnalysisService.deletePitchRange(music.getMusicId(), 0.123, "upper");

        // then
        verify(kafkaTemplate).send(
                eq("musicAnalysis"),
                contains("\"command\": \"delete\"")
        );
    }

    @DisplayName("음악 분석 결과 수정 요청시 분석이 완료되지 않았으면 예외가 발생합니다.")
    @Test
    void deletePitchNotComplete() throws Exception {
        // given
        MusicRequestDto musicRequest = createMusicRequest("audio");
        Music music = musicService.create(musicRequest);
        MultipartFile audioFile = getAudioFile();
        musicAnalysisService.updateAudioFile(audioFile);

        copySampleTestResultXlsxFile(music);

        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        // when then
        assertThatThrownBy(() -> musicAnalysisService.deletePitch(music.getMusicId(), 0.123))
            .isInstanceOf(Exception.class)
            .hasMessage("Analysis status is not COMPLETE");
        assertThatThrownBy(() -> musicAnalysisService.deletePitchRange(music.getMusicId(), 0.123, "upper"))
                .isInstanceOf(Exception.class)
                .hasMessage("Analysis status is not COMPLETE");
    }

    @DisplayName("콘텐츠 기반 추천 결과 업데이트 요청을 보냅니다.")
    @Test
    void requestContentBasedRec() {
        // given
        given(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .willReturn(null);

        // when
        musicAnalysisService.requestContentBasedRec();

        // then
        verify(kafkaTemplate).send(
                eq("musicRecSys"),
                contains("\"command\": \"content-based\"")
        );
    }

    @DisplayName("전체 회원 대상 추천 결과 업데이트 요청을 보냅니다.")
    @Test
    void requestCollaborativeRec() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        String expectedPayload = "all";
        GenericMessage mockMessage = new GenericMessage<>(expectedPayload);
        RequestReplyMessageFuture<String, String> mockFuture = mock(RequestReplyMessageFuture.class);

        given(mockFuture.get(20, TimeUnit.SECONDS)).willReturn(mockMessage);
        given(kafkaTemplate.sendAndReceive(any(Message.class))).willReturn(mockFuture);

        // when
        Object payload = musicAnalysisService.requestCollaborativeRec();

        // then
        assertThat(payload).isEqualTo(expectedPayload);
    }

    @DisplayName("한 명의 회원 대상 추천 결과 업데이트 요청을 보냅니다.")
    @Test
    void requestCollaborativeRecOne() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        User user = userRepository.save(createUser());

        String expectedPayload = String.valueOf(user.getUserId());
        GenericMessage mockMessage = new GenericMessage<>(expectedPayload);
        RequestReplyMessageFuture<String, String> mockFuture = mock(RequestReplyMessageFuture.class);

        given(mockFuture.get(20, TimeUnit.SECONDS)).willReturn(mockMessage);
        given(kafkaTemplate.sendAndReceive(any(Message.class))).willReturn(mockFuture);

        // when
        Object payload = musicAnalysisService.requestCollaborativeRecOne(user.getUserId());

        // then
        assertThat(payload).isEqualTo(expectedPayload);
    }

    @DisplayName("모델의 현재 상태를 확인합니다.")
    @Test
    void checkSystemStatus() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        String expectedPayload = "pong";
        GenericMessage mockMessage = new GenericMessage<>(expectedPayload);
        RequestReplyMessageFuture<String, String> mockFuture = mock(RequestReplyMessageFuture.class);

        given(mockFuture.get(2, TimeUnit.SECONDS)).willReturn(mockMessage);
        given(kafkaTemplate.sendAndReceive(any(Message.class))).willReturn(mockFuture);

        // when
        Map<String, Boolean> status = musicAnalysisService.checkSystemStatus();

        // then
        assertThat(status.get("musicAnalysis")).isTrue();
        assertThat(status.get("recSys")).isTrue();
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

    private User createUser() {
        return User.builder()
                .loginId("loginId")
                .password("password")
                .email("email@email.com")
                .nickname("홍길동")
                .role(Role.USER)
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

    private void copySampleTestResultXlsxFile(Music music) throws IOException {
        Path folderPath = Paths.get(fileHandler.getAudioDirectoryPath() + music.getMusicId());
        Files.createDirectory(folderPath);

        Path source = Paths.get("src/test/resources/pitch.xlsx");
        Path target = Paths.get(fileHandler.getAudioDirectoryPath() + music.getMusicId() + "/pitch.xlsx");
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}