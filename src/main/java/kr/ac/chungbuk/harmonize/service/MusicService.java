package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MusicService {

    private MusicRepository musicRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public void create(String title, String genre, String albumCover, String karaokeNum,
                       LocalDateTime releaseDate, String playLink) throws Exception {
        Music music = new Music();
        music.setTitle(title);
        music.setGenre(Genre.fromString(genre));
        music.setAlbumCover(albumCover);
        music.setKaraokeNum(karaokeNum);
        music.setReleaseDate(releaseDate);
        music.setPlayLink(playLink);
        music.setView(0L);
        music.setLikes(0L);

        musicRepository.save(music);
    }

}
