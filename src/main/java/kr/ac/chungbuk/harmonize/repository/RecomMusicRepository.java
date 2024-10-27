package kr.ac.chungbuk.harmonize.repository;

import jakarta.persistence.LockModeType;
import kr.ac.chungbuk.harmonize.entity.RecomMusic;
import kr.ac.chungbuk.harmonize.entity.User;
import kr.ac.chungbuk.harmonize.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RecomMusicRepository extends JpaRepository<RecomMusic, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(value = "SELECT rm FROM RecomMusic rm WHERE rm.target = :user ORDER BY FUNCTION('RAND')",
            countQuery = "SELECT count(*) FROM RecomMusic rm WHERE rm.target = :user")
    Page<RecomMusic> findByUser(User user, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(value = "SELECT rm FROM RecomMusic rm INNER JOIN Music m ON rm.recomMusic = m " +
                   "WHERE rm.target = :user AND m.genre = :genre ORDER BY FUNCTION('RAND')",
            countQuery = "SELECT count(*) FROM RecomMusic rm INNER JOIN Music m ON rm.recomMusic = m " +
                         " WHERE rm.target = :user AND m.genre = :genre")
    Page<RecomMusic> findByUserAndGenre(User user, Genre genre, Pageable pageable);
}
