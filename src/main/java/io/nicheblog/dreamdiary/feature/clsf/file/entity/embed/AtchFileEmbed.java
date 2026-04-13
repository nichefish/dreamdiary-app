package io.nicheblog.dreamdiary.feature.clsf.file.entity.embed;

import io.nicheblog.dreamdiary.feature.clsf.file.entity.AtchFileDtlEntity;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.AtchFileEntity;
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
 * @see AtchFileEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtchFileEmbed
        implements Serializable {

    /** 첨부파일 번호 (FK) */
    @Column(name = "atch_file_id")
    private Integer atchFileId;

    /** 첨부파일 정보 */
    @OneToOne
    @JoinColumn(name = "atch_file_id", referencedColumnName = "id", insertable = false, updatable = false)
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
