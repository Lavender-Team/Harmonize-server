package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class GroupRequestDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String groupName;

    @Pattern(regexp = "^(SOLO|GROUP)$")
    private String groupType;

    @Size(max = 100)
    private String agency;

    private MultipartFile profileImage;

    List<Long> artistIds;
}
