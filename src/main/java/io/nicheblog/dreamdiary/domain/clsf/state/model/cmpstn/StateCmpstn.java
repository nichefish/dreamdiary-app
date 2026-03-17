package io.nicheblog.dreamdiary.domain.clsf.state.model.cmpstn;

import io.nicheblog.dreamdiary.domain.clsf.state.StateCd;
import io.nicheblog.dreamdiary.domain.clsf.state.model.StateDto;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * StateCmpstn
 * <pre>
 *  위임 :: 상태 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see StateCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateCmpstn
        implements Serializable {

    /** 컨텐츠 타입 :: 상위에서 주입받음. */
    private String contentType;

    /** 상태 목록 */
    private List<StateDto> list;

    /**
     * 상태 추가
     * @param stateCd 상태 코드
     */
    public void put(final StateCd stateCd) {
        if (stateCd == null) return;
        if (this.list == null) this.list = new ArrayList<>();
        final boolean exists = this.list.stream()
            .anyMatch(s -> stateCd.key.equals(s.getStateCd()));

        if (exists) return;

        this.list.add(new StateDto(stateCd));
    }

    /**
     * 상태 제거
     * @param stateCd 상태 코드
     */
    public void remove(final StateCd stateCd) {
        if (stateCd == null) return;
        if (this.list == null) return;

        this.list.removeIf(s -> stateCd.key.equals(s.getStateCd()));

        if (this.list.isEmpty()) this.list = null; // 선택 사항: 직렬화/메모리 정리 목적
    }

    /**
     * toggle
     */
    public void apply(final StateCd stateCd, final Boolean isEnabled) {
        if (stateCd == null) return;
        if (Boolean.TRUE.equals(isEnabled)) {
            put(stateCd);
        } else {
            remove(stateCd);
        }
    }
}
