package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.Theme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = "SELECT DISTINCT t.themeName FROM Theme t")
    Page<Theme> findUniqueThemeNames(Pageable pageable);

    List<Theme> findByThemeName(String themeName);

    void deleteAllByMusic(Music music);

    void deleteByMusicAndThemeName(Music music, String themeName);
}
