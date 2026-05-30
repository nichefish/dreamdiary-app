package io.nicheblog.dreamdiary.feature.calendar.holiday.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * HolydayKasiApiItemDto
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 날짜 item Dto
 * </pre>
 *
 * @author nichefish
 */
@XmlRootElement(name = "item")
@Getter
@Setter
public class HolydayKasiApiItemDto {

    /** 날짜 */
    private String locdate;

    /** 명칭 */
    private String dateName;
}
