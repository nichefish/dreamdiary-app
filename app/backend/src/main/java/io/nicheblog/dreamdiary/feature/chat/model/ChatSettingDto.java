package io.nicheblog.dreamdiary.feature.chat.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * ChatSettingDto
 * <pre>
 *  사용자별 또는 관리자 기본 채팅 설정 응답에 사용하는 DTO.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChatSettingDto
        extends BaseAuditDto
        implements Identifiable<Integer> {

    /** 설정 ID */
    private Integer id;

    /** 설정 범위 */
    @Builder.Default
    private String scope = "USER";

    /** 설정 범위 키 */
    private String scopeKey;

    /** 최근 대화 기억 메시지 수 */
    @Builder.Default
    private Integer recentMessageLimit = 50;

    /** RAG 검색 사용 여부 */
    @Builder.Default
    private Boolean ragEnabled = true;

    /** LOOKUP RAG 최대 검색 건수 */
    @Builder.Default
    private Integer ragTopK = 5;

    /** LOOKUP/SUMMARY 벡터 최소 점수 */
    @Builder.Default
    private Double ragMinScore = 0.35D;

    /** SUMMARY RAG 최대 검색 건수 */
    @Builder.Default
    private Integer ragSummaryTopK = 12;

    /** SYNTHESIS(비태도) RAG 최대 검색 건수 */
    @Builder.Default
    private Integer ragSynthesisTopK = 25;

    /** 태도(person-stance) RAG 최대 검색 건수 */
    @Builder.Default
    private Integer ragStanceTopK = 50;

    /** SYNTHESIS 벡터 최소 점수 */
    @Builder.Default
    private Double ragSynthesisMinScore = 0.25D;


    /* ----- */

    /**
     * 공통 식별자 인터페이스에서 사용할 설정 ID를 반환한다.
     *
     * @return 채팅 설정 ID
     */
    @Override
    public Integer getKey() {
        return this.id;
    }
}
