package io.nicheblog.dreamdiary.feature.attachable._shared.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.meta.service.MetaProcService;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.service.TagProcService;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalPeriodModule;
import io.nicheblog.dreamdiary.global.handler.SpringBeanProvider;
import lombok.experimental.UtilityClass;

/**
 * BaseAttachableProcPostProcessor
 * <pre>
 *  BaseAttachableService의 tag/meta 후처리 유틸.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class BaseAttachableProcPostProcessor {

    private static final class JournalPeriod {
        private final Integer yy;
        private final Integer mnth;

        private JournalPeriod(final Integer yy, final Integer mnth) {
            this.yy = yy;
            this.mnth = mnth;
        }

        private Integer getYy() {
            return yy;
        }

        private Integer getMnth() {
            return mnth;
        }
    }

    /**
     * 등록/수정 후처리 공통.
     */
    public static void afterWrite(final Object postDto, final BaseAttachableDto updatedDto) throws Exception {
        BaseAttachableCmpstnHelper.copyCmpstnFromPostDto(postDto, updatedDto);
        processTags(updatedDto, false);
        processMetas(updatedDto, false);
    }

    /**
     * 삭제 후처리 공통.
     */
    public static void afterDelete(final BaseAttachableDto deletedDto) throws Exception {
        processTags(deletedDto, true);
        processMetas(deletedDto, true);
    }

    /**
     * TagCmpstnModule 구현 DTO의 공통 태그 처리.
     */
    public static void processTags(final BaseAttachableDto dto, final boolean isDeleteMethod) throws Exception {
        if (!(dto instanceof TagCmpstnModule tagCmpstnModule)) return;
        // Reflection 은 태그를 두지 않는다(표시 DTO 가 TagCmpstnModule 이어도 처리하지 않는다).
        if (ContentType.JOURNAL_REFLECTION.key.equals(dto.getContentType())) return;

        final BaseAttachableKey attachableKey = dto.getAttachableKey();
        if (attachableKey == null) return;

        final TagCmpstn tagCmpstn = isDeleteMethod ? null : tagCmpstnModule.getTag();
        final TagProcService tagProcService = SpringBeanProvider.getBean(TagProcService.class);
        final JournalPeriod period = resolveJournalPeriod(dto, "tag");
        tagProcService.process(attachableKey, tagCmpstn, period.getYy(), period.getMnth());
    }

    /**
     * MetaCmpstnModule 구현 DTO의 공통 메타 처리.
     */
    public static void processMetas(final BaseAttachableDto dto, final boolean isDeleteMethod) throws Exception {
        if (!(dto instanceof MetaCmpstnModule metaCmpstnModule)) return;
        // Reflection 은 메타를 두지 않는다.
        if (ContentType.JOURNAL_REFLECTION.key.equals(dto.getContentType())) return;

        final BaseAttachableKey attachableKey = dto.getAttachableKey();
        if (attachableKey == null) return;

        final MetaCmpstn metaCmpstn = isDeleteMethod ? null : metaCmpstnModule.getMeta();
        final MetaProcService metaProcService = SpringBeanProvider.getBean(MetaProcService.class);
        final JournalPeriod period = resolveJournalPeriod(dto, "meta");
        metaProcService.process(attachableKey, metaCmpstn, period.getYy(), period.getMnth());
    }

    /**
     * 저널 DTO인 경우 yy/mnth를 검증해 반환, 아니면 (null, null) 반환.
     */
    private static JournalPeriod resolveJournalPeriod(final BaseAttachableDto dto, final String context) {
        if (!(dto instanceof JournalPeriodModule periodModule)) return new JournalPeriod(null, null);

        final Integer yy = periodModule.getYy();
        final Integer mnth = periodModule.getMnth();
        if (yy == null || mnth == null) {
            throw new IllegalStateException("yy/mnth must not be null for journal " + context + " process: " + dto.getClass().getName());
        }
        return new JournalPeriod(yy, mnth);
    }
}

