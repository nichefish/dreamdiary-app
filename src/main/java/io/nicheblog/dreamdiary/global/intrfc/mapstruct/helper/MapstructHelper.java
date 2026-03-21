package io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf.shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditRegDto;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cd.service.DtlCdService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

/**
 * MapstructHelper
 * <pre>
 *  Mapstruct에서 쓰는 공통 로직 분리
 * </pre>
 *
 * @author nichefish
 * TODO: 모듈 수가 증가할 경우 Strategy 기반 분리 고려.
 */
@Component
@RequiredArgsConstructor
public class MapstructHelper {

    private final DtlCdService dtlCdService;

    /**
     * Map Base-inheritted Fields (entity -> dto)
     *
     * @param entity 매핑할 Entity
     * @param dto 매핑 대상 Dto
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
        if (entity instanceof BaseAuditEntity baseEntity && dto instanceof BaseAuditDto) {
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
        // CLSF :: BaseClsfMapstruct쪽에 정의
    }

    /** 
     * 새 글 여부 처리 로직:: 메소드 분리
     *
     * @param entity 새 글 여부를 판단할 BaseClsfEntity 객체
     * @return 새 글이면 true, 그렇지 않으면 false
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
}
