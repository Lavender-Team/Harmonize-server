package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.Status;
import lombok.Data;

@Entity
@Data
public class MusicAnalysis {

    @Id
    private Long musicId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(nullable = true)
    private Double highestPitch;
    @Column(nullable = true)
    private Double highPitchRatio;
    @Column(nullable = true)
    private Double highPitchCont;

    @Column(nullable = true)
    private Double lowestPitch;
    @Column(nullable = true)
    private Double lowPitchRatio;
    @Column(nullable = true)
    private Double lowPitchCont;

    @Column(nullable = true)
    private Integer steepSlope;

    @Column(nullable = true)
    private Integer level;

    @Column(nullable = true)
    private String pitchFile;

    public MusicAnalysis(Long musicId) {
        this.musicId = musicId;
        this.status = Status.INCOMPLETE;
    }

    public MusicAnalysis() {}
}
