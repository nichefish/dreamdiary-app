package io.nicheblog.dreamdiary.feature.attachable._shared.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.managt.model.cmpstn.ManagtCmpstnModule;
import lombok.experimental.UtilityClass;

/**
 * BaseAttachableManagtHelper
 * <pre>
 *  BaseAttachableService의 managt 관련 공통 처리 유틸.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class BaseAttachableManagtHelper {

    /**
     * Dto/Entity가 각각 ManagtCmpstnModule/ManagtEmbedModule 인스턴스인지 확인.
     */
    public static boolean isManagtModule(final Object dto, final Object entity) {
        return dto instanceof ManagtCmpstnModule && entity instanceof ManagtEmbedModule;
    }

    /**
     * 등록 시 managt 처리.
     */
    public static void applyRegistManagt(final Object dto, final Object entity) {
        if (!isManagtModule(dto, entity)) return;
        ((ManagtEmbedModule) entity).setManagt(new ManagtEmbed(true));
    }

    /**
     * 수정 시 managt 처리.
     */
    public static void applyModifyManagt(final Object dto, final Object entity) {
        if (!isManagtModule(dto, entity)) return;

        final ManagtEmbedModule managtEmbedModule = (ManagtEmbedModule) entity;
        final ManagtCmpstnModule managtCmpstnModule = (ManagtCmpstnModule) dto;

        // (수정시) 조치일자 변경하지 않음 처리
        final boolean isManagtDtNull = managtEmbedModule.getManagt() == null || managtEmbedModule.getManagt().getManagtDt() == null;
        final boolean updtManagtDt = isManagtDtNull || "Y".equals(managtCmpstnModule.getManagt().getManagtDtUpdtYn());
        managtEmbedModule.setManagt(new ManagtEmbed(updtManagtDt));
    }
}
