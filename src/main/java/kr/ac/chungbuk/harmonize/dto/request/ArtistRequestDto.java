package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
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
}
