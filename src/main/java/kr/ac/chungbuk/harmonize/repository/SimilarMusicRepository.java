package kr.ac.chungbuk.harmonize.repository;

import jakarta.persistence.LockModeType;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.SimilarMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimilarMusicRepository extends JpaRepository<SimilarMusic, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(value = "SELECT sm FROM SimilarMusic sm WHERE sm.target = :target ORDER BY FUNCTION('RAND') LIMIT 6")
    List<SimilarMusic> findByTarget(Music target);
}
