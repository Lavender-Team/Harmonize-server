package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSaveDto {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z\\d-_]{6,20}$")
    private String loginId;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,100}|" +
            "(?=.*[A-Za-z])(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}|" +
            "(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,100}$")
    private String password;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 2, max = 12)
    private String nickname;

    private String gender;
    private Integer age;
}
