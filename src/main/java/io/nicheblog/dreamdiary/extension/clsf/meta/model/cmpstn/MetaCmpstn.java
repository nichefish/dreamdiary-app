package io.nicheblog.dreamdiary.extension.clsf.meta.model.cmpstn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaContentDto;
import io.nicheblog.dreamdiary.extension.clsf.meta.model.MetaDto;
import io.nicheblog.dreamdiary.global.intrfc.model.tagify.BaseTagifyDataDto;
import io.nicheblog.dreamdiary.global.intrfc.model.tagify.BaseTagifyDto;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MetaCmpstn
 * <pre>
 *  위임 :: 메타 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 * @see MetaCmpstnModule
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaCmpstn
        implements Serializable {

    /** 컨텐츠 타입 :: 상위에서 주입받음. */
    private String contentType;

    /** 메타-컨텐츠 목록 */
    private List<MetaContentDto> list;

    /** 메타-컨텐츠 문자열 목록 */
    private List<String> metaStrList;

    /** 메타-컨텐츠 문자열 (','로 구분) */
    private String metaListStr;

    /* ----- */

    /**
     * Metaify 형식의 문자열을 파싱하여 "value" 리스트로 반환하는 메서드.
     * Metaify (ex.) = [{"value":"123.123.123.123"},{"value":"234.234.234.234"}] 문자열 형식으로 넘어온댜.
     * @return List<String> - 파싱된 문자열 값들의 리스트. 문자열이 비어 있을 경우 빈 리스트 반환.
     */
    public List<String> getParsedMetaStrList() {
        if (StringUtils.isEmpty(this.metaListStr)) return new ArrayList<>();
        final JSONArray jArray = new JSONArray(metaListStr);
        return IntStream.range(0, jArray.length())
                .mapToObj(jArray::getJSONObject)
                .map(json -> json.getString("value"))
                .collect(Collectors.toList());
    }

    /**
     * Metaify 형식의 문자열을 파싱하여 MetaDto 리스트로 반환하는 메서드.
     * Metaify (ex.) = [{"value":"123.123.123.123"},{"value":"234.234.234.234"}] 문자열 형식으로 넘어온댜.
     * @return List<MetaDto> - 파싱된 MetaDto 객체들의 리스트. 문자열이 비어 있을 경우 빈 리스트 반환.
     */
    public List<MetaDto> getParsedMetaList() {
        if (StringUtils.isEmpty(this.metaListStr)) return new ArrayList<>();
        final JSONArray jArray = new JSONArray(metaListStr);
        return IntStream.range(0, jArray.length())
                .mapToObj(jArray::getJSONObject)
                .map(json -> {
                    final String metaNm = json.getString("value").trim().replaceAll("\\s+", "_");
                    if (!json.has("data") || json.optJSONObject("data") == null) return new MetaDto(metaNm);
                    final String ctgr = json.getJSONObject("data").getString("ctgr");
                    final String rawValue = json.getJSONObject("data").getString("value");
                    String label = null, valueWithUnit = null, value = null, unit = null;
                    if (rawValue.contains(":")) {
                        final String[] parts = rawValue.split(":", 2);
                        label = parts[0].trim();      // 메신저
                        valueWithUnit = parts[1].trim();
                    } else {
                        label = "";
                        valueWithUnit = rawValue.trim();
                    }
                    final Pattern p = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)(.*)$");
                    final Matcher m = p.matcher(valueWithUnit.trim());
                    if (m.matches()) {
                        value = m.group(1).trim();  // "981" / "72.5"
                        unit = m.group(2).trim();   // "개" / "kg"
                    }
                    return MetaDto.builder().metaNm(metaNm).ctgr(ctgr).label(label).value(value).unit(unit).build();
                })
                .collect(Collectors.toList());
    }

    /* ----- */

    /**
     * Getter :: 메타 목록을 문자열로 반환
     * @return String - 콤마로 구분된 메타 이름 문자열, 리스트가 비어 있을 경우 null 반환
     */
    public String getMetaListStr() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        final ObjectMapper mapper = new ObjectMapper();
        return this.list.stream()
                .sorted()
                .map(meta -> {
                    try {
                        final BaseTagifyDataDto data = BaseTagifyDataDto.builder().ctgr(meta.getCtgr()).value(meta.getLabel() + ":" + meta.getValue() + meta.getUnit()).build();
                        final BaseTagifyDto tagifyDto = new BaseTagifyDto(meta.getMetaNm(), data);
                        return mapper.writeValueAsString(tagifyDto);
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException("Error processing JSON", e);
                    }
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Getter :: 메타 목록을 리스트로 반환.
     * @return List<String> - 메타 이름의 리스트, 리스트가 비어 있을 경우 null 반환
     */
    public List<String> getMetaStrList() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .sorted()
                .map(meta -> meta.getMeta().getMetaNm())
                .collect(Collectors.toList());
    }
}