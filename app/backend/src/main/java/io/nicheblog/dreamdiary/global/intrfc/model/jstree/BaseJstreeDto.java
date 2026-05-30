package io.nicheblog.dreamdiary.global.intrfc.model.jstree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * BaseJstreeDto
 * <pre>
 *  (공통) Jstree Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseJstreeDto {

    /** 노드 고유 ID */
    @JsonProperty("id")
    protected String id;

    /** 상위 노드 ID */
    @JsonProperty("parent")
    protected String parent;

    /** 표시 */
    @JsonProperty("text")
    protected String test;

    /** 상태 */
    @JsonProperty("state")
    private BaseJstreeStateDto state;

    /** 하위 노드 */
    @JsonProperty("children")
    protected List<BaseJstreeDto> children;

    /* ----- */

    /**
     * 노드의 "열림" 상태를 설정하는 메서드.
     */
    public void setOpenedState() {
        if (this.state == null) this.state = new BaseJstreeStateDto();
        this.state.setOpened(true);
    }

    /**
     * 노드의 "선택" 상태를 설정하는 메서드.
     */
    public void setSelectedState() {
        if (this.state == null) this.state = new BaseJstreeStateDto();
        this.state.setSelected(true);
    }

    /**
     * 노드의 "열림" + "선택" 상태를 설정하는 메서드.
     */
    public void setOpenedAndSelectedState() {
        if (this.state == null) this.state = new BaseJstreeStateDto();
        this.state.setOpened(true);
        this.state.setSelected(true);
    }
}
