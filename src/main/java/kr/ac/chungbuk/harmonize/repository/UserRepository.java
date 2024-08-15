package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Page<User> findAll(Pageable pageable);

    Page<User> findByNicknameContaining(String nickname, Pageable pageable);
}
