package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MusicRequestDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    @Pattern(regexp = "^(KPOP|POP|BALLADE|RAP|DANCE|JPOP|RNB|FOLK|ROCK|OST|INDIE|TROT|KID)$")
    private String genre;

    private MultipartFile albumCover;

    @NotBlank
    @Size(min = 1, max = 50)
    private String karaokeNum;

    private LocalDateTime releaseDate;

    private String playLink;

    private Long groupId;

    List<String> themes;
}
