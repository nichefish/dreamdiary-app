package io.nicheblog.dreamdiary.feature.user.account.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserInfoItemDto
 * <pre>
 *  사용자 추가정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserInfoItemDto {

    /** 사용자 정보 추가정보 고유 ID */
    private Integer userInfoItemNo;

    /** 항목 이름 */
    private String itemNm;

    /** 항목 내용 */
    private String itemCn;

    /** 항목 설명 */
    private String itemDc;

    /** 정렬 순서 */
    private Integer sortOrder;
}
