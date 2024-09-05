package kr.ac.chungbuk.harmonize.dto.response;

import kr.ac.chungbuk.harmonize.entity.Group;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDto {

    Long id;
    private String groupName;
    private String groupType;
    private String groupTypeName;
    private Integer groupSize;
    private String agency;
    private String profileImage;
    private List<ArtistDto> members;

    public static GroupDto build(Group group) {
        return GroupDto.builder()
                .id(group.getGroupId())
                .groupName(group.getGroupName())
                .groupType(group.getGroupType().name())
                .groupTypeName(GroupType.toString(group.getGroupType()))
                .groupSize(group.getGroupSize())
                .agency(group.getAgency())
                .profileImage(group.getProfileImage())
                .members(group.getMembers().stream().map(m -> ArtistDto.build(m.getArtist())).toList())
                .build();
    }

}
