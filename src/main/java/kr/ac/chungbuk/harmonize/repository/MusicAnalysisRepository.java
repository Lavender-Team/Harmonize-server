package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.MusicAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicAnalysisRepository extends JpaRepository<MusicAnalysis, Long> {

}
