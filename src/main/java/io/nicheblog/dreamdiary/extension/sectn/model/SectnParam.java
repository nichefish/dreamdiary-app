package io.nicheblog.dreamdiary.extension.sectn.model;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseParam;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * SectnParam
 * <pre>
 *  단락 인자 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SectnParam
        extends BaseParam {

    /** 정렬순서 배열 */
    private List<SectnDto> sortOrdr;
}
