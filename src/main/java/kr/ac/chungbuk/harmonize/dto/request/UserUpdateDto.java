package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserUpdateDto {

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,100}|" +
            "(?=.*[A-Za-z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}|" +
            "(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$")
    private String password;

    @Email
    private String email;

    @Size(min = 2, max = 12)
    private String nickname;

    private String gender;

    private Integer age;

    private List<String> genre;
}
