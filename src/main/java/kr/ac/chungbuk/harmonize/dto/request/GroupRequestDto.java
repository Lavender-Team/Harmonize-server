package kr.ac.chungbuk.harmonize.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ac.chungbuk.harmonize.entity.Artist;
import kr.ac.chungbuk.harmonize.enums.GroupType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GroupRequestDto {

    @NotBlank
    @Size(min = 1, max = 50)
    private String groupName;

    @Pattern(regexp = "^(SOLO|GROUP)$")
    private String groupType;

    @Size(max = 100)
    private String agency;

    private MultipartFile profileImage;

    private List<Long> artistIds;

    // 가수 프로필 이미지 경로 : 솔로 그룹 자동 생성시에만 사용
    private String copyProfileImagePath;

    // 솔로 그룹 자동 생성시 Artist 객체를 변환
    public static GroupRequestDto convertFrom(Artist artist) {
        GroupRequestDto groupParam = new GroupRequestDto();
        groupParam.setGroupName(artist.getArtistName());
        groupParam.setGroupType(GroupType.SOLO.name());
        groupParam.setAgency(artist.getAgency());
        groupParam.setArtistIds(List.of(artist.getArtistId()));
        groupParam.setCopyProfileImagePath(artist.getProfileImage());
        return groupParam;
    }
}
