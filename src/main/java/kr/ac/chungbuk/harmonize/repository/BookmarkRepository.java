package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Bookmark;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Bookmark findByUserAndMusic(User user, Music music);

    Long countByMusic(Music music);

    Boolean existsByUserAndMusic(User user, Music music);
}
