package io.nicheblog.dreamdiary.extension.clsf.meta.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * MetaContentDto
 * <pre>
 *  메타-컨텐츠 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MetaContentDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Comparable<MetaContentDto> {

    /** 메타-컨텐츠 번호 (PK) */
    @Positive
    private Integer metaContentNo;

    /** 참조 메타 번호 */
    @Positive
    private Integer refMetaNo;

    /** 참조 글 번호 */
    @Positive
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Size(max = 50)
    private String refContentType;

    /** 메타 카테고리 */
    @Size(max = 50)
    private String ctgr;

    /** 메타 정보 */
    private MetaDto meta;

    /** 메타 이름 */
    @Size(max = 50)
    private String metaNm;

    /** 메타 값 */
    private String label;

    /** 메타 값 */
    private String value;

    /** 메타 값 */
    private String unit;

    /* ----- */

    /**
     * 메타이름 오름차순 정렬
     *
     * @param compare - 비교할 객체
     * @return 양수: 현재 객체가 더 큼, 음수: 현재 객체가 더 작음, 0: 두 객체가 같음
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull MetaContentDto compare) {
        final String thisMetaNm = this.getMetaNm();
        final String otherMetaNm = compare.getMetaNm();
        return thisMetaNm.compareTo(otherMetaNm);
    }

    @Override
    public Integer getKey() {
        return this.metaContentNo;
    }
}
