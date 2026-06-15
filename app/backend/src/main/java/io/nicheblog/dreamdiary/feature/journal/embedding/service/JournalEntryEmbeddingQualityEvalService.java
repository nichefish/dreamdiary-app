package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingQualityEvalCaseDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingQualityEvalReportDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingQualityEvalSuiteDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSkippedSampleDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 현재 임베딩 모델의 한국어 의미 유사도를 고정 시드·코퍼스 샘플로 실측합니다.
 *
 * <p>Ollama 임베딩 API가 필요합니다. DB 네이티브 VECTOR 여부와 무관하게
 * 저장된 벡터·쿼리 임베딩 간 코사인 유사도 품질만 검증합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingQualityEvalService {

    private static final String STATUS_EMBEDDED = "EMBEDDED";
    private static final String STATUS_SKIPPED = "SKIPPED";

    private static final double PARAPHRASE_MIN_SIMILARITY = 0.72D;
    private static final double PARAPHRASE_SUITE_PASS_RATE = 0.80D;

    private static final double DISTINCT_MIN_MARGIN = 0.05D;
    private static final double DISTINCT_SUITE_PASS_RATE = 0.80D;

    private static final double CORPUS_SELF_MIN_MARGIN = 0.0D;
    private static final double CORPUS_SUITE_PASS_RATE = 0.70D;

    private static final int CORPUS_SAMPLE_SIZE = 12;
    private static final int CORPUS_NEGATIVE_POOL = 8;
    private static final int SKIPPED_SAMPLE_LIMIT = 20;

    /** 한국어 paraphrase 쌍 — 같은 뜻, 다른 표현 */
    private static final List<String[]> PARAPHRASE_PAIRS = List.of(
            new String[]{"꿈에서 바다를 봤다", "꿈속에서 바다 장면이 나왔다"},
            new String[]{"오늘 회의가 길었다", "오늘 회의가 오래 걸렸다"},
            new String[]{"민수와 점심을 먹었다", "민수님이랑 같이 점심 먹음"},
            new String[]{"불안하고 초조한 하루", "하루 종일 불안했다"},
            new String[]{"조직 변화에 대한 고민", "회사 조직 개편 걱정"},
            new String[]{"어제 꿈이 선명했다", "어젯밤 꿈이 또렷했다"},
            new String[]{"박지연과 대화했다", "지연이랑 이야기 나눴다"},
            new String[]{"스트레스를 많이 받았다", "압박감이 심했다"},
            new String[]{"새 프로젝트 아이디어", "신규 과제 구상"},
            new String[]{"퇴근 후 산책", "퇴근하고 걸었다"}
    );

    /** anchor, related(가까운 뜻), unrelated(먼 뜻) 삼중 */
    private static final List<String[]> DISTINCT_TRIPLETS = List.of(
            new String[]{"동료와 회의에서 의견 충돌", "팀 미팅에서 말다툼", "주말에 본 영화가 재밌었다"},
            new String[]{"꿈에서 누군가 쫓아왔다", "악몽에서 도망치는 장면", "오늘 점심 메뉴 고민"},
            new String[]{"회사 조직 개편 소식", "부서 이동 이야기", "고양이 사료 주문"},
            new String[]{"민수와 프로젝트 논의", "민수님과 업무 회의", "비 오는 날 창밖 풍경"},
            new String[]{"일기에 감정을 적었다", "오늘 기분을 기록했다", "버스 시간표 확인"},
            new String[]{"꿈 해몽이 궁금하다", "꿈 의미를 찾아봤다", "세탁기 돌리는 중"},
            new String[]{"가족과 전화 통화", "부모님께 안부 전화", "새 운동화 구매"},
            new String[]{"업무 우선순위 정리", "할 일 목록 재정렬", "라면 끓이는 법"},
            new String[]{"팀원과 갈등", "동료와 마찰", "날씨가 맑다"},
            new String[]{"Deep work 시간 확보", "집중 업무 블록", "택배 수령"}
    );

    private final JournalEntryEmbeddingRepository repository;
    private final JournalEntryEmbeddingSearchService searchService;
    private final OllamaClient ollamaClient;

    /**
     * 고정 시드 + 코퍼스 샘플로 임베딩 품질 실측을 실행합니다.
     *
     * @return 실측 리포트
     */
    public JournalEntryEmbeddingQualityEvalReportDto runEval() {
        final long start = System.currentTimeMillis();
        final long embeddedCount = repository.countByEmbeddingStatus(STATUS_EMBEDDED);
        final long skippedCount = repository.countByEmbeddingStatus(STATUS_SKIPPED);
        final int cachedVectorCount = searchService.getCachedVectorCount();
        final Integer vectorDimension = resolveVectorDimension();

        final List<JournalEntryEmbeddingSkippedSampleDto> skippedSamples = loadSkippedSamples();
        final List<JournalEntryEmbeddingQualityEvalSuiteDto> suites = new ArrayList<>();

        boolean ollamaAvailable = true;
        try {
            suites.add(runParaphraseSuite());
            suites.add(runDistinctSuite());
            suites.add(runCorpusSelfRankSuite());
        } catch (final OllamaEvalUnavailableException e) {
            ollamaAvailable = false;
            log.warn("Embedding quality eval aborted: {}", e.getMessage());
        }

        final boolean overallPassed = ollamaAvailable && suites.stream().allMatch(JournalEntryEmbeddingQualityEvalSuiteDto::isSuitePassed);
        final String recommendation = resolveRecommendation(ollamaAvailable, suites);
        final String summary = buildSummary(ollamaAvailable, overallPassed, suites, recommendation);

        return JournalEntryEmbeddingQualityEvalReportDto.builder()
                .embeddingModel(ollamaClient.getEmbeddingModel())
                .embeddedCount(embeddedCount)
                .cachedVectorCount(cachedVectorCount)
                .vectorDimension(vectorDimension)
                .skippedCount(skippedCount)
                .skippedSamples(skippedSamples)
                .suites(suites)
                .overallPassed(overallPassed)
                .recommendation(recommendation)
                .summary(summary)
                .elapsedMs(System.currentTimeMillis() - start)
                .build();
    }

    private JournalEntryEmbeddingQualityEvalSuiteDto runParaphraseSuite() {
        final List<JournalEntryEmbeddingQualityEvalCaseDto> cases = new ArrayList<>();
        for (int i = 0; i < PARAPHRASE_PAIRS.size(); i++) {
            final String[] pair = PARAPHRASE_PAIRS.get(i);
            cases.add(evaluateParaphrasePair("paraphrase-" + String.format("%02d", i + 1), pair[0], pair[1]));
        }
        return buildSuiteResult(
                "PARAPHRASE",
                "한국어 paraphrase 쌍의 코사인 유사도",
                "각 쌍 유사도 >= " + PARAPHRASE_MIN_SIMILARITY + ", 통과율 >= " + formatPercent(PARAPHRASE_SUITE_PASS_RATE),
                cases,
                PARAPHRASE_SUITE_PASS_RATE
        );
    }

    private JournalEntryEmbeddingQualityEvalCaseDto evaluateParaphrasePair(
            final String caseId,
            final String left,
            final String right
    ) {
        final double similarity = cosineSimilarity(embed(left), embed(right));
        final boolean passed = similarity >= PARAPHRASE_MIN_SIMILARITY;
        return JournalEntryEmbeddingQualityEvalCaseDto.builder()
                .caseId(caseId)
                .description(left + "  <->  " + right)
                .expectation("유사도 >= " + PARAPHRASE_MIN_SIMILARITY)
                .passed(passed)
                .metric(round4(similarity))
                .detail(passed ? null : "paraphrase 유사도가 기대보다 낮음")
                .build();
    }

    private JournalEntryEmbeddingQualityEvalSuiteDto runDistinctSuite() {
        final List<JournalEntryEmbeddingQualityEvalCaseDto> cases = new ArrayList<>();
        for (int i = 0; i < DISTINCT_TRIPLETS.size(); i++) {
            final String[] triplet = DISTINCT_TRIPLETS.get(i);
            cases.add(evaluateDistinctTriplet("distinct-" + String.format("%02d", i + 1), triplet[0], triplet[1], triplet[2]));
        }
        return buildSuiteResult(
                "RELATED_DISTINCT",
                "관련 문장은 가깝고 무관 문장은 먼지 (related - unrelated 마진)",
                "마진 >= " + DISTINCT_MIN_MARGIN + ", 통과율 >= " + formatPercent(DISTINCT_SUITE_PASS_RATE),
                cases,
                DISTINCT_SUITE_PASS_RATE
        );
    }

    private JournalEntryEmbeddingQualityEvalCaseDto evaluateDistinctTriplet(
            final String caseId,
            final String anchor,
            final String related,
            final String unrelated
    ) {
        final double[] anchorVector = embed(anchor);
        final double relatedSim = cosineSimilarity(anchorVector, embed(related));
        final double unrelatedSim = cosineSimilarity(anchorVector, embed(unrelated));
        final double margin = relatedSim - unrelatedSim;
        final boolean passed = margin >= DISTINCT_MIN_MARGIN;
        return JournalEntryEmbeddingQualityEvalCaseDto.builder()
                .caseId(caseId)
                .description(anchor + " | related=" + related + " | unrelated=" + unrelated)
                .expectation("related - unrelated >= " + DISTINCT_MIN_MARGIN)
                .passed(passed)
                .metric(round4(margin))
                .comparisonMetric(round4(relatedSim))
                .detail(passed ? null : "unrelated=" + round4(unrelatedSim))
                .build();
    }

    private JournalEntryEmbeddingQualityEvalSuiteDto runCorpusSelfRankSuite() {
        final List<Integer> sampleIds = searchService.sampleCachedJournalEntryIds(CORPUS_SAMPLE_SIZE);
        final List<JournalEntryEmbeddingQualityEvalCaseDto> cases = new ArrayList<>();

        if (sampleIds.isEmpty()) {
            cases.add(JournalEntryEmbeddingQualityEvalCaseDto.builder()
                    .caseId("corpus-empty")
                    .description("EMBEDDED 캐시가 비어 있음")
                    .expectation("최소 1건 샘플")
                    .passed(false)
                    .detail("벡터 캐시가 비어 있어 코퍼스 실측을 건너뜀")
                    .build());
            return buildSuiteResult(
                    "CORPUS_SELF_RANK",
                    "실제 저널 embedding_text 요약 쿼리가 자기 벡터보다 타 벡터에 가깝지 않은지",
                    "selfSim > bestOtherSim, 통과율 >= " + formatPercent(CORPUS_SUITE_PASS_RATE),
                    cases,
                    CORPUS_SUITE_PASS_RATE
            );
        }

        for (final Integer journalEntryId : sampleIds) {
            cases.add(evaluateCorpusSelfRank(journalEntryId, sampleIds));
        }

        return buildSuiteResult(
                "CORPUS_SELF_RANK",
                "실제 저널 embedding_text 요약 쿼리가 자기 벡터보다 타 벡터에 가깝지 않은지",
                "selfSim - bestOtherSim >= " + CORPUS_SELF_MIN_MARGIN + ", 통과율 >= " + formatPercent(CORPUS_SUITE_PASS_RATE),
                cases,
                CORPUS_SUITE_PASS_RATE
        );
    }

    private JournalEntryEmbeddingQualityEvalCaseDto evaluateCorpusSelfRank(
            final Integer journalEntryId,
            final List<Integer> samplePool
    ) {
        final Optional<JournalEntryEmbeddingEntity> metaOptional = searchService.getCachedMeta(journalEntryId);
        final Optional<double[]> selfVectorOptional = searchService.getCachedVector(journalEntryId);
        if (metaOptional.isEmpty() || selfVectorOptional.isEmpty()) {
            return JournalEntryEmbeddingQualityEvalCaseDto.builder()
                    .caseId("rank-entry-" + journalEntryId)
                    .description("journalEntryId=" + journalEntryId)
                    .expectation("캐시 hit")
                    .passed(false)
                    .detail("캐시에 벡터 없음")
                    .build();
        }

        final String probe = buildCorpusProbeQuery(metaOptional.get().getEmbeddingText());
        if (StringUtils.isBlank(probe)) {
            return JournalEntryEmbeddingQualityEvalCaseDto.builder()
                    .caseId("rank-entry-" + journalEntryId)
                    .description("journalEntryId=" + journalEntryId)
                    .expectation("프로브 쿼리 생성")
                    .passed(false)
                    .detail("embedding_text에서 본문 추출 실패")
                    .build();
        }

        final double[] queryVector = embed(probe);
        final double selfSim = cosineSimilarity(queryVector, selfVectorOptional.get());
        final double bestOtherSim = findBestOtherSimilarity(queryVector, journalEntryId, samplePool);

        final double margin = selfSim - bestOtherSim;
        final boolean passed = margin > CORPUS_SELF_MIN_MARGIN;
        return JournalEntryEmbeddingQualityEvalCaseDto.builder()
                .caseId("rank-entry-" + journalEntryId)
                .description("probe=" + StringUtils.abbreviate(probe, 80))
                .expectation("selfSim > bestOtherSim")
                .passed(passed)
                .metric(round4(selfSim))
                .comparisonMetric(round4(bestOtherSim))
                .detail(passed ? null : "margin=" + round4(margin))
                .build();
    }

    private double findBestOtherSimilarity(
            final double[] queryVector,
            final Integer selfId,
            final List<Integer> samplePool
    ) {
        final List<Integer> negatives = new ArrayList<>(samplePool);
        negatives.remove(selfId);
        if (negatives.isEmpty()) {
            negatives.addAll(searchService.sampleCachedJournalEntryIds(CORPUS_NEGATIVE_POOL));
            negatives.remove(selfId);
        }
        shuffleForSample(negatives);
        double best = Double.NEGATIVE_INFINITY;
        int checked = 0;
        for (final Integer otherId : negatives) {
            if (otherId.equals(selfId)) continue;
            final Optional<double[]> otherVector = searchService.getCachedVector(otherId);
            if (otherVector.isEmpty()) continue;
            best = Math.max(best, cosineSimilarity(queryVector, otherVector.get()));
            checked++;
            if (checked >= CORPUS_NEGATIVE_POOL) break;
        }
        return best == Double.NEGATIVE_INFINITY ? 0D : best;
    }

    private void shuffleForSample(final List<Integer> ids) {
        for (int i = ids.size() - 1; i > 0; i--) {
            final int j = ThreadLocalRandom.current().nextInt(i + 1);
            final Integer tmp = ids.get(i);
            ids.set(i, ids.get(j));
            ids.set(j, tmp);
        }
    }

    private String buildCorpusProbeQuery(final String embeddingText) {
        if (StringUtils.isBlank(embeddingText)) return "";

        final String body = extractEmbeddingBody(embeddingText);
        if (StringUtils.isNotBlank(body)) {
            return StringUtils.abbreviate(StringUtils.normalizeSpace(body), 120);
        }
        return StringUtils.abbreviate(StringUtils.normalizeSpace(embeddingText), 120);
    }

    private String extractEmbeddingBody(final String embeddingText) {
        final int markerIndex = embeddingText.indexOf("본문:");
        if (markerIndex < 0) return "";
        final String body = embeddingText.substring(markerIndex + "본문:".length()).trim();
        final int nextLabelIndex = body.indexOf("\n유형:");
        if (nextLabelIndex > 0) return body.substring(0, nextLabelIndex).trim();
        return body;
    }

    private JournalEntryEmbeddingQualityEvalSuiteDto buildSuiteResult(
            final String code,
            final String description,
            final String passCriteria,
            final List<JournalEntryEmbeddingQualityEvalCaseDto> cases,
            final double requiredPassRate
    ) {
        final int passedCount = (int) cases.stream().filter(JournalEntryEmbeddingQualityEvalCaseDto::isPassed).count();
        final int failedCount = cases.size() - passedCount;
        final double passRate = cases.isEmpty() ? 0D : (double) passedCount / cases.size();
        return JournalEntryEmbeddingQualityEvalSuiteDto.builder()
                .code(code)
                .description(description)
                .passCriteria(passCriteria)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .suitePassed(passRate >= requiredPassRate)
                .cases(cases)
                .build();
    }

    private List<JournalEntryEmbeddingSkippedSampleDto> loadSkippedSamples() {
        return repository.findAllByEmbeddingStatus(STATUS_SKIPPED).stream()
                .sorted(Comparator.comparing(JournalEntryEmbeddingEntity::getJournalEntryId, Comparator.nullsLast(Integer::compareTo)))
                .limit(SKIPPED_SAMPLE_LIMIT)
                .map(entity -> JournalEntryEmbeddingSkippedSampleDto.builder()
                        .journalEntryId(entity.getJournalEntryId())
                        .errorMessage(StringUtils.abbreviate(StringUtils.defaultString(entity.getErrorMessage()), 240))
                        .build())
                .collect(Collectors.toList());
    }

    private Integer resolveVectorDimension() {
        return searchService.sampleCachedJournalEntryIds(1).stream()
                .flatMap(id -> searchService.getCachedVector(id).stream())
                .map(vector -> vector.length)
                .findFirst()
                .orElse(null);
    }

    private String resolveRecommendation(
            final boolean ollamaAvailable,
            final List<JournalEntryEmbeddingQualityEvalSuiteDto> suites
    ) {
        if (!ollamaAvailable) return "OLLAMA_UNAVAILABLE";
        final boolean allPassed = suites.stream().allMatch(JournalEntryEmbeddingQualityEvalSuiteDto::isSuitePassed);
        if (allPassed) return "KEEP_MODEL";
        final long failedSuites = suites.stream().filter(suite -> !suite.isSuitePassed()).count();
        if (failedSuites >= 2) return "REVIEW_MODEL";
        return "REVIEW_MODEL";
    }

    private String buildSummary(
            final boolean ollamaAvailable,
            final boolean overallPassed,
            final List<JournalEntryEmbeddingQualityEvalSuiteDto> suites,
            final String recommendation
    ) {
        if (!ollamaAvailable) {
            return "Ollama 임베딩 API에 연결하지 못해 실측을 완료하지 못했습니다.";
        }
        if (overallPassed) {
            return "고정 시드·코퍼스 샘플 기준 현재 모델(" + ollamaClient.getEmbeddingModel() + ") 유지 가능.";
        }
        final String failedCodes = suites.stream()
                .filter(suite -> !suite.isSuitePassed())
                .map(JournalEntryEmbeddingQualityEvalSuiteDto::getCode)
                .collect(Collectors.joining(", "));
        return "실패 스위트: " + failedCodes + ". 권고=" + recommendation + " (bge-m3 등 한국어 모델 교체 검토).";
    }

    private double[] embed(final String text) {
        try {
            return toDoubleArray(ollamaClient.embed(text));
        } catch (final Exception e) {
            log.warn("Ollama embed failed during quality eval. textLength={}, error={}",
                    StringUtils.length(text), e.getMessage());
            throw new OllamaEvalUnavailableException("Ollama embedding unavailable: " + e.getMessage(), e);
        }
    }

    private double[] toDoubleArray(final List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private double cosineSimilarity(final double[] a, final double[] b) {
        double dot = 0, normA = 0, normB = 0;
        final int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double round4(final double value) {
        return Math.round(value * 10_000D) / 10_000D;
    }

    private String formatPercent(final double rate) {
        return Math.round(rate * 100) + "%";
    }

    private static final class OllamaEvalUnavailableException extends RuntimeException {
        private OllamaEvalUnavailableException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
