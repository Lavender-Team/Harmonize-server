package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class UserAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisId;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @Column(nullable = false)
    private Double highestPitch;

    @Column(nullable = false)
    private Double lowestPitch;

    protected UserAnalysis() { }

    public UserAnalysis(double highestPitch, double lowestPitch) {
        this.highestPitch = highestPitch;
        this.lowestPitch = lowestPitch;
        analysisDate = LocalDateTime.now();
    }
}
