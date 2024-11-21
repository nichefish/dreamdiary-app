package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global._common._clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.comment.mapstruct.embed.CommentEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.comment.model.cmpstn.CommentCmpstnModule;
import io.nicheblog.dreamdiary.global._common._clsf.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.managt.mapstruct.embed.ManagtEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.managt.model.cmpstn.ManagtCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.managt.model.cmpstn.ManagtCmpstnModule;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.entity.embed.SectnEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.entity.embed.SectnEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.mapstruct.embed.SectnEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.cmpstn.SectnCmpstnModule;
import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.state.mapstruct.embed.StateEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.state.model.cmpstn.StateCmpstnModule;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.tag.mapstruct.embed.TagEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn.TagCmpstnModule;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.mapstruct.embed.ViewerEmbedMapstruct;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.model.cmpstn.ViewerCmpstn;
import io.nicheblog.dreamdiary.global._common._clsf.viewer.model.cmpstn.ViewerCmpstnModule;
import io.nicheblog.dreamdiary.global._common.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global._common.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global.intrfc.entity.*;
import io.nicheblog.dreamdiary.global.intrfc.model.*;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.mapstruct.MappingTarget;
import org.springframework.util.CollectionUtils;

/**
 * MapstructHelper
 * <pre>
 *  Mapstruct에서 쓰는 공통 로직 분리
 * </pre>
 *
 * @author nichefish
 */
public class MapstructHelper {

    /**
     * Map Base-inheritted Fields
     *
     * @param entity 매핑할 Entity
     * @param dto 매핑 대상 Dto
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    public static <Entity extends BaseCrudEntity, Dto extends BaseCrudDto> void mapBaseFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {

        // AUDIT_REG :: 공통 필드 매핑 로직
        if (entity instanceof BaseAuditRegEntity && dto instanceof BaseAuditRegDto) {
            final BaseAuditRegEntity baseEntity = ((BaseAuditEntity) entity);
            final AuditorInfo regstrInfo = baseEntity.getRegstrInfo();
            if (regstrInfo != null) {
                // 작성자 이름
                ((BaseAuditRegDto) dto).setRegstrNm(baseEntity.getRegstrInfo().getNickNm());
                // 작성일사
                ((BaseAuditRegDto) dto).setRegDt(DateUtils.asStr(baseEntity.getRegDt(), DatePtn.DATETIME));
                // 작성자 여부
                ((BaseAuditRegDto) dto).setIsRegstr(baseEntity.isRegstr());
            }
        }
        // AUDIT :: 공통 필드 매핑 로직
        if (entity instanceof BaseAuditEntity && dto instanceof BaseAuditDto) {
            final BaseAuditEntity baseEntity = ((BaseAuditEntity) entity);
            final AuditorInfo mdfusrInfo = baseEntity.getMdfusrInfo();
            if (mdfusrInfo != null) {
                // 수정자 이름
                ((BaseAuditDto) dto).setMdfusrNm(baseEntity.getMdfusrInfo().getNickNm());
                // 수정일시
                ((BaseAuditDto) dto).setMdfDt(DateUtils.asStr(baseEntity.getMdfDt(), DatePtn.DATETIME));
                // 수정자 여부
                ((BaseAuditDto) dto).setIsMdfusr(baseEntity.isMdfusr());
            }
        }
        // MANAGE :: ...
        // ATCH :: 공통 필드 매핑 로직
        if (entity instanceof BaseAtchEntity && dto instanceof BaseAtchDto) {
            final BaseAtchEntity baseEntity = ((BaseAtchEntity) entity);
            // 첨부파일 존재 여부
            final Boolean hasAtchFile = !(baseEntity.getAtchFileNo() == null || baseEntity.getAtchFileInfo() == null || CollectionUtils.isEmpty(baseEntity.getAtchFileInfo().getAtchFileList()));
            ((BaseAtchDto) dto).setHasAtchFile(hasAtchFile);
        }
        // CLSF :: BaseClsfMapstruct쪽에 정의
        // POST :: 공통 필드 매핑 로직
        if (entity instanceof BasePostEntity && dto instanceof BasePostDto) {
            final BasePostEntity baseEntity = ((BasePostEntity) entity);
            // 글분류 이름
            if (baseEntity.getCtgrCdInfo() != null) {
                ((BasePostDto) dto).setCtgrNm(baseEntity.getCtgrCdInfo().getDtlCdNm());
                ((BasePostDto) dto).setHasCtgrNm(baseEntity.getCtgrCdInfo() != null);
            }
        }
    }

    /**
     * Map Clsf Fields
     *
     * @param entity 매핑할 Entity
     * @param dto 매핑 대상 Dto
     */
    public static <Entity extends BaseClsfEntity, Dto extends BaseClsfDto> void mapClsfFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        // 댓글 :: 공통 필드 매핑 로직
        boolean usesCommentModule = (entity instanceof CommentEmbedModule && dto instanceof CommentCmpstnModule);
        if (usesCommentModule) {
            final CommentEmbed embed = ((CommentEmbedModule) entity).getComment();
            final CommentCmpstn cmpstn = CommentEmbedMapstruct.INSTANCE.toDto(embed);
            ((CommentCmpstnModule) dto).setComment(cmpstn);
        }

