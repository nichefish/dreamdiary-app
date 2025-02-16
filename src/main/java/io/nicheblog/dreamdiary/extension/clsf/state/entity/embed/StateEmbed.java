package io.nicheblog.dreamdiary.extension.clsf.state.entity.embed;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * StateEmbed
 * <pre>
 *  상태 관리(사용여부, 정렬순서) 관련 정보 위임 (entity level)
 * </pre>
 *
 * @author nichefish
 * @see StateEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateEmbed
        implements Serializable {

    /** 정렬 순서 */
    @Column(name = "sort_ordr", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrdr;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";
}