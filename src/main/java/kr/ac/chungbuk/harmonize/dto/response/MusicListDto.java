package kr.ac.chungbuk.harmonize.dto.response;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import kr.ac.chungbuk.harmonize.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicListDto {

    protected Long id;
    protected String title;
    protected String artist;
    protected String genre;
    protected String genreName;
    protected String status;
    protected String albumCover;
    protected Long view;
    protected Long likes;
    protected List<String> themes;

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

    public static MusicListDto build(MusicListDto dto, Music music) {
        dto.setId(music.getMusicId());
        dto.setTitle(music.getTitle());
        dto.setArtist((music.getGroup() == null) ? "-" : music.getGroup().getGroupName());
        dto.setGenre(music.getGenre().name());
        dto.setGenreName(Genre.toString(music.getGenre()));
        dto.setStatus(music.getAnalysis().getStatus().name());
        dto.setAlbumCover(music.getAlbumCover());
        dto.setView(music.getView());
        dto.setLikes(music.getLikes());
        dto.setThemes(music.getThemes().stream().map(Theme::getThemeName).toList());

        return dto;
    }
}