        // 단락 :: 공통 필드 매핑 로직
        boolean usesSectnModule = (entity instanceof SectnEmbedModule && dto instanceof SectnCmpstnModule);
        if (usesSectnModule) {
            final SectnEmbed embed = ((SectnEmbedModule) entity).getSectn();
            final SectnCmpstn cmpstn = SectnEmbedMapstruct.INSTANCE.toDto(embed);
            ((SectnCmpstnModule) dto).setSectn(cmpstn);
        }

        // 태그 :: 공통 필드 매핑 로직
        boolean usesTagModule = (entity instanceof TagEmbedModule && dto instanceof TagCmpstnModule);
        if (usesTagModule) {
            final TagEmbed embed = ((TagEmbedModule) entity).getTag();
            final TagCmpstn cmpstn = TagEmbedMapstruct.INSTANCE.toDto(embed);
            ((TagCmpstnModule) dto).setTag(cmpstn);
        }

        // 조치 :: 공통 필드 매핑 로직
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

        // 새 글 여부 표시
        if (usesManagtModule && usesViewerModule) {
            ((ViewerCmpstnModule) dto).setIsNew(determineIfNew(entity));
        }
    }

    /**
     * Map State Fields
     *
     * @param entity 매핑할 Dto
     * @param dto 매핑 대상 Entity
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    public static <Entity, Dto> void mapStateFields(final Dto dto, final @MappingTarget Entity entity) throws Exception {
        // 댓글 :: 공통 필드 매핑 로직
        boolean usesStateModule = (entity instanceof StateEmbedModule && dto instanceof StateCmpstnModule);
        if (!usesStateModule) return;
        StateEmbed embed = ((StateEmbedModule) entity).getState();
        if (embed == null) embed = new StateEmbed();
        StateCmpstn cmpstn = ((StateCmpstnModule) dto).getState();
        if (cmpstn == null) cmpstn = new StateCmpstn();
        StateEmbedMapstruct.INSTANCE.updateFromDto(cmpstn, embed);
        ((StateEmbedModule) entity).setState(embed);
    }

    /** 
     * 새 글 여부 처리 로직:: 메소드 분리
     *
     * @param entity 새 글 여부를 판단할 BaseClsfEntity 객체
     * @return 새 글이면 true, 그렇지 않으면 false
     * @throws Exception 판단 중 발생할 수 있는 예외
     */
    public static <Entity extends BaseClsfEntity, Dto extends BaseClsfDto> Boolean determineIfNew(Entity entity) throws Exception {

        if (((ManagtEmbedModule) entity).getManagt() == null || ((ManagtEmbedModule) entity).getManagt().getManagtDt() == null) return false;
        // 최종수정 이후 7일 지난 글은 새 글이 아님
        if (!((ManagtEmbedModule) entity).getManagt().getManagtDt().after(DateUtils.getCurrDateAddDay(-7))) return false;
        // 내가 최종수정자면 false
        if (AuthUtils.isRegstr(((ManagtEmbedModule) entity).getManagt().getManagtrId())) return false;
        // 열람자에 내가 없으면 true
        if (((ViewerEmbedModule) entity).getViewer() == null || CollectionUtils.isEmpty(((ViewerEmbedModule) entity).getViewer().getList())) return true;
        return ((ViewerEmbedModule) entity).getViewer().getList().stream()
                .anyMatch(e -> !AuthUtils.getLgnUserId().equals(e.getRegstrId()));
    }

    /**
     * Map Post Fields
     *
     * @param entity 매핑할 Entity
     * @param dto 매핑 대상 Dto
     */
    public static <Entity extends BaseClsfEntity, Dto extends BaseClsfDto> void mapPostFields(Entity entity, Dto dto) {
        // AUDIT :: 공통 필드 매핑 로직
        if (entity instanceof BasePostEntity && dto instanceof BasePostDto) {
            if (((BasePostEntity) entity).getCtgrCdInfo() != null) {
                ((BasePostDto) dto).setCtgrNm(((BasePostEntity) entity).getCtgrCdInfo().getDtlCdNm());
            }
        }
    }

}