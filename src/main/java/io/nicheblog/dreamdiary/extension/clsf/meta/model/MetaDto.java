package io.nicheblog.dreamdiary.extension.clsf.meta.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * MetaDto
 * <pre>
 *  메타 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"value", "metaNm"}, callSuper = false)
public class MetaDto
        extends BaseCrudDto
        implements Identifiable<Integer>, Comparable<MetaDto> {

    /** 메타 번호 (PK) */
    @Positive
    private Integer metaNo;

    /** 태그 카테고리 */
    @Builder.Default
    @Size(max = 50)
    private String ctgr = "";

    /** 메타 */
    @Size(max = 50)
    private String metaNm;

    /** 메타 라벨 */
    @Builder.Default
    @Size(max = 50)
    private String label = "";

    /** 메타 값 */
    @Builder.Default
    @Size(max = 50)
    private String value = "";

    /** 메타 단위 */
    @Builder.Default
    @Size(max = 50)
    private String unit = "";

    /** 게시물 메타 목록 (=게시물 리스트) */
    private List<MetaContentDto> metaContentList;

    /** 게시물 목록 (게시물 메타 목록을 글 목록으로 정제한 버전) */
    private List<?> contentList;

    /** 메타 크기 (=컨텐츠 개수) */
    @Builder.Default
    private Integer contentSize = 0;

    /** 글자 크기 클래스 */
    private String metaClass;

    /* ----- */

    /**
     * 생성자.
     *
     * @param metaNm - 생성할 메타의 이름
     */
    public MetaDto(final String metaNm) {
        this.metaNm = metaNm;
        this.value = "";
    }

    /**
     * 생성자.
     *
     * @param metaNm - 생성할 메타의 이름
     * @param value - 생성할 메타의 값
     */
    public MetaDto(final String metaNm, final String value) {
        this.metaNm = metaNm;
        this.value =  StringUtils.isEmpty(value) ? "" : value;
    }

    /**
     * 생성자.

     * @param metaNm - 생성할 메타 번호
     * @param value - 생성할 메타의 값
     */
    public MetaDto(final Integer metaNo, final String metaNm, final String value) {
        this(metaNm, value);
        this.metaNo = metaNo;
    }

    /**
     * 메타이름 오름차순 정렬
     *
     * @param other - 비교할 `MetaDto` 객체
     * @return int - 사전적으로 앞서면 음수, 같으면 0, 뒤에 있으면 양수를 반환
     */
    @SneakyThrows
    @Override
    public int compareTo(final @NotNull MetaDto other) {
        String thisMetaNm = this.getMetaNm();
        String otherMetaNm = other.getMetaNm();
        return thisMetaNm.compareTo(otherMetaNm);
    }

    @Override
    public Integer getKey() {
        return this.metaNo;
    }
}
