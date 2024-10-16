package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class SimilarMusic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long similarMusicId;

    @Column(nullable = false)
    private LocalDateTime version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetId", unique = false)
    private Music target;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recomId", unique = false)
    private Music recom;

    @Column(nullable = false)
    private Integer rank;

    @Column(nullable = false)
    private Double score;
}
