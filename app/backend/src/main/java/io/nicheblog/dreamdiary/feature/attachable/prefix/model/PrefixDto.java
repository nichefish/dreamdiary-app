package io.nicheblog.dreamdiary.feature.attachable.prefix.model;

import lombok.*;

import javax.validation.constraints.*;

/**
 * 말머리 관리·표시 DTO.
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PrefixDto {

    @Positive
    private Integer id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;

    @NotNull
    @PositiveOrZero
    private Integer sortOrder;

    @Pattern(regexp = "[YN]")
    private String activeYn;
}
