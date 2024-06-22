package kr.ac.chungbuk.harmonize.dto;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
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
public class MusicDTO {

    private Long id;
    private String title;
    private String artist;
    private String genre;
    private String status;
    private String karaokeNum;
    private LocalDateTime releaseDate;
    private String albumCover;
    private String playLink;
    private Long view;
    private Long likes;
    private List<String> themes;
    private String audioFile;
    private String lyrics;

    public static MusicDTO build(Music music) {
        return MusicDTO.builder()
                .id(music.getMusicId())
                .title(music.getTitle())
                .artist("구현안됨")
                .genre(music.getGenre().name())
                .status(music.getAnalysis().getStatus().name())
                .karaokeNum(music.getKaraokeNum())
                .releaseDate(music.getReleaseDate())
                .albumCover(music.getAlbumCover())
                .playLink(music.getPlayLink())
                .view(music.getView())
                .likes(music.getLikes())
                .themes(music.getThemes().stream().map(Theme::getThemeName).toList())
                .audioFile(music.getAudioFile())
                .lyrics(music.getLyrics())
                .build();
    }
}
