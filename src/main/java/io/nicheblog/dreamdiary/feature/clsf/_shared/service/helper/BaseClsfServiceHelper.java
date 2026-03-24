package io.nicheblog.dreamdiary.feature.clsf._shared.service.helper;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.feature.clsf.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.event.JrnlTagProcEvent;
import io.nicheblog.dreamdiary.feature.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlPeriodModule;
import io.nicheblog.dreamdiary.global.handler.ApplicationEventPublisherWrapper;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

/**
 * BaseClsfServiceHelper
 * <pre>
 *  BaseClsfService에서 재사용되는 정적 유틸 로직.
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
public final class BaseClsfServiceHelper {

    /**
     * Dto/Entity가 각각 ManagtCmpstnModule/ManagtEmbedModule 인스턴스인지 확인.
     */
    public static boolean isManagtModule(final Object dto, final Object entity) {
        return dto instanceof ManagtCmpstnModule && entity instanceof ManagtEmbedModule;
    }

    /**
     * Dto가 TagCmpstnModule 인스턴스인지 확인.
     */
    public static boolean isTagModule(final Object dto) {
        return dto instanceof TagCmpstnModule;
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
        final boolean isManagtDtNull = managtEmbedModule.getManagt() == null
                || managtEmbedModule.getManagt().getManagtDt() == null;
        final boolean updtManagtDt = isManagtDtNull
                || "Y".equals(managtCmpstnModule.getManagt().getManagtDtUpdtYn());
        managtEmbedModule.setManagt(new ManagtEmbed(updtManagtDt));
    }

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

    /**
     * 등록/수정 공통 후처리:
     * 화면 tag/meta 문자열 전달 + 태그 처리 이벤트 발행.
     */
    public static void afterWrite(
            final Object source,
            final ApplicationEventPublisherWrapper explicitPublisher,
            final Object postDto,
            final BaseClsfDto updatedDto
    ) throws Exception {
        copyCmpstnFromPostDto(postDto, updatedDto);
        publishTagProcEvent(source, explicitPublisher, updatedDto, false);
    }

    /**
     * 삭제 공통 후처리:
     * 태그 처리 이벤트 발행.
     */
    public static void afterDelete(
            final Object source,
            final ApplicationEventPublisherWrapper explicitPublisher,
            final BaseClsfDto deletedDto
    ) throws Exception {
        publishTagProcEvent(source, explicitPublisher, deletedDto, true);
    }

    /**
     * 등록/수정 응답 생성.
     */
    public static ServiceResponse newWriteResponse(final Identifiable<?> updatedDto) {
        return newResponse(updatedDto != null && updatedDto.getKey() != null, updatedDto);
    }

    /**
     * 삭제 응답 생성.
     */
    public static ServiceResponse newDeleteResponse(final Object deletedDto) {
        return newResponse(true, deletedDto);
    }

    /**
     * 공통 ServiceResponse 생성.
     */
    public static ServiceResponse newResponse(final boolean rslt, final Object rsltObj) {
        final ServiceResponse response = new ServiceResponse();
        response.setRslt(rslt);
        response.setRsltObj(rsltObj);
        return response;
    }

    /**
     * TagCmpstnModule 구현 DTO는 공통 CRUD 경로에서 태그 이벤트를 반드시 발행.
     */
    public static void publishTagProcEvent(
            final Object source,
            final ApplicationEventPublisherWrapper explicitPublisher,
            final BaseClsfDto dto,
            final boolean isDeleteMethod
    ) throws Exception {
        if (!(dto instanceof TagCmpstnModule tagCmpstnModule)) return;

        final BaseClsfKey clsfKey = dto.getClsfKey();
        if (clsfKey == null) return;

        final ApplicationEventPublisherWrapper publisher = resolvePublisher(source, explicitPublisher);
        if (publisher == null) {
            throw new IllegalStateException("ApplicationEventPublisherWrapper not resolved: " + source.getClass().getName());
        }

        final TagCmpstn tagCmpstn = tagCmpstnModule.getTag();

        // 저널 DTO는 yy/mnth를 함께 전달해야 캐시 갱신 이벤트 연쇄가 정상 동작.
        if (dto instanceof JrnlPeriodModule periodModule) {
            final Integer yy = periodModule.getYy();
            final Integer mnth = periodModule.getMnth();
            if (yy == null || mnth == null) {
                throw new IllegalStateException("yy/mnth must not be null for jrnl tag event: " + dto.getClass().getName());
            }

            final JrnlTagProcEvent event = isDeleteMethod
                    ? new JrnlTagProcEvent(source, clsfKey, yy, mnth)
                    : new JrnlTagProcEvent(source, clsfKey, yy, mnth, tagCmpstn);
            publisher.publishCustomEvent(event);
            return;
        }

        final TagProcEvent event = isDeleteMethod
                ? new TagProcEvent(source, clsfKey)
                : new TagProcEvent(source, clsfKey, tagCmpstn);
        publisher.publishCustomEvent(event);
    }

    /**
     * 구현체 getPublisher() 우선, 없으면 `publisher` 필드 reflection fallback.
     */
    public static ApplicationEventPublisherWrapper resolvePublisher(
            final Object source,
            final ApplicationEventPublisherWrapper explicitPublisher
    ) {
        if (explicitPublisher != null) return explicitPublisher;

        Class<?> current = source.getClass();
        while (current != null) {
            try {
                final Field field = current.getDeclaredField("publisher");
                field.setAccessible(true);
                final Object value = field.get(source);
                if (value instanceof ApplicationEventPublisherWrapper publisher) {
                    return publisher;
                }
            } catch (final NoSuchFieldException ex) {
                // 상위 클래스로 계속 탐색
            } catch (final Exception ex) {
                return null;
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
