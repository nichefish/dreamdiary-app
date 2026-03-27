package io.nicheblog.dreamdiary.feature.clsf.state.model;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * StateDto
 * <pre>
 *  상태 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StateDto
        extends BaseCrudDto
        implements Identifiable<Integer> {

    /** 상태 번호 (PK) */
    @Positive
    private Integer stateNo;

    /** 상태 코드 */
    @Size(max = 50)
    private String stateCd;

    /** 참조 글 번호 */
    @Positive
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    @Override
    public Integer getKey() {
        return this.stateNo;
    }

    public StateDto(final StateCd stateCd) {
        this.stateCd = stateCd.key;
    }
}
