package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findByTitle(String title);

    Page<Music> findAll(Pageable pageable);

    Page<Music> findByTitleContaining(String title, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE (:query is null or m.title LIKE %:query% or m.group.groupName LIKE %:query% or m.karaokeNum LIKE %:query%)" +
            " AND (:groupType is null or :groupType = m.group.groupType)" +
            " AND (:genre is null or :genre = m.genre)")
    Page<Music> searchAll(String query, GroupType groupType, Genre genre, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE (:query is null or m.title LIKE %:query%)" +
            " AND (:groupType is null or :groupType = m.group.groupType)" +
            " AND (:genre is null or :genre = m.genre)")
    Page<Music> searchTitle(String query, GroupType groupType, Genre genre, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE (:query is null or m.group.groupName LIKE %:query%)" +
            " AND (:groupType is null or :groupType = m.group.groupType)" +
            " AND (:genre is null or :genre = m.genre)")
    Page<Music> searchGroupName(String query, GroupType groupType, Genre genre, Pageable pageable);

    @Query("SELECT m FROM Music m WHERE (:query is null or m.karaokeNum LIKE %:query%)" +
            " AND (:groupType is null or :groupType = m.group.groupType)" +
            " AND (:genre is null or :genre = m.genre)")
    Page<Music> searchKaraokeNum(String query, GroupType groupType, Genre genre, Pageable pageable);

    @Query("SELECT m FROM Music m ORDER BY m.view DESC, m.likes DESC, m.releaseDate DESC")
    Page<Music> findAllOrderByRank(Pageable pageable);

    @Query("SELECT m FROM Music m WHERE m.releaseDate BETWEEN :oneYearAgo AND :today ORDER BY m.releaseDate DESC")
    Page<Music> findReleasedWithinOneYear(LocalDateTime oneYearAgo, LocalDateTime today, Pageable pageable);

    @Query("SELECT m FROM Music m INNER JOIN Theme t ON m.musicId = t.music.musicId WHERE t.themeName = :themeName")
    Page<Music> findAllByTheme(String themeName, Pageable pageable);

    @Query("SELECT m FROM Music m INNER JOIN Theme t ON m.musicId = t.music.musicId WHERE t.themeName = :themeName AND m.title LIKE %:title%")
    Page<Music> findAllByThemeTitleContaining(String title, String themeName, Pageable pageable);
}
