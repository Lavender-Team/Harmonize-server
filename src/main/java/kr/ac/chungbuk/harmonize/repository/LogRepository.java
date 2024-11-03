package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    @Query("SELECT count(*) FROM Log l WHERE l.createdAt BETWEEN :startOfDay AND :endOfDay")
    long countCreatedToday(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
