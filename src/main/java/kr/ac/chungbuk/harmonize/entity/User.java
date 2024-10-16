package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user")
public class User implements UserDetails {

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
    @Column(nullable = true)
    private Gender gender; // 성별 (male, female, other)

    @Column(nullable = true)
    private Integer age; // 나이

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(value = EnumType.STRING)
    private List<Genre> genre; // 선호 장르

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

    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(this.role.name()));

        return authorities;
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return (!isLocked && !isBanned && !isDeleted);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return (!isLocked && !isBanned && !isDeleted);
    }


    /* 관계(Relationships) */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<Bookmark> bookmarks = new ArrayList<>();

    public void addBookmark(Bookmark bookmark) {
        this.bookmarks.add(bookmark);

        if (bookmark.getUser() != this)
            bookmark.setUser(this);
    }

}
