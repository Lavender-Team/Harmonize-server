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

    private Long musicId;
    private String title;

    public static MusicListDTO build(Music music) {
        return MusicListDTO.builder()
                .musicId(music.getMusicId())
                .title(music.getTitle())
                .build();
    }
}
