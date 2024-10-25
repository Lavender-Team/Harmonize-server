package kr.ac.chungbuk.harmonize.entity;

import jakarta.persistence.*;
import kr.ac.chungbuk.harmonize.enums.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long event_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "targetId", nullable = false)
    private Music music;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventType event;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    protected Log() { }

    public Log(User user, Music music, EventType event) {
        this.user = user;
        this.music = music;
        this.event = event;
        createdAt = LocalDateTime.now();
    }
}
