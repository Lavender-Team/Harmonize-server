package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.UserAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnalysisRepository extends JpaRepository<UserAnalysis, Long> {

}
