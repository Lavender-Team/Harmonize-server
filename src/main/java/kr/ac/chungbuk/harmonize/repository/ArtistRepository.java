package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    Optional<Artist> findByArtistName(String artistName);

    Page<Artist> findAll(Pageable pageable);

    @Query("SELECT a FROM Artist a WHERE a.nation = :nation")
    Page<Artist> findAllByNation(Pageable pageable, String nation);

    @Query("SELECT a FROM Artist a WHERE a.agency = :agency")
    Page<Artist> findAllByAgency(Pageable pageable, String agency);
}
