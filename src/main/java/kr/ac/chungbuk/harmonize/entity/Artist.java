package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.Gender;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artistId;

    @Column(length = 50, nullable = false)
    private String artistName;                  // 가수 이름

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Gender gender;                      // 가수 성별

    @Column(length = 100)
    private String activityPeriod;              // 활동 연대

    @Column(length = 100)
    private String nation;                      // 국적

    @Column(length = 100)
    private String agency;                      // 소속사

    @Column(nullable = true)
    private String profileImage;                // 가수 프로필 이미지

    @OneToMany(mappedBy = "artist", fetch = FetchType.EAGER)
    private List<GroupMember> groups;
}
