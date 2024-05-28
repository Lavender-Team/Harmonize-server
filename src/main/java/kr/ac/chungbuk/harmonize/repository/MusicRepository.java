package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {


}
