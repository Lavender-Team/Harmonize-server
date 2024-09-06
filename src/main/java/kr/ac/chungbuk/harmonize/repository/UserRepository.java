package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Page<User> findAll(Pageable pageable);

    Page<User> findByNicknameContaining(String nickname, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findAllByRole(String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isDeleted = :isDeleted")
    Page<User> findAllByIsDeleted(boolean isDeleted, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isLocked = :isLocked")
    Page<User> findAllByIsLocked(boolean isLocked, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.role = :role")
    Page<User> findAllByNicknameAndRole(String nickname, String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.isDeleted = :isDeleted")
    Page<User> findAllByNicknameAndIsDeleted(String nickname, boolean isDeleted, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% AND u.isLocked = :isLocked")
    Page<User> findAllByNicknameAndIsLocked(String nickname, boolean isLocked, Pageable pageable);

    Boolean existsByLoginId(String loginId);

    Boolean existsByEmail(String email);
}