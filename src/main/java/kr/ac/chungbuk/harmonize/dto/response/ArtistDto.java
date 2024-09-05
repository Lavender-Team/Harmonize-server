package kr.ac.chungbuk.harmonize.dto.response;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistDto {

    private Long id;
    private String artistName;
    private String gender;
    private String genderName;
    private String activityPeriod;
    private String nation;
    private String agency;
    private String profileImage;

    public static ArtistDto build(Artist artist) {
        return ArtistDto.builder()
                .id(artist.getArtistId())
                .artistName(artist.getArtistName())
                .gender(artist.getGender().name())
                .genderName(Gender.toString(artist.getGender()))
                .activityPeriod(artist.getActivityPeriod())
                .nation(artist.getNation())
                .agency(artist.getAgency())
                .profileImage(artist.getProfileImage())
                .build();
    }
}
