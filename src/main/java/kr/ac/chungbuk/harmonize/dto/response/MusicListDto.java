package kr.ac.chungbuk.harmonize.dto.response;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicListDto {

    private Long id;
    private String title;
    private String artist;
    private String genre;
    private String genreName;
    private String status;
    private String albumCover;
    private Long view;
    private Long likes;
    private List<String> themes;

    public static MusicListDto build(Music music) {
        return MusicListDto.builder()
                .id(music.getMusicId())
                .title(music.getTitle())
                .artist((music.getGroup() == null) ? "-" : music.getGroup().getGroupName())
                .genre(music.getGenre().name())
                .genreName(Genre.toString(music.getGenre()))
                .status(music.getAnalysis().getStatus().name())
                .albumCover(music.getAlbumCover())
                .view(music.getView())
                .likes(music.getLikes())
                .themes(music.getThemes().stream().map(Theme::getThemeName).toList())
                .build();
    }
}
