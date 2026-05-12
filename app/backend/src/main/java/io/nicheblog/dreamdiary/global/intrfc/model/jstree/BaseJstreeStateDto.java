package io.nicheblog.dreamdiary.global.intrfc.model.jstree;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BaseJstreeStateDto
 * <pre>
 *  (공통) Jstree 상태 { state } 정보 dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseJstreeStateDto {

    /** opened */
    @JsonProperty("opened")
    private boolean opened;

    /** disabled */
    @JsonProperty("disabled")
    private boolean disabled;

    /** selected */
    @JsonProperty("selected")
    private boolean selected;
}
