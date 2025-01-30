package io.nicheblog.dreamdiary.domain.flsys.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * FlsysRefDto
 * <pre>
 *  파일시스템 참조 Dto
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FlsysRefDto {

    /** 사용자 고유 ID (PK) */
    @Positive
    private Integer flsysRefNo;

    /** 글 번호 */
    @Positive
    private String postNo;

    /** 컨텐츠 타입 */
    @Size(max = 50)
    private String contentType;

    /** 파일 경로 */
    private String filePath;
}
