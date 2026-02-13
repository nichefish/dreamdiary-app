package io.nicheblog.dreamdiary.global.intrfc.model.cmpstn;

import io.nicheblog.dreamdiary.extension.file.model.AtchFileDtlDto;
import io.nicheblog.dreamdiary.extension.file.model.AtchFileDto;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * AtchFileCmpstn
 * <pre>
 *  위임:: 첨부파일 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see AtchFileCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtchFileCmpstn
        implements Serializable {

    /** 첨부파일 번호 */
    private Integer atchFileNo;
    /** 첨부파일 정보 */
    private AtchFileDto atchFileInfo;

    /**
     * 첨부파일 상세 목록 조회 Getter
     */
    public List<AtchFileDtlDto> getList() {
        if (this.atchFileInfo == null) return null;

        return this.atchFileInfo.getAtchFileList();
    }

    /**
     * 첨부파일 존재 여부
     * @return 첨부파일 존재 여부
     */
    public Boolean hasAtchFile() {
        if (this.atchFileInfo == null) return false;
        return !CollectionUtils.isEmpty(this.atchFileInfo.getAtchFileList());
    }
}