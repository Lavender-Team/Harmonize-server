package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MusicRequestDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

    @Pattern(regexp = "^(KPOP|POP|BALLADE|RAP|DANCE|JPOP|RNB|FOLK|ROCK|OST|INDIE|TROT|KID)$")
    private String genre;

    private MultipartFile albumCover;

    @Size(min = 1, max = 50)
    private String karaokeNum;

    private LocalDateTime releaseDate;

    private String playLink;

    private Long groupId;

    List<String> themes;


    @Builder
    public MusicRequestDto(String title, String genre, MultipartFile albumCover, String karaokeNum,
                           LocalDateTime releaseDate, String playLink, Long groupId, List<String> themes) {
        this.title = title;
        this.genre = genre;
        this.albumCover = albumCover;
        this.karaokeNum = karaokeNum;
        this.releaseDate = releaseDate;
        this.playLink = playLink;
        this.groupId = groupId;
        this.themes = themes;
    }
}
