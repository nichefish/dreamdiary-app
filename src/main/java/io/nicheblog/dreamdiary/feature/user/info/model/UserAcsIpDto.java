package io.nicheblog.dreamdiary.feature.user.info.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UserAcsIpDto
 * <pre>
 *  사용자 접속 IP Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class UserAcsIpDto {

    /** 사용자 접속 IP 고유 ID */
    private Integer id;

    /** 사용자 번호 (FK) */
    private Integer userId;

    /** 접속 IP */
    private String acsIp;
}
