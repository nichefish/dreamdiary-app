package io.nicheblog.dreamdiary.feature.clsf._shared.service.helper;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.meta.service.MetaProcService;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProcService;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlPeriodModule;
import io.nicheblog.dreamdiary.global.handler.SpringBeanProvider;
import lombok.experimental.UtilityClass;

/**
 * BaseClsfProcPostProcessor
 * <pre>
 *  BaseClsfService의 tag/meta 후처리 유틸.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class BaseClsfProcPostProcessor {

    private static final class JrnlPeriod {
        private final Integer yy;
        private final Integer mnth;

        private JrnlPeriod(final Integer yy, final Integer mnth) {
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
    public static void afterWrite(final Object postDto, final BaseClsfDto updatedDto) throws Exception {
        BaseClsfCmpstnHelper.copyCmpstnFromPostDto(postDto, updatedDto);
        processTags(updatedDto, false);
        processMetas(updatedDto, false);
    }

    /**
     * 삭제 후처리 공통.
     */
    public static void afterDelete(final BaseClsfDto deletedDto) throws Exception {
        processTags(deletedDto, true);
        processMetas(deletedDto, true);
    }

    /**
     * TagCmpstnModule 구현 DTO의 공통 태그 처리.
     */
    public static void processTags(final BaseClsfDto dto, final boolean isDeleteMethod) throws Exception {
        if (!(dto instanceof TagCmpstnModule tagCmpstnModule)) return;

        final BaseClsfKey clsfKey = dto.getClsfKey();
        if (clsfKey == null) return;

        final TagCmpstn tagCmpstn = isDeleteMethod ? null : tagCmpstnModule.getTag();
        final TagProcService tagProcService = SpringBeanProvider.getBean(TagProcService.class);
        final JrnlPeriod period = resolveJrnlPeriod(dto, "tag");
        tagProcService.process(clsfKey, tagCmpstn, period.getYy(), period.getMnth());
    }

    /**
     * MetaCmpstnModule 구현 DTO의 공통 메타 처리.
     */
    public static void processMetas(final BaseClsfDto dto, final boolean isDeleteMethod) throws Exception {
        if (!(dto instanceof MetaCmpstnModule metaCmpstnModule)) return;

        final BaseClsfKey clsfKey = dto.getClsfKey();
        if (clsfKey == null) return;

        final MetaCmpstn metaCmpstn = isDeleteMethod ? null : metaCmpstnModule.getMeta();
        final MetaProcService metaProcService = SpringBeanProvider.getBean(MetaProcService.class);
        final JrnlPeriod period = resolveJrnlPeriod(dto, "meta");
        metaProcService.process(clsfKey, metaCmpstn, period.getYy(), period.getMnth());
    }

    /**
     * 저널 DTO인 경우 yy/mnth를 검증해 반환, 아니면 (null, null) 반환.
     */
    private static JrnlPeriod resolveJrnlPeriod(final BaseClsfDto dto, final String context) {
        if (!(dto instanceof JrnlPeriodModule periodModule)) return new JrnlPeriod(null, null);

        final Integer yy = periodModule.getYy();
        final Integer mnth = periodModule.getMnth();
        if (yy == null || mnth == null) {
            throw new IllegalStateException("yy/mnth must not be null for jrnl " + context + " process: " + dto.getClass().getName());
        }
        return new JrnlPeriod(yy, mnth);
    }
}
