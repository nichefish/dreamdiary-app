package io.nicheblog.dreamdiary.feature.schdul.kasi.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * HldyKasiApiRespDto
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 response Dto
 * </pre>
 *
 * @author nichefish
 */
@XmlRootElement(name = "response")
@Getter
@Setter
public class HldyKasiApiRespDto {

    /** header */
    private Map<String, String> header;

    /** body */
    private HldyKasiApiBodyDto body;

    /* ----- */

    @Override
    public String toString() {
        return "HldyKasiApiRespDto (header=" + header + ", body=" + body + ")";
    }
}
