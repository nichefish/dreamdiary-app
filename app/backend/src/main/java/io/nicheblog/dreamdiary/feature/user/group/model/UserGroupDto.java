package io.nicheblog.dreamdiary.feature.user.group.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * UserGroupDto
 * <pre>
 *  사용자 그룹 관리 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupDto {

    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String groupKey;

    @NotBlank
    @Size(max = 100)
    private String groupName;

    @Size(max = 500)
    private String description;

    private Integer sortOrder;

    @Builder.Default
    private String useYn = "Y";

    /** 멤버 수 (목록용) */
    private Long memberCount;

    /** 부여된 권한 키 목록 (상세) */
    @Builder.Default
    private List<String> permissionKeys = new ArrayList<>();

    /** 멤버 username 목록 (상세) */
    @Builder.Default
    private List<String> memberUsernames = new ArrayList<>();
}
