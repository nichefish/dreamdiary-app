package io.nicheblog.dreamdiary.auth.permission.service;

import io.nicheblog.dreamdiary.auth.permission.repository.jpa.PermissionRepository;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.group.entity.UserGroupMemberEntity;
import io.nicheblog.dreamdiary.feature.user.group.repository.jpa.UserGroupMemberRepository;
import io.nicheblog.dreamdiary.feature.user.group.repository.jpa.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PermissionResolveService
 * <pre>
 *  사용자의 유효 권한 = 시스템 롤 permission ∪ 소속 그룹 permission (합집합).
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class PermissionResolveService {

    private final PermissionRepository permissionRepository;
    private final UserGroupMemberRepository userGroupMemberRepository;
    private final UserGroupRepository userGroupRepository;

    /**
     * 사용자 엔티티 기준 유효 permission key 목록을 합집합으로 반환한다.
     *
     * @param user 사용자 엔티티 (userRoles 로드 권장)
     * @return 정렬된 권한 키 목록 (중복 없음)
     */
    @Transactional(readOnly = true)
    public List<String> resolvePermKeys(final UserEntity user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        final Set<String> keys = new LinkedHashSet<>();

        final List<Integer> roleIds = extractRoleIds(user);
        if (CollectionUtils.isNotEmpty(roleIds)) {
            keys.addAll(permissionRepository.findPermKeysByRoleIds(roleIds));
        }

        final List<UserGroupMemberEntity> memberships = userGroupMemberRepository.findByUserId(user.getId());
        if (CollectionUtils.isNotEmpty(memberships)) {
            final List<Integer> groupIds = memberships.stream()
                    .map(UserGroupMemberEntity::getGroupId)
                    .filter(id -> id != null)
                    .filter(id -> userGroupRepository.findById(id)
                            .map(g -> "Y".equals(g.getUseYn()))
                            .orElse(false))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                keys.addAll(permissionRepository.findPermKeysByGroupIds(groupIds));
            }
        }

        log.debug("resolved permissions for userId={}: count={}", user.getId(), keys.size());
        return new ArrayList<>(keys);
    }

    private List<Integer> extractRoleIds(final UserEntity user) {
        if (CollectionUtils.isEmpty(user.getUserRoles())) {
            return List.of();
        }
        final List<Integer> roleIds = new ArrayList<>();
        for (final UserRoleEntity ur : user.getUserRoles()) {
            if (ur.getRoleId() != null) {
                roleIds.add(ur.getRoleId());
            } else if (ur.getRoleInfo() != null && ur.getRoleInfo().getId() != null) {
                roleIds.add(ur.getRoleInfo().getId());
            }
        }
        return roleIds;
    }
}
