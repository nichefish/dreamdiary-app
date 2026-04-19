package io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.attachable.state.model.StateDto;
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
     * @param stateKey 상태 키
     */
    public void put(final StateKey stateKey) {
        if (stateKey == null) return;
        if (this.list == null) this.list = new ArrayList<>();
        final boolean exists = this.list.stream()
            .anyMatch(s -> stateKey.key.equals(s.getStateKey()));

        if (exists) return;

        this.list.add(new StateDto(stateKey));
    }

    /**
     * 상태 제거
     * @param stateKey 상태 키
     */
    public void remove(final StateKey stateKey) {
        if (stateKey == null) return;
        if (this.list == null) return;

        this.list.removeIf(s -> stateKey.key.equals(s.getStateKey()));

        if (this.list.isEmpty()) this.list = null; // 선택 사항: 직렬화/메모리 정리 목적
    }

    /**
     * toggle
     */
    public void apply(final StateKey stateKey, final Boolean isEnabled) {
        if (stateKey == null) return;
        if (Boolean.TRUE.equals(isEnabled)) {
            put(stateKey);
        } else {
            remove(stateKey);
        }
    }
}
