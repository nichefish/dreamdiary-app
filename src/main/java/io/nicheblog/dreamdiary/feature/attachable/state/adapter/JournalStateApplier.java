package io.nicheblog.dreamdiary.feature.attachable.state.adapter;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;

/**
 * JournalStateApplier
 *
 * @author nichefish
 */
public final class JournalStateApplier {

    private JournalStateApplier() {
    }

    /**
     * 캐시 맵에 상태 반영
     * @param state JournalState
     * @param stateKey StateKey
     * @param isEnabled 활성화 여부
     */
    public static void apply(final JournalState state, final StateKey stateKey, final Boolean isEnabled) {
        if (state == null || stateKey == null) return;
        switch (stateKey) {
            case RESOLVED -> state.setResolved(isEnabled);
            case COLLAPSED -> state.setCollapsed(isEnabled);
            case IMPRTC -> state.setImprtc(isEnabled);
            case REFRNC -> state.setRefrnc(isEnabled);
            case NHTMR -> state.setNhtmr(isEnabled);
            case HALLUC -> state.setHalluc(isEnabled);
        }
    }
}
