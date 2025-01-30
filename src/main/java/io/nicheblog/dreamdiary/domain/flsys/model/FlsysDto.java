package io.nicheblog.dreamdiary.domain.flsys.model;

import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FlsysCmmDto
 * <pre>
 *  파일시스템 공통요소 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FlsysDto
        implements Comparable<FlsysDto>, Serializable {

    private File file;
    private FlsysMetaDto meta;

    /** 파일명 */
    private String fileNm;

    /** 절대경로 */
    private String filePath;

    /** 상위절대경로 */
    private String upperPath;

    private List<String> filePathStrList;

    /** 하위 폴더 목록 */
    private List<FlsysDirDto> dirList;

    /** 하위 파일 목록 */
    private List<FlsysFileDto> fileList;

    /** lastModified */
    private String lastModified;

    /* ----- */

    /**
     * 생성자.
     *
     * @param file 파일 객체 (File)
     * @throws Exception 파일 정보를 처리하는 중 발생할 수 있는 예외
     */
    public FlsysDto(final File file) throws Exception {
        this.file = file;
        this.upperPath = (file.getParent() != null ? file.getParent() : "/").replace("\\", "/");
        this.fileNm = file.getName()
                          .replace("\\", "/");
        this.filePath = file.getPath()
                            .replace("\\", "/");

        this.filePathStrList = Arrays.stream(this.filePath.split("/"))
                .collect(Collectors.toList());
        Timestamp timestamp = new Timestamp(file.lastModified());
        this.lastModified = DateUtils.asStr(new Date(timestamp.getTime()), DatePtn.DATETIME);
    }

    /**
     * Getter :: 메타 정보 존쟈 여부를 반환합니다.
     *
     * @return {@link Boolean} -- 메타 정보가 있으면 true, 없으면 false
     */
    public Boolean getHasMeta() {
        return this.meta != null;
    }

    /**
     * 파일 이름 기준 정렬 (내림차순)
     *
     * @param other - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull FlsysDto other) {
        String fileNm = this.fileNm;
        if (fileNm == null) return -1;

        return fileNm.compareTo(other.getFileNm());
    }
}
