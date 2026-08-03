package io.nicheblog.dreamdiary.auth.permission.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PermissionDto
 * <pre>
 *  권한 카탈로그 항목 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {

    private Integer id;
    private String permKey;
    private String permName;
    private String description;
    private Integer sortOrder;
    private String useYn;
}
