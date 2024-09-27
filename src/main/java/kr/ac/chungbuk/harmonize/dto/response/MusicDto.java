package kr.ac.chungbuk.harmonize.dto.response;

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
    private Double highPitchRatio;
    private Double highPitchCont;
    private Double lowestPitch;
    private Double lowPitchRatio;
    private Double lowPitchCont;
    private Integer steepSlope;
    private Integer level;

    public static MusicDto build(Music music, boolean isBookmarked) {
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
                .highPitchRatio(music.getAnalysis().getHighPitchRatio())
                .highPitchCont(music.getAnalysis().getHighPitchCont())
                .lowestPitch(music.getAnalysis().getLowestPitch())
                .lowPitchRatio(music.getAnalysis().getLowPitchRatio())
                .lowPitchCont(music.getAnalysis().getLowPitchCont())
                .steepSlope(music.getAnalysis().getSteepSlope())
                .level(music.getAnalysis().getLevel())
                .build();
    }
}
