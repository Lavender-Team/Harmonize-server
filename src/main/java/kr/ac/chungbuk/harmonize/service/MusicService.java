package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Music;
import kr.ac.chungbuk.harmonize.enums.Genre;
import kr.ac.chungbuk.harmonize.repository.MusicRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MusicService {

    private final MusicRepository musicRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    // 음악 생성
    public void create(String title, String genre, MultipartFile albumCover, String karaokeNum,
                       LocalDateTime releaseDate, String playLink) throws Exception {
        Music music = new Music();
        music.setTitle(title);
        music.setGenre(Genre.fromString(genre));
        music.setKaraokeNum(karaokeNum);
        music.setReleaseDate(releaseDate);
        music.setPlayLink(playLink);
        music.setView(0L);
        music.setLikes(0L);

        music = musicRepository.save(music);

        try {
            String albumCoverPath = FileHandler.saveAlbumCoverFile(albumCover, music.getMusicId());
            music.setAlbumCover(albumCoverPath);
        } catch (IOException e) {
            musicRepository.delete(music);
            throw e;
        }
        musicRepository.save(music);
    }

    // 음악 삭제
    public void delete(Long musicId) throws Exception {
        Music music = musicRepository.findById(musicId).orElseThrow();

        FileHandler.deleteAlbumCoverFile(music.getAlbumCover(), music.getMusicId());
        musicRepository.delete(music);
    }
    
    
    // 음악 목록 조회
    public Page<Music> list(Pageable pageable) {
        return musicRepository.findAll(pageable);
    }
}
