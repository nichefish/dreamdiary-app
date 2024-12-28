package io.nicheblog.dreamdiary.domain.jrnl.diary.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JrnlDiaryTagCtgrSynchronizer
 * <pre>
 *  저널 일기 태그 카테고리 메타 파일-DB 동기화 모듈
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDiaryTagCtgrSynchronizer")
@RequiredArgsConstructor
@Log4j2
public class JrnlDiaryTagCtgrSynchronizer {

    private final JrnlDiaryTagService jrnlDreamTagService;

    /**
     * 태그 조회해서 파일 생성
     */
    public void tagSync() throws Exception {
        final List<JrnlDiaryTagEntity> tagList = jrnlDreamTagService.getListEntity(new HashMap<>());

        final Map<String, List<String>> tagCtgrMap = tagList.stream()
                .collect(Collectors.groupingBy(
                        JrnlDiaryTagEntity::getTagNm,
                        Collectors.mapping(tag -> {
                            if (StringUtils.isBlank(tag.getCtgr())) return "";
                            return tag.getCtgr();
                        }, Collectors.toList())
                ));

        // 각 태그의 카테고리 리스트를 정렬
        tagCtgrMap.forEach((key, value) -> value.sort(String::compareTo));

        // 파일 생성 (메소드 분리)
        this.writeToFile(tagCtgrMap);
    }

    /**
     * 태그 카테고리 데이터를 기반으로 파일을 생성합니다.
     *
     * @param tagCtgrMap 태그 이름과 그에 해당하는 카테고리 목록을 담은 맵
     * @throws Exception 파일 생성 중 발생할 수 있는 예외
     */
    private void writeToFile(final Map<String, List<String>> tagCtgrMap) throws Exception {
        final String FILE_PATH = "templates/view/domain/jrnl/diary/tag/_jrnl_diary_tag_ctgr_map.ftlh";
        final String MAP_NM = "jrnlDiary";

        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            fileWriter.write("<script>\n");
            fileWriter.write("\tif (typeof TagCtgrMap === 'undefined') { var TagCtgrMap = {}; }\n");
            fileWriter.write("\tTagCtgrMap." + MAP_NM + " = (function() {\n");
            fileWriter.write("\t\treturn {\n");

            if (tagCtgrMap.isEmpty()) {
                fileWriter.write("\t\t // tag list is empty. \n");
            } else {
                for (Map.Entry<String, List<String>> entry : tagCtgrMap.entrySet()) {
                    final String tagName = entry.getKey();
                    final List<String> ctgrList = entry.getValue();
                    final String formattedCategories = ctgrList.stream()
                            .map(category -> "\"" + category + "\"")
                            .collect(Collectors.joining(", "));
                    fileWriter.write(String.format("\t\t\t\"%s\": [%s],\n", tagName, formattedCategories));
                }
            }

            fileWriter.write("\t\t}\n");
            fileWriter.write("\t})();\n");
            fileWriter.write("</script>\n");
        } catch (Exception e) {
            log.error("Failed to write tag category map to file", e);
        }
    }
}
