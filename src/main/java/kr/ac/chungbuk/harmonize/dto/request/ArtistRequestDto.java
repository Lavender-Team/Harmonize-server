package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ArtistRequestDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String artistName;

    private String gender;

    private MultipartFile profileImage;

    @Size(max = 100)
    private String activityPeriod;

    @Size(max = 100)
    private String nation;

    @Size(max = 100)
    private String agency;

    private Boolean createSoloGroup;


    @Builder
    public ArtistRequestDto(String artistName, String gender, MultipartFile profileImage, String activityPeriod,
                            String nation, String agency, Boolean createSoloGroup) {
        this.artistName = artistName;
        this.gender = gender;
        this.profileImage = profileImage;
        this.activityPeriod = activityPeriod;
        this.nation = nation;
        this.agency = agency;
        this.createSoloGroup = createSoloGroup;
    }
}
