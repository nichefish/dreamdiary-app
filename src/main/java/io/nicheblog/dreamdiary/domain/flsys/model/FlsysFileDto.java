package io.nicheblog.dreamdiary.domain.flsys.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.activation.MimetypesFileTypeMap;
import javax.validation.constraints.Positive;
import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * FlsysFileDto
 * <pre>
 *  파일시스템 파일 요소 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlsysFileDto
        extends FlsysDto
        implements Serializable {

    /** 실행가능여부 */
    private Boolean isExcecutable;

    /** VOD 여부 */
    private Boolean isVod;

    /** 컨텐츠 타입 */
    private String contentType;

    /** 파일 크기 */
    @Positive
    private Long length;

    /* ----- */

    /**
     * 생성자.
     *
     * @param file 파일 객체 (File)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    public FlsysFileDto(final File file) throws Exception {
        super(file);
        this.length = file.length();
        this.contentType = new MimetypesFileTypeMap().getContentType(file);
        this.isExcecutable = file.canExecute();
        String fileExtn = file.getName()
                .substring(file.getName()
                        .lastIndexOf('.') + 1);
        List<String> vodExtnList = List.of(new String[]{"mp3", "mp4", "mov", "avi", "webm", "webp", "wmv", "flv"});
        this.isVod = vodExtnList.contains(fileExtn);
    }
}
