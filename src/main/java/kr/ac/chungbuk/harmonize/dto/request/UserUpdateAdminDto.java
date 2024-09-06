package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ac.chungbuk.harmonize.enums.Role;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserUpdateAdminDto extends UserUpdateDto {

    @Pattern(regexp = "^(USER|ADMIN|MODERATOR)$")
    private String role;

    private Boolean isDeleted;

    private Boolean isBanned;

    private Boolean isLocked;

}
