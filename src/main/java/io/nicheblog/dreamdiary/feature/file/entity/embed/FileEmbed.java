package io.nicheblog.dreamdiary.feature.file.entity.embed;

import io.nicheblog.dreamdiary.feature.file.entity.FileRecordEntity;
import io.nicheblog.dreamdiary.feature.file.entity.FileGroupEntity;
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
 * FileEmbed
 * <pre>
 *  위임:: 첨부파일 관련 정보. (entity level)
 * </pre>
 *
 * @author nichefish
 * @see FileEmbedModule
 */
@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEmbed
        implements Serializable {

    /** 첨부파일 번호 (FK) */
    @Column(name = "file_group_id")
    private Integer fileGroupId;

    /** 첨부파일 정보 */
    @OneToOne
    @JoinColumn(name = "file_group_id", referencedColumnName = "id", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private FileGroupEntity fileGroupInfo;

    /**
     * 첨부파일 상세 목록 조회 Getter
     */
    public List<FileRecordEntity> getList() {
        if (this.fileGroupInfo == null) return null;

        return this.fileGroupInfo.getFileRecordList();
    };
}
