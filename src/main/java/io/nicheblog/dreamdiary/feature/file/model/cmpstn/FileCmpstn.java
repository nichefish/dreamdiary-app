package io.nicheblog.dreamdiary.feature.file.model.cmpstn;

import io.nicheblog.dreamdiary.feature.file.model.FileRecordDto;
import io.nicheblog.dreamdiary.feature.file.model.FileGroupDto;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * FileCmpstn
 * <pre>
 *  위임:: 첨부파일 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see FileCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileCmpstn
        implements Serializable {

    /** 첨부파일 번호 (FK) */
    private Integer fileGroupId;
    /** 첨부파일 정보 */
    private FileGroupDto fileGroupInfo;

    /**
     * 첨부파일 상세 목록 조회 Getter
     */
    public List<FileRecordDto> getList() {
        if (this.fileGroupInfo == null) return null;

        return this.fileGroupInfo.getFileRecordList();
    }

    /**
     * 첨부파일 존재 여부
     * @return 첨부파일 존재 여부
     */
    public Boolean hasFileGroup() {
        if (this.fileGroupInfo == null) return false;
        return !CollectionUtils.isEmpty(this.fileGroupInfo.getFileRecordList());
    }
}
