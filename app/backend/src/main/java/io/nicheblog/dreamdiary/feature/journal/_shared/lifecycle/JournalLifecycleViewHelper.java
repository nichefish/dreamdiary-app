package io.nicheblog.dreamdiary.feature.journal._shared.lifecycle;

import io.nicheblog.dreamdiary.feature.attachable.lifecycle.LifecycleKey;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.model.cmpstn.LifecycleCmpstn;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.thread.model.JournalThreadDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 라이프사이클 보조 맵 값을 저널 DTO에 병합하는 화면 helper.
 *
 * <p>목록 조회는 라이프사이클을 작은 캐시 맵으로 들고 있다. 이 helper는 원본 키를
 * 화면 템플릿이 쓰는 DTO 조합 객체로 변환한다.</p>
 */
@UtilityClass
public class JournalLifecycleViewHelper {

    /**
     * 저널 일기 DTO 목록에 라이프사이클 값을 적용한다.
     *
     * @param listDto 라이프사이클을 붙일 일기 DTO 목록
     * @param lifecycleMap 일기 ID 기준 라이프사이클 키 맵
     */
    public static void applyEntryLifecycle(
            final List<JournalEntryDto> listDto,
            final Map<Integer, String> lifecycleMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setLifecycle(toCmpstn(lifecycleMap.get(dto.getId())));
        }
    }

    /**
     * 저널 해석 DTO 목록에 라이프사이클 값을 적용한다.
     *
     * @param listDto 라이프사이클을 붙일 해석 DTO 목록
     * @param lifecycleMap 해석 ID 기준 라이프사이클 키 맵
     */
    public static void applyInterpretationLifecycle(
            final List<JournalInterpretationDto> listDto,
            final Map<Integer, String> lifecycleMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalInterpretationDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setLifecycle(toCmpstn(lifecycleMap.get(dto.getId())));
        }
    }

    /**
     * 저널 스레드 DTO 목록에 라이프사이클 값을 적용한다.
     *
     * @param listDto 라이프사이클을 붙일 스레드 DTO 목록
     * @param lifecycleMap 스레드 ID 기준 라이프사이클 키 맵
     */
    public static void applyThreadLifecycle(
            final List<JournalThreadDto> listDto,
            final Map<Integer, String> lifecycleMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalThreadDto dto : listDto) {
            if (dto == null || dto.getId() == null) continue;
            dto.setLifecycle(toCmpstn(lifecycleMap.get(dto.getId())));
        }
    }

    /**
     * 캐시의 라이프사이클 키를 템플릿 친화적인 조합 객체로 변환한다.
     *
     * @param lifecycleKey 캐시 또는 DB에서 읽은 원본 키
     * @return 라이프사이클 조합 객체. 값이 없으면 {@code OPEN}
     */
    private static LifecycleCmpstn toCmpstn(final String lifecycleKey) {
        final LifecycleKey key = LifecycleKey.getByKey(lifecycleKey);
        return LifecycleCmpstn.of(key == null ? LifecycleKey.OPEN : key);
    }
}
