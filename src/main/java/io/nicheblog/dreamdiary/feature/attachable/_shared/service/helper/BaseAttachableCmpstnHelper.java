package io.nicheblog.dreamdiary.feature.attachable._shared.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import lombok.experimental.UtilityClass;

/**
 * BaseAttachableCmpstnHelper
 * <pre>
 *  BaseAttachableService의 cmpstn(tag/meta) 전달 관련 공통 처리 유틸.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class BaseAttachableCmpstnHelper {

    /**
     * 화면에서 넘어온 tag/meta 문자열을 후처리 대상 DTO에 전달.
     */
    public static void copyCmpstnFromPostDto(final Object postDto, final Object updatedDto) {
        if (postDto instanceof TagCmpstnModule && updatedDto instanceof TagCmpstnModule) {
            ((TagCmpstnModule) updatedDto).setTagFrom((TagCmpstnModule) postDto);
        }
        if (postDto instanceof MetaCmpstnModule && updatedDto instanceof MetaCmpstnModule) {
            ((MetaCmpstnModule) updatedDto).setMetaFrom((MetaCmpstnModule) postDto);
        }
    }
}
