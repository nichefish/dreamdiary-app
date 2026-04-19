package io.nicheblog.dreamdiary.infrastructure.code.model;

import lombok.*;

/**
 * CodeLookupItem.
 * <pre>
 *  화면/캐시용 코드 상세 한 줄.
 * </pre>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeLookupItem {

    private Integer id;
    private String groupCode;
    private String code;
    private String codeName;
    private String description;
    private Integer sortOrder;
    private String useYn;
    private String protectedYn;
}
