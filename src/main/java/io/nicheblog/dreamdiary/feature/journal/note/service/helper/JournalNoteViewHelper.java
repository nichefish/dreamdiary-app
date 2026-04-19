package io.nicheblog.dreamdiary.feature.journal.note.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.note.model.JournalNoteDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

@UtilityClass
public class JournalNoteViewHelper {

    public static void applyStates(final List<JournalNoteDto> listDto, final Map<Integer, JournalState> noteMap) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalNoteDto note : listDto) {
            final JournalState state = noteMap.get(note.getId());
            if (state == null) continue;

            note.state.apply(StateKey.COLLAPSED, state.getCollapsed());
            note.state.apply(StateKey.RESOLVED, state.getResolved());
            note.state.apply(StateKey.IMPRTC, state.getImprtc());
            note.state.apply(StateKey.REFRNC, state.getRefrnc());
        }
    }
}
