package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long themeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musicId", nullable = false)
    private Music music;

    @Column(length = 20, nullable = false)
    private String themeName;               // 테마 명

    public Theme() {}

    public Theme(Music music, String themeName) {
        this.music = music;
        this.themeName = themeName;
    }
}
