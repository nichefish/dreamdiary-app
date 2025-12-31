package io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn;

import io.nicheblog.dreamdiary.extension.clsf.state.StateCd;
import io.nicheblog.dreamdiary.extension.clsf.state.model.StateDto;
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
}