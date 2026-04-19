package io.nicheblog.dreamdiary.feature.calendar.holiday.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * HolydayKasiApiRespDto
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 response Dto
 * </pre>
 *
 * @author nichefish
 */
@XmlRootElement(name = "response")
@Getter
@Setter
public class HolydayKasiApiRespDto {

    /** header */
    private Map<String, String> header;

    /** body */
    private HolydayKasiApiBodyDto body;

    /* ----- */

    @Override
    public String toString() {
        return "HolydayKasiApiRespDto (header=" + header + ", body=" + body + ")";
    }
}
