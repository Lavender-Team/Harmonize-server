package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class UserAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysis_id;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @Column(nullable = false)
    private Double highestPitch;

    @Column(nullable = false)
    private Double lowestPitch;

}
