package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.Genre;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long musicId;

    @Column(length = 50, nullable = false)
    private String title;               // 노래 제목

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Genre genre;                // 장르

    @Column(nullable = true)
    private String albumCover;          // 앨범 표지

    @Column(length = 50, nullable = false)
    private String karaokeNum;          // 노래방 번호

    @Column(nullable = true)
    private LocalDateTime releaseDate;  // 발매일

    @Column(nullable = true)
    private Integer length;             // 노래 길이(초)

    @Column(nullable = true)
    private String playLink;            // 재생 링크

    @Column(columnDefinition = "TEXT", nullable = true)
    private String lyrics;              // 가사

    @Column(nullable = true)
    private String audioFile;           // 음악 파일

    private Long view;                  // 조회 수
    private Long likes;                 // 좋아요 수

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "musicId", nullable = false)
    private MusicAnalysis analysis;     // 음악 분석 결과

    @OneToMany(mappedBy = "music", fetch = FetchType.EAGER)
    private List<Theme> themes;

}
