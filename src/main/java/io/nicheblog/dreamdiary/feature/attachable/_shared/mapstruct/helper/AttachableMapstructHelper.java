package io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.helper;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.history.mapstruct.embed.HistoryEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.history.model.cmpstn.HistoryCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.managt.mapstruct.embed.ManagtEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.managt.model.cmpstn.ManagtCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.embed.MetaEmbed;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.embed.MetaEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.meta.mapstruct.embed.MetaEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.state.mapstruct.embed.StateEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.feature.attachable.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * MapstructHelper
 * <pre>
 *  Mapstruct에서 쓰는 공통 로직 분리
 * </pre>
 *
 * @author nichefish
 * TODO: 모듈 수가 증가하면 Strategy 기반 분리 고려.
 */
@Component
@RequiredArgsConstructor
public class AttachableMapstructHelper {

    private final CodeLookupService codeLookupService;

    /**
     * Map Attachable Fields (entity -> dto)
     *
     * @param entity 매핑용 Entity
     * @param dto 매핑 대상 Dto
     */
    public static <Entity extends BaseAttachableEntity, Dto extends BaseAttachableDto> void mapAttachableFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        // 댓글 :: 공통 필드 매핑 로직
        boolean usesCommentModule = (entity instanceof CommentEmbedModule && dto instanceof CommentCmpstnModule);
        if (usesCommentModule) {
            final CommentEmbed embed = ((CommentEmbedModule) entity).getComment();
            final CommentCmpstn cmpstn = CommentEmbedMapstruct.INSTANCE.toDto(embed);
            ((CommentCmpstnModule) dto).setComment(cmpstn);
        }

        // 상태 :: 공통 필드 매핑 로직
        boolean usesStateModule = (entity instanceof StateEmbedModule && dto instanceof StateCmpstnModule);
        if (usesStateModule) {
            final StateEmbed embed = ((StateEmbedModule) entity).getState();
            final StateCmpstn cmpstn = StateEmbedMapstruct.INSTANCE.toDto(embed);
            ((StateCmpstnModule) dto).setState(cmpstn);
        }

        // 태그 :: 공통 필드 매핑 로직
        boolean usesTagModule = (entity instanceof TagEmbedModule && dto instanceof TagCmpstnModule);
        if (usesTagModule) {
            final TagEmbed embed = ((TagEmbedModule) entity).getTag();
            final TagCmpstn cmpstn = TagEmbedMapstruct.INSTANCE.toDto(embed);
            ((TagCmpstnModule) dto).setTag(cmpstn);
        }

        // 메타 :: 공통 필드 매핑 로직
        boolean usesMetaModule = (entity instanceof MetaEmbedModule && dto instanceof MetaCmpstnModule);
        if (usesMetaModule) {
            final MetaEmbed embed = ((MetaEmbedModule) entity).getMeta();
            final MetaCmpstn cmpstn = MetaEmbedMapstruct.INSTANCE.toDto(embed);
            ((MetaCmpstnModule) dto).setMeta(cmpstn);
        }

        // 이력 :: 공통 필드 매핑 로직
        boolean usesHistoryModule = (entity instanceof HistoryEmbedModule && dto instanceof HistoryCmpstnModule);
        if (usesHistoryModule) {
            final HistoryEmbed embed = ((HistoryEmbedModule) entity).getHistory();
            final HistoryCmpstn cmpstn = HistoryEmbedMapstruct.INSTANCE.toDto(embed);
            ((HistoryCmpstnModule) dto).setHistory(cmpstn);
        }

        boolean usesManagtModule = (entity instanceof ManagtEmbedModule && dto instanceof ManagtCmpstnModule);
        if (usesManagtModule) {
            final ManagtEmbed embed = ((ManagtEmbedModule) entity).getManagt();
            final ManagtCmpstn cmpstn = ManagtEmbedMapstruct.INSTANCE.toDto(embed);
            ((ManagtCmpstnModule) dto).setManagt(cmpstn);
        }

        // 열람 :: 공통 필드 매핑 로직
        boolean usesViewerModule = (entity instanceof ViewerEmbedModule && dto instanceof ViewerCmpstnModule);
        if (usesViewerModule) {
            final ViewerEmbed embed = ((ViewerEmbedModule) entity).getViewer();
            final ViewerCmpstn cmpstn = ViewerEmbedMapstruct.INSTANCE.toDto(embed);
            ((ViewerCmpstnModule) dto).setViewer(cmpstn);
        }

        // 새 글 여부 상태
        if (usesManagtModule && usesViewerModule) {
            ((ViewerCmpstnModule) dto).setIsNew(determineIfAttachableNew(entity));
        }
    }

    /**
     * 새 글 여부 처리 로직:: 메소드 분리
     *
     * @param entity 새 글 여부를 판단할 BaseAttachableEntity 객체
     * @return 새 글이면 true, 그렇지 않으면 false
     */
    public static <Entity extends BaseAttachableEntity, Dto extends BaseAttachableDto> Boolean determineIfAttachableNew(final Entity entity) throws Exception {
        if (((ManagtEmbedModule) entity).getManagt() == null || ((ManagtEmbedModule) entity).getManagt().getManagtDt() == null) return false;
        // 최종수정 이후 7일 지난 글은 새 글이 아님
        if (!((ManagtEmbedModule) entity).getManagt().getManagtDt().after(DateUtils.getCurrDateAddDay(-7))) return false;
        // 내가 최종수정자면 false
        if (AuthUtils.isCreatedBy(((ManagtEmbedModule) entity).getManagt().getManagtrId())) return false;
        // 열람자에 내가 없으면 true
        if (((ViewerEmbedModule) entity).getViewer() == null || CollectionUtils.isEmpty(((ViewerEmbedModule) entity).getViewer().getList())) return true;
        return ((ViewerEmbedModule) entity).getViewer().getList().stream()
                .anyMatch(e -> !Objects.equals(AuthUtils.getLoginUsername(), e.getCreatedBy()));
    }
}
