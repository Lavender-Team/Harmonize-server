package kr.ac.chungbuk.harmonize.dto;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistDTO {

    private Long id;
    private String artistName;
    private String gender;
    private String genderName;
    private String activityPeriod;
    private String nation;
    private String agency;
    private String profileImage;

    public static ArtistDTO build(Artist artist) {
        return ArtistDTO.builder()
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
