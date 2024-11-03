package kr.ac.chungbuk.harmonize.dto.response;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.RecomMusic;
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
public class RecomMusicListDto extends MusicListDto {

    private Integer rank;
    private Double score;

    public static RecomMusicListDto build(RecomMusic recomMusic) {
        Music music = recomMusic.getRecomMusic();

        RecomMusicListDto dto = new RecomMusicListDto();
        dto.setRank(recomMusic.getRank());
        dto.setScore(recomMusic.getScore());
        return (RecomMusicListDto) MusicListDto.build(dto, music);
    }
}
