package kr.ac.chungbuk.harmonize.dto;

import kr.ac.chungbuk.harmonize.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long userId;
    private String loginId;
    private String email;
    private String nickname;
    private String role;
    private String gender;
    private Integer age;
    private Boolean isDeleted;
    private Boolean isBanned;
    private Boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static UserDTO build(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .gender(user.getGender().name())
                .age(user.getAge())
                .isDeleted(user.getIsDeleted())
                .isBanned(user.getIsBanned())
                .isLocked(user.getIsLocked())
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
