package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RecomMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recomId;

    @Column(nullable = false)
    private LocalDateTime version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetUser", unique = false)
    private User target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recomMusic", unique = false)
    private Music recomMusic;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Double score;
}
