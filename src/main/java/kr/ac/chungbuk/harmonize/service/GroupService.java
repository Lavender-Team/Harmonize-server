package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.repository.GroupRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, ArtistRepository artistRepository) {
        this.groupRepository = groupRepository;
        this.artistRepository = artistRepository;
    }

    @Transactional
    public void create(GroupRequestDto groupParam) throws IOException {
        Group group = new Group();
        group.setGroupName(groupParam.getGroupName());
        group.setGroupSize(groupParam.getArtistIds().size());
        group.setGroupType(GroupType.fromString(groupParam.getGroupType()));
        group.setAgency(groupParam.getAgency());
        group = groupRepository.save(group);

        // 그룹 프로필 이미지
        if (groupParam.getProfileImage() != null) {
            try {
                String imagePath = FileHandler.saveGroupProfileImageFile(
                        groupParam.getProfileImage(),
                        group.getGroupId()
                );
                group.setProfileImage(imagePath);
            } catch (IOException e) {
                groupRepository.delete(group);
                throw e;
            }
        }

        // 그룹 멤버
        int groupSize = 0;
        if (groupParam.getArtistIds() != null) {
            for (Long artistId : groupParam.getArtistIds()) {
                if (artistRepository.existsById(artistId)) {
                    groupRepository.addMember(group.getGroupId(), artistId);
                    groupSize++;
                }
            }
        }
        group.setGroupSize(groupSize);
    }

    @Transactional
    public void update(Long groupId, GroupRequestDto groupParam) throws IOException {
        Group group = groupRepository.findById(groupId).orElseThrow();

        group.setGroupName(groupParam.getGroupName());
        if (group.getGroupType() != null)
            group.setGroupType(GroupType.fromString(groupParam.getGroupType()));
        if (group.getAgency() != null)
            group.setAgency(groupParam.getAgency());

        // 그룹 프로필 이미지
        if (groupParam.getProfileImage() != null) {
            try {
                if (group.getProfileImage() != null)
                    FileHandler.deleteGroupProfileImageFile(group.getProfileImage(), group.getGroupId());
                String imagePath = FileHandler.saveGroupProfileImageFile(
                        groupParam.getProfileImage(),
                        group.getGroupId()
                );
                group.setProfileImage(imagePath);
            } catch (IOException e) {
                throw e;
            }
        }

        // 그룹 멤버
        int groupSize = 0;
        if (groupParam.getArtistIds() != null) {
            groupRepository.clearMember(group.getGroupId());
            for (Long artistId : groupParam.getArtistIds()) {
                if (artistRepository.existsById(artistId)) {
                    groupRepository.addMember(group.getGroupId(), artistId);
                    groupSize++;
                }
            }
        }
        group.setGroupSize(groupSize);
    }

    // 그룹 삭제
    @Transactional
    public void delete(Long groupId) throws Exception {
        Group group = groupRepository.findById(groupId).orElseThrow();

        if (group.getProfileImage() != null && !group.getProfileImage().isEmpty())
            FileHandler.deleteGroupProfileImageFile(group.getProfileImage(), groupId);
        groupRepository.clearMember(groupId);
        groupRepository.delete(group);
    }

    // 그룹 목록 조회
    public Page<Group> list(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    // 그룹 목록 검색
    public Page<Group> search(String groupName, Pageable pageable) {
        return groupRepository.findByGroupNameContaining(groupName, pageable);
    }

    // 그룹 상세정보 조회
    public Group findById(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow();
    }
}
