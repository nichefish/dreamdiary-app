package io.nicheblog.dreamdiary.global.intrfc.spec;

import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseSpec
 * <pre>
 *  (공통/상속) 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     *
     * @param searchParam 검색 파라미터
     * @return {@link Specification} -- 검색 조건에 맞는 Specification 객체
     */
    default Specification<Entity> searchWith(final BaseSearchParam searchParam) throws Exception {
        Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        return this.searchWith(searchParamMap);
    }

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     * 
     * @param searchParamMap 검색 파라미터 맵
     * @return {@link Specification} -- 검색 조건에 맞는 Specification 객체
     */
    default Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        // exclude filter
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                predicate = getPredicateWithParams(searchParamMap, root, builder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.postQuery(root, query, builder, searchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * default: 인자별로 구체적인 검색 조건을 세팅한다.
     * 
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    default List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        List<Predicate> predicate = new ArrayList<>();
        for (String key : searchParamMap.keySet()) {
            // default :: 조건 파라미터에 대해 equal 검색
            try {
                predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
            } catch (Exception e) {
                e.printStackTrace();
                // log.info("unable to locate attribute " + key + " while trying root.get(key).");
            }
        }
        return predicate;
    }

    /**
     * default: 조회 후처리 (정렬 순서 변경, distinct 등)
     * 
     * @param root 조회 대상 엔티티의 Root 객체
     * @param query CriteriaQuery 객체, 조회 결과에 대한 쿼리 정의
     * @param builder CriteriaBuilder 객체, 조회 조건을 설정하는 데 사용
     * @param searchParamMap 검색 조건을 담은 파라미터 맵
     */
    default void postQuery(
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Map<String, Object> searchParamMap
    ) {
        // 정렬 처리:: 기본 공백, 필요시 각 함수에서 Override
        this.postQuery(root, query, builder);
    }

    /**
     * default: 조회 후처리 (정렬 순서 변경, distinct 등)
     * 
     * @param root 조회 대상 엔티티의 Root 객체
     * @param query CriteriaQuery 객체로, 조회 결과를 정의하는 데 사용됩니다.
     * @param builder CriteriaBuilder 객체로, 쿼리 조건을 설정하는 데 사용됩니다.
     */
    default void postQuery(
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 처리:: 기본 공백, 필요시 각 함수에서 Override
    }

    /**
     * IPv4/CIDR 검색 조건 세팅 :: 메소드 분리
     * 
     * @param key 검색할 컬럼의 이름
     * @param ipStr 검색할 IP 주소 또는 CIDR 형식의 문자열
     * @param root 검색 대상 엔티티의 Root 객체
     * @param builder CriteriaBuilder 객체로, 쿼리 조건을 설정하는 데 사용됩니다.
     * @return {@link Predicate} -- IP 주소에 대한 검색 조건을 나타내는 Predicate 객체
     * @throws Exception 서브넷 계산 중 발생할 수 있는 예외
     */
    private Predicate getIpPredicate(
            final String key,
            final String ipStr,
            final Root<Entity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        boolean isCidr = ipStr.contains("/");
        // ipv4인 경우 : 단순 비교
        if (!isCidr) return builder.like(root.get(key), "%" + ipStr + "%");
        // cidr인 경우 : subnetUtils 사용
        SubnetUtils subnetUtils = new SubnetUtils(ipStr);
        String lowAddress = subnetUtils.getInfo().getLowAddress();
        String mask = ipStr.substring(ipStr.indexOf("/") + 1);
        String partialIp = ipStr;
        if (Integer.parseInt(mask) >= 24)
            partialIp = lowAddress.substring(0, lowAddress.lastIndexOf(".") + 1);
        else if (Integer.parseInt(mask) >= 16)
            partialIp = lowAddress.substring(0, lowAddress.indexOf(".", lowAddress.indexOf(".") + 1) + 1);
        else if (Integer.parseInt(mask) >= 8)
            partialIp = lowAddress.substring(0, lowAddress.indexOf(".") + 1);
        return builder.like(root.get(key), partialIp + "%");
    }
}
