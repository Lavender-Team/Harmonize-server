package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    @Column(length = 50, nullable = false)
    private String groupName;                       // 그룹 이름

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private GroupType groupType;                    // 그룹 형태 (솔로/그룹)
    
    @Column(nullable = false)
    private Integer groupSize;                      // 그룹 소속 인원 수

    @Column(length = 100)
    private String agency;                          // 소속사

    @Column(nullable = true)
    private String profileImage;                    // 그룹 프로필 이미지

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Music> musics;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private List<GroupMember> members;
}
