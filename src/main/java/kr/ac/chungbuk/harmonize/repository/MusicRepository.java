package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findByTitle(String title);

    Page<Music> findAll(Pageable pageable);

    Page<Music> findByTitleContaining(String title, Pageable pageable);

    @Query("SELECT m FROM Music m INNER JOIN Theme t ON m.musicId = t.music.musicId WHERE t.themeName = :themeName")
    Page<Music> findAllByTheme(String themeName, Pageable pageable);

    @Query("SELECT m FROM Music m INNER JOIN Theme t ON m.musicId = t.music.musicId WHERE t.themeName = :themeName AND m.title LIKE %:title%")
    Page<Music> findAllByThemeTitleContaining(String title, String themeName, Pageable pageable);
}
