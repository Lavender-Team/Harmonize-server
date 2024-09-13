package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;        // 북마크 ID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id", nullable=false)
    private User user;              // 유저 ID

    public void setUser(User user) {
        this.user = user;

        if (user != null && !user.getBookmarks().contains(this)) {
            user.getBookmarks().add(this);
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;            // 음악 ID


    private LocalDateTime createdAt; // 생성일시


    public Bookmark(User user, Music music) {
        this.setUser(user);
        this.music = music;
        this.createdAt = LocalDateTime.now();
    }
}
