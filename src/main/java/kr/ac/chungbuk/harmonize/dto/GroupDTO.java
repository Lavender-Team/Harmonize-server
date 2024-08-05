package kr.ac.chungbuk.harmonize.dto;

import kr.ac.chungbuk.harmonize.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDTO {

    Long id;
    private String groupName;
    private String groupType;
    private Integer groupSize;
    private String agency;
    private String profileImage;
    private List<ArtistDTO> members;

    public static GroupDTO build(Group group) {
        return GroupDTO.builder()
                .id(group.getGroupId())
                .groupName(group.getGroupName())
                .groupType(group.getGroupType().name())
                .groupSize(group.getGroupSize())
                .agency(group.getAgency())
                .profileImage(group.getProfileImage())
                .members(group.getMembers().stream().map(m -> ArtistDTO.build(m.getArtist())).toList())
                .build();
    }

}
