package kr.ac.chungbuk.harmonize.service;

import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Autowired
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }


    public void create() {
        // TODO 가수 등록 기능
    }

    public void delete() {
        // TODO 가수 삭제 기능
    }

    public Page<Artist> list() {
        // TODO 가수 목록 조회 기능

        return null;
    }
}
