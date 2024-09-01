package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String loginId; // 로그인 ID

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 역할 (user, admin, moderator)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // 성별 (male, female, other)

    @Column(nullable = false)
    private Integer age; // 나이

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성일자

    @Column(nullable = true)
    private LocalDateTime deletedAt; // 삭제일자

    @Column(nullable = false)
    private Boolean isDeleted = false; // 삭제 여부

    @Column(nullable = false)
    private Boolean isBanned = false; // 밴 여부

    @Column(nullable = false)
    private Boolean isLocked = false; // 계정 잠금 여부

    // Enum for Role
    public enum Role {
        USER, ADMIN, MODERATOR
    }

    // Enum for Gender
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public User() {
    }

    public User(String loginId, String password, String email, String nickname, Role role, Gender gender, Integer age) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.gender = gender;
        this.age = age;
        this.createdAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }
}
