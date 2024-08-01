package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artistId", nullable = false)
    private Artist artist;
}
