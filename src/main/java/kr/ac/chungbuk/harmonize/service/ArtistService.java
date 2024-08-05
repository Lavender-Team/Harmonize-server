package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.enums.Gender;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Autowired
    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    // 가수 생성
    public Artist create(String artistName, String gender, MultipartFile profileImage, String activityPeriod,
            String nation, String agency) throws IOException {
        // 가수 객체
        Artist artist = new Artist();
        artist.setArtistName(artistName);
        artist.setGender(Gender.valueOf(gender.toUpperCase()));
        artist.setActivityPeriod(activityPeriod);
        artist.setNation(nation);
        artist.setAgency(agency);

        artist = artistRepository.save(artist);

        // 프로필 이미지 파일 저장
        if (profileImage != null) {
            try {
                String profileImagePath = FileHandler.saveProfileImageFile(profileImage, artist.getArtistId());
                artist.setProfileImage(profileImagePath);
            } catch (IOException e) {
                artistRepository.delete(artist);
                throw e;
            }
        }

        return artistRepository.save(artist);
    }

    // 가수 수정
    @Transactional
    public void update(Long artistId, String artistName, String gender, MultipartFile profileImage,
            String activityPeriod, String nation, String agency) throws IOException {
        // 가수 객체
        Artist artist = artistRepository.findById(artistId).orElseThrow();
        if (artistName != null)
            artist.setArtistName(artistName);
        if (gender != null)
            artist.setGender(Gender.valueOf(gender.toUpperCase()));
        if (activityPeriod != null)
            artist.setActivityPeriod(activityPeriod);
        if (nation != null)
            artist.setNation(nation);
        if (agency != null)
            artist.setAgency(agency);

        // 프로필 이미지 파일 새로 업로드시 수정
        if (profileImage != null) {
            try {
                if (artist.getProfileImage() != null)
                    FileHandler.deleteProfileImageFile(artist.getProfileImage(), artist.getArtistId()); // 기존 파일 삭제
                String profileImagePath = FileHandler.saveProfileImageFile(profileImage, artist.getArtistId()); // 새 파일
                                                                                                                // 저장
                artist.setProfileImage(profileImagePath);
            } catch (IOException e) {
                artistRepository.delete(artist);
                throw e;
            }
        }
        artistRepository.save(artist);
    }

    // 가수 삭제
    @Transactional
    public void delete(Long artistId) throws IOException {
        Artist artist = artistRepository.findById(artistId).orElseThrow();

        if (artist.getProfileImage() != null && !artist.getProfileImage().isEmpty())
            FileHandler.deleteProfileImageFile(artist.getProfileImage(), artist.getArtistId());

        artistRepository.delete(artist);
    }

    // 가수 상세정보 조회
    public Artist readByAdmin(Long artistId) {
        return artistRepository.findById(artistId).orElseThrow();
    }

    // 가수 목록 조회
    public Page<Artist> list(Pageable pageable) {
        return artistRepository.findAll(pageable);
    }

    // 가수 이름 검색
    public Page<Artist> search(String artistName, Pageable pageable) {
        return artistRepository.findByArtistNameContaining(artistName, pageable);
    }

    public Optional<Artist> findById(Long artistId) {
        return artistRepository.findById(artistId);
    }
}
