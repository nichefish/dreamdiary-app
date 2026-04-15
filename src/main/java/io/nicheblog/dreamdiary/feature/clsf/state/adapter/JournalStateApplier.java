package io.nicheblog.dreamdiary.feature.clsf.state.adapter;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import lombok.experimental.UtilityClass;

/**
 * JournalStateApplier
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalStateApplier {

    /**
     * 저널 상태 적용
     * @param state JournalState
     * @param stateCd StateCd
     * @param isEnabled Boolean
     */
    public static void apply(final JournalState state, final StateCd stateCd, final Boolean isEnabled) {
         switch (stateCd) {
            case IMPRTC -> state.setImprtc(isEnabled);
            case COLLAPSED-> state.setCollapsed(isEnabled);
            case REFRNC -> state.setRefrnc(isEnabled);
            case RESOLVED -> state.setResolved(isEnabled);
        }
    }
}

