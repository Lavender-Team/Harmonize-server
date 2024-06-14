package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findByTitle(String title);

    Page<Music> findAll(Pageable pageable);
}
