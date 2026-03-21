package io.nicheblog.dreamdiary.feature.schdul.kasi.model;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * HldyKasiApiBodyDto
 * <pre>
 *  API:: 한국천문연구원(KASI):: 휴일 정보 날짜 responseBody Dto
 * </pre>
 *
 * @author nichefish
 */
@XmlRootElement(name = "body")
@Setter
public class HldyKasiApiBodyDto {

    /** 일자 목록 */
    private List<HldyKasiApiItemDto> items;

    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    public List<HldyKasiApiItemDto> getItems() {
        return items;
    }
}
