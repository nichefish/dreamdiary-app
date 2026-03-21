package io.nicheblog.dreamdiary.global.intrfc.entity.embed;

import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.infrastructure.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.infrastructure.file.entity.AtchFileEntity;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.List;

/**
 * AtchFileEmbed
 * <pre>
 *  위임:: 첨부파일 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see CommentEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtchFileEmbed
        implements Serializable {

    /** 첨부파일 번호 */
    @Column(name = "atch_file_no")
    private Integer atchFileNo;

    /** 첨부파일 정보 */
    @OneToOne
    @JoinColumn(name = "atch_file_no", referencedColumnName = "atch_file_no", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private AtchFileEntity atchFileInfo;

    /**
     * 첨부파일 상세 목록 조회 Getter
     */
    public List<AtchFileDtlEntity> getList() {
        if (this.atchFileInfo == null) return null;

        return this.atchFileInfo.getAtchFileList();
    };
}
