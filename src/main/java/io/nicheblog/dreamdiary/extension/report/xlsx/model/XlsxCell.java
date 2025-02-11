package io.nicheblog.dreamdiary.extension.report.xlsx.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * XlsxCell
 * <pre>
 *  (공통) 엑셀 셀 정보.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@AllArgsConstructor
public class XlsxCell {

    /** 셀 헤더 */
    private String header;

    /** 길이 */
    private Integer length;
}
