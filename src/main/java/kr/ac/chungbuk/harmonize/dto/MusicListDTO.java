package kr.ac.chungbuk.harmonize.dto;

import kr.ac.chungbuk.harmonize.entity.Music;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MusicListDTO {

    private Long id;
    private String title;
    private String artist;
    private String genre;
    private String status;
    private Long view;
    private Long likes;

    public static MusicListDTO build(Music music) {
        return MusicListDTO.builder()
                .id(music.getMusicId())
                .title(music.getTitle())
                .artist("구현안됨")
                .genre(music.getGenre().name())
                .status("INCOMPLETE")
                .view(music.getView())
                .likes(music.getLikes())
                .build();
    }
}
