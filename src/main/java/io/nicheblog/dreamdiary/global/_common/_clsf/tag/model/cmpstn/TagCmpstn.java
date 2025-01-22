package io.nicheblog.dreamdiary.global._common._clsf.tag.model.cmpstn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.ContentTagDto;
import io.nicheblog.dreamdiary.global._common._clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.global.intrfc.model.tagify.BaseTagifyDataDto;
import io.nicheblog.dreamdiary.global.intrfc.model.tagify.BaseTagifyDto;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * TagCmpstn
 * <pre>
 *  위임 :: 태그 관련 정보. (dto level)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagCmpstn
        implements Serializable {

    /** 컨텐츠 타입 :: 상위에서 주입받음. */
    private String contentType;

    /** 컨텐츠 태그 목록 */
    private List<ContentTagDto> list;

    /** 컨텐츠 태그 문자열 목록 */
    private List<String> tagStrList;

    /** 컨텐츠 태그 문자열 (','로 구분) */
    private String tagListStr;

    /* ----- */

    /**
     * Tagify 형식의 문자열을 파싱하여 "value" 리스트로 반환하는 메서드.
     * Tagify (ex.) = [{"value":"123.123.123.123"},{"value":"234.234.234.234"}] 문자열 형식으로 넘어온댜.
     * @return List<String> - 파싱된 문자열 값들의 리스트. 문자열이 비어 있을 경우 빈 리스트 반환.
     */
    public List<String> getParsedTagStrList() {
        if (StringUtils.isEmpty(this.tagListStr)) return new ArrayList<>();
        final JSONArray jArray = new JSONArray(tagListStr);
        return IntStream.range(0, jArray.length())
                .mapToObj(jArray::getJSONObject)
                .map(json -> json.getString("value"))
                .collect(Collectors.toList());
    }

    /**
     * Tagify 형식의 문자열을 파싱하여 TagDto 리스트로 반환하는 메서드.
     * Tagify (ex.) = [{"value":"123.123.123.123"},{"value":"234.234.234.234"}] 문자열 형식으로 넘어온댜.
     * @return List<TagDto> - 파싱된 TagDto 객체들의 리스트. 문자열이 비어 있을 경우 빈 리스트 반환.
     */
    public List<TagDto> getParsedTagList() {
        if (StringUtils.isEmpty(this.tagListStr)) return new ArrayList<>();
        final JSONArray jArray = new JSONArray(tagListStr);
        return IntStream.range(0, jArray.length())
                .mapToObj(jArray::getJSONObject)
                .map(json -> {
                    if (!json.has("data") || json.getJSONObject("data") == null) return new TagDto(json.getString("value"));
                    return new TagDto(json.getString("value"), json.getJSONObject("data").getString("ctgr"));
                })
                .collect(Collectors.toList());
    }

    /* ----- */

    /**
     * Getter :: 태그 목록을 문자열로 반환
     * @return String - 콤마로 구분된 태그 이름 문자열, 리스트가 비어 있을 경우 null 반환
     */
    public String getTagListStr() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .sorted()
                .map(tag -> tag.getTag().getTagNm())
                .collect(Collectors.joining(","));
    }

    /**
     * Getter :: 태그 목록을 문자열로 반환
     * @return String - 콤마로 구분된 태그 이름 문자열, 리스트가 비어 있을 경우 null 반환
     */
    public String getHashTagStr() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .sorted()
                .map(tag -> "#" + tag.getTag().getTagNm())  // 각 태그 앞에 #을 붙임
                .collect(Collectors.joining(" "));  // 태그들 사이에 띄어쓰기 추가
    }

    /**
     * Getter :: 태그 목록과 카테고리를 포함한 문자열로 반환.
     * @return String - JSON 형식의 태그와 카테고리 문자열, 리스트가 비어 있을 경우 null 반환
     */
    public String getTagListStrWithCtgr() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        final ObjectMapper mapper = new ObjectMapper();
        return this.list.stream()
                .sorted()
                .map(tag -> {
                    try {
                        return mapper.writeValueAsString(new BaseTagifyDto(tag.getTagNm(), new BaseTagifyDataDto(tag.getCtgr())));
                    } catch (final JsonProcessingException e) {
                        throw new RuntimeException("Error processing JSON", e);
                    }
                })
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Getter :: 태그 목록을 리스트로 반환.
     * @return List<String> - 태그 이름의 리스트, 리스트가 비어 있을 경우 null 반환
     */
    public List<String> getTagStrList() {
        if (CollectionUtils.isEmpty(this.list)) return null;
        return this.list.stream()
                .sorted()
                .map(tag -> tag.getTag().getTagNm())
                .collect(Collectors.toList());
    }
}