package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.Pattern;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Genre;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchRequestDto {

    private String query;

    @Pattern(regexp = "^(SOLO|GROUP)$")
    private String groupType;

    @Pattern(regexp = "^(KPOP|POP|BALLADE|RAP|DANCE|JPOP|RNB|FOLK|ROCK|OST|INDIE|TROT|KID)$")
    private String genre;

    // TODO: 음역대 검색 구현
}
