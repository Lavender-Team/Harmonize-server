package kr.ac.chungbuk.harmonize.repository;

import kr.ac.chungbuk.harmonize.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findAll(Pageable pageable);

    Page<Group> findByGroupNameContaining(String groupName, Pageable pageable);

    @Query(value = "INSERT INTO group_member (group_id, artist_id) VALUES (:groupId, :artistId)", nativeQuery = true)
    void addMember(Long groupId, Long artistId);

    @Query(value = "DELETE FROM group_member WHERE group_id = :groupId", nativeQuery = true)
    void clearMember(Long groupId);
}
