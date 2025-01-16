package kr.ac.chungbuk.harmonize.service;

import jakarta.transaction.Transactional;
import kr.ac.chungbuk.harmonize.dto.request.GroupRequestDto;
import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import kr.ac.chungbuk.harmonize.repository.ArtistRepository;
import kr.ac.chungbuk.harmonize.repository.GroupRepository;
import kr.ac.chungbuk.harmonize.utility.FileHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final ArtistRepository artistRepository;
    private final FileHandler fileHandler;


    @Transactional
    public Group create(GroupRequestDto groupParam) throws IOException {

        log.info("size={}", groupParam.getArtistIds().size());

        Group group = Group.builder()
                .groupName(groupParam.getGroupName())
                .groupSize(groupParam.getArtistIds().size())
                .groupType(GroupType.fromString(groupParam.getGroupType()))
                .agency(groupParam.getAgency())
                .build();
        group = groupRepository.save(group);

        // 그룹 프로필 이미지
        if (groupParam.getProfileImage() != null) {
            try {
                String imagePath = fileHandler.saveGroupProfileImageFile(
                        groupParam.getProfileImage(),
                        group.getGroupId()
                );
                group.setProfileImage(imagePath);
            } catch (IOException e) {
                groupRepository.delete(group);
                throw e;
            }
        }
        else if (groupParam.getCopyProfileImagePath() != null) {
            // 기존 가수 프로필 이미지 복사 (솔로 그룹 자동 생성시)
            try {
                String imagePath = fileHandler.copyArtistProfileImageFile(
                        groupParam.getCopyProfileImagePath(),
                        group.getGroupId(),
                        groupParam.getArtistIds().get(0)
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

        return groupRepository.save(group);
    }

    @Transactional
    public Group update(Long groupId, GroupRequestDto groupParam) throws IOException {
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
                    fileHandler.deleteGroupProfileImageFile(group.getProfileImage(), group.getGroupId());
                String imagePath = fileHandler.saveGroupProfileImageFile(
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

        return groupRepository.save(group);
    }

    // 그룹 삭제
    @Transactional
    public void delete(Long groupId) throws Exception {
        Group group = groupRepository.findById(groupId).orElseThrow();

        if (group.getProfileImage() != null && !group.getProfileImage().isEmpty())
            fileHandler.deleteGroupProfileImageFile(group.getProfileImage(), groupId);
        groupRepository.clearMember(groupId);
        groupRepository.delete(group);
    }

    // 그룹 상세정보 조회
    public Group read(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow();
    }

    // 그룹 목록 조회
    public Page<Group> list(Pageable pageable) {
        return groupRepository.findAll(pageable);
    }

    // 그룹 목록 검색
    public Page<Group> search(String groupName, Pageable pageable) {
        return groupRepository.findByGroupNameContaining(groupName, pageable);
    }
}
