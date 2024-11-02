package kr.ac.chungbuk.harmonize.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicDto {

    private Long id;
    private String title;
    private String artist;
    private GroupDto group;
    private String genre;
    private String genreName;
    private String karaokeNum;
    private LocalDateTime releaseDate;
    private String albumCover;
    private String playLink;
    private Long view;
    private Long likes;
    private Boolean isBookmarked;
    private List<String> themes;
    private String audioFile;
    private String lyrics;

    private String status;
    private Double highestPitch;
    private Double lowestPitch;
    private PitchStatDto pitchStat;

    private Double highPitchRatio;
    private Double highPitchCont;
    private Double lowPitchRatio;
    private Double lowPitchCont;
    private Integer steepSlope;
    private Integer level;

    private List<MusicListDto> similarMusics;

    public static MusicDto build(Music music, ObjectMapper objectMapper, List<Music> similarMusics, boolean isBookmarked) {

        PitchStatDto pitchStat = null;
        try {
            pitchStat = objectMapper.readValue(
                    music.getAnalysis().getPitchStat().replace("'", "\"").toLowerCase(),
                    PitchStatDto.class
            );
        } catch (Exception ignored) { }

        return MusicDto.builder()
                .id(music.getMusicId())
                .title(music.getTitle())
                .artist(music.getGroup() == null ? "-" : music.getGroup().getGroupName())
                .group(music.getGroup() == null ? null : GroupDto.build(music.getGroup()))
                .genre(music.getGenre().name())
                .genreName(Genre.toString(music.getGenre()))
                .karaokeNum(music.getKaraokeNum())
                .releaseDate(music.getReleaseDate())
                .albumCover(music.getAlbumCover())
                .playLink(music.getPlayLink())
                .view(music.getView())
                .likes(music.getLikes())
                .isBookmarked(isBookmarked)
                .themes(music.getThemes().stream().map(Theme::getThemeName).toList())
                .audioFile(music.getAudioFile())
                .lyrics(music.getLyrics())
                .status(music.getAnalysis().getStatus().name())
                .highestPitch(music.getAnalysis().getHighestPitch())
                .lowestPitch(music.getAnalysis().getLowestPitch())
                .pitchStat(pitchStat)
                .highPitchRatio(music.getAnalysis().getHighPitchRatio())
                .highPitchCont(music.getAnalysis().getHighPitchCont())
                .lowPitchRatio(music.getAnalysis().getLowPitchRatio())
                .lowPitchCont(music.getAnalysis().getLowPitchCont())
                .steepSlope(music.getAnalysis().getSteepSlope())
                .level(music.getAnalysis().getLevel())
                .similarMusics(similarMusics.stream().map(MusicListDto::build).toList())
                .build();
    }
}
