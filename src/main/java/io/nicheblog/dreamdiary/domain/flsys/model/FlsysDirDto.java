package io.nicheblog.dreamdiary.domain.flsys.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.io.File;
import java.io.Serializable;

/**
 * FlsysDirDto
 * <pre>
 *  파일시스템 디렉토리 요소 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlsysDirDto
        extends FlsysDto
        implements Serializable {

    /** totalSpace */
    @Positive
    private Long totalSpace;

    /** usableSpace */
    @Positive
    private Long usableSpace;

    /** freeSpace */
    @Positive
    private Long freeSpace;

    /* ----- */

    /**
     * 생성자.
     *
     * @param file 파일 객체 (File)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public FlsysDirDto(final File file) throws Exception {
        super(file);
        this.totalSpace = file.getTotalSpace();
        this.usableSpace = file.getUsableSpace();
        this.freeSpace = file.getFreeSpace();
    }
}
