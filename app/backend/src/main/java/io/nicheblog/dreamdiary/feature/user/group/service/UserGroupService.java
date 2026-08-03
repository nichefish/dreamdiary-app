package io.nicheblog.dreamdiary.feature.user.group.service;

import io.nicheblog.dreamdiary.auth.permission.entity.PermissionEntity;
import io.nicheblog.dreamdiary.auth.permission.model.PermissionDto;
import io.nicheblog.dreamdiary.auth.permission.repository.jpa.PermissionRepository;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.feature.user.group.entity.GroupPermissionEntity;
import io.nicheblog.dreamdiary.feature.user.group.entity.UserGroupEntity;
import io.nicheblog.dreamdiary.feature.user.group.entity.UserGroupMemberEntity;
import io.nicheblog.dreamdiary.feature.user.group.model.UserGroupDto;
import io.nicheblog.dreamdiary.feature.user.group.repository.jpa.GroupPermissionRepository;
import io.nicheblog.dreamdiary.feature.user.group.repository.jpa.UserGroupMemberRepository;
import io.nicheblog.dreamdiary.feature.user.group.repository.jpa.UserGroupRepository;
import io.nicheblog.dreamdiary.global.exception.BusinessException;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserGroupService
 * <pre>
 *  사용자 그룹 CRUD, 멤버십, 그룹 권한 부여 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMemberRepository userGroupMemberRepository;
    private final GroupPermissionRepository groupPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserGroupDto> getPage(final String keyword, final Pageable pageable) {
        Specification<UserGroupEntity> spec = (root, query, cb) -> cb.conjunction();
        if (StringUtils.isNotBlank(keyword)) {
            final String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("groupKey")), like),
                    cb.like(cb.lower(root.get("groupName")), like)
            ));
        }
        return userGroupRepository.findAll(spec, pageable).map(this::toListDto);
    }

    @Transactional(readOnly = true)
    public UserGroupDto getDtl(final Integer id) {
        final UserGroupEntity entity = userGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageUtils.getMessage("common.result.not-exists")));
        return toDtlDto(entity);
    }

    @Transactional
    public ServiceResponse regist(final UserGroupDto dto) throws Exception {
        if (userGroupRepository.findByGroupKey(dto.getGroupKey()).isPresent()) {
            throw new BusinessException("Group key already exists: " + dto.getGroupKey());
        }
        final UserGroupEntity entity = UserGroupEntity.builder()
                .groupKey(dto.getGroupKey().trim())
                .groupName(dto.getGroupName().trim())
                .description(StringUtils.trimToNull(dto.getDescription()))
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .useYn("Y".equalsIgnoreCase(dto.getUseYn()) ? "Y" : "N")
                .build();
        final UserGroupEntity saved = userGroupRepository.save(entity);
        this.replacePermissions(saved.getId(), dto.getPermissionKeys());
        this.replaceMembers(saved.getId(), dto.getMemberUsernames());
        this.evictMenuCaches();
        log.info("user group registered: id={}, key={}", saved.getId(), saved.getGroupKey());
        return ServiceResponse.builder().rslt(true).rsltObj(toDtlDto(saved)).build();
    }

    @Transactional
    public ServiceResponse modify(final Integer id, final UserGroupDto dto) throws Exception {
        final UserGroupEntity entity = userGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageUtils.getMessage("common.result.not-exists")));
        if (!entity.getGroupKey().equals(dto.getGroupKey())) {
            final Optional<UserGroupEntity> dup = userGroupRepository.findByGroupKey(dto.getGroupKey());
            if (dup.isPresent() && !dup.get().getId().equals(id)) {
                throw new BusinessException("Group key already exists: " + dto.getGroupKey());
            }
            entity.setGroupKey(dto.getGroupKey().trim());
        }
        entity.setGroupName(dto.getGroupName().trim());
        entity.setDescription(StringUtils.trimToNull(dto.getDescription()));
        if (dto.getSortOrder() != null) entity.setSortOrder(dto.getSortOrder());
        if (dto.getUseYn() != null) entity.setUseYn("Y".equalsIgnoreCase(dto.getUseYn()) ? "Y" : "N");
        userGroupRepository.save(entity);
        this.replacePermissions(id, dto.getPermissionKeys());
        this.replaceMembers(id, dto.getMemberUsernames());
        this.evictMenuCaches();
        log.info("user group modified: id={}, key={}", id, entity.getGroupKey());
        return ServiceResponse.builder().rslt(true).rsltObj(toDtlDto(entity)).build();
    }

    @Transactional
    public ServiceResponse delete(final Integer id) {
        final UserGroupEntity entity = userGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageUtils.getMessage("common.result.not-exists")));
        final List<GroupPermissionEntity> perms = groupPermissionRepository.findByGroupId(id);
        groupPermissionRepository.deleteAll(perms);
        final List<UserGroupMemberEntity> members = userGroupMemberRepository.findByGroupId(id);
        userGroupMemberRepository.deleteAll(members);
        userGroupRepository.delete(entity);
        this.evictMenuCaches();
        log.info("user group deleted: id={}, key={}", id, entity.getGroupKey());
        return ServiceResponse.builder().rslt(true).build();
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> listActivePermissions() {
        return permissionRepository.findByUseYnOrderBySortOrderAscPermKeyAsc("Y").stream()
                .map(p -> PermissionDto.builder()
                        .id(p.getId())
                        .permKey(p.getPermKey())
                        .permName(p.getPermName())
                        .description(p.getDescription())
                        .sortOrder(p.getSortOrder())
                        .useYn(p.getUseYn())
                        .build())
                .collect(Collectors.toList());
    }

    private void replacePermissions(final Integer groupId, final List<String> permissionKeys) {
        final List<GroupPermissionEntity> existing = groupPermissionRepository.findByGroupId(groupId);
        groupPermissionRepository.deleteAll(existing);
        groupPermissionRepository.flush();

        if (CollectionUtils.isEmpty(permissionKeys)) return;
        final Set<String> unique = new LinkedHashSet<>();
        for (final String key : permissionKeys) {
            if (StringUtils.isNotBlank(key)) unique.add(key.trim());
        }
        for (final String key : unique) {
            final PermissionEntity perm = permissionRepository.findByPermKey(key)
                    .orElseThrow(() -> new BusinessException("Unknown permission: " + key));
            groupPermissionRepository.save(GroupPermissionEntity.builder()
                    .groupId(groupId)
                    .permissionId(perm.getId())
                    .build());
        }
    }

    private void replaceMembers(final Integer groupId, final List<String> usernames) {
        final List<UserGroupMemberEntity> existing = userGroupMemberRepository.findByGroupId(groupId);
        userGroupMemberRepository.deleteAll(existing);
        userGroupMemberRepository.flush();

        if (CollectionUtils.isEmpty(usernames)) return;
        final Set<String> unique = new LinkedHashSet<>();
        for (final String u : usernames) {
            if (StringUtils.isNotBlank(u)) unique.add(u.trim());
        }
        for (final String username : unique) {
            final UserEntity user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("Unknown user: " + username));
            userGroupMemberRepository.save(UserGroupMemberEntity.builder()
                    .userId(user.getId())
                    .groupId(groupId)
                    .isPrimaryYn("N")
                    .build());
        }
    }

    private UserGroupDto toListDto(final UserGroupEntity entity) {
        return UserGroupDto.builder()
                .id(entity.getId())
                .groupKey(entity.getGroupKey())
                .groupName(entity.getGroupName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .useYn(entity.getUseYn())
                .memberCount(userGroupMemberRepository.countByGroupId(entity.getId()))
                .build();
    }

    private UserGroupDto toDtlDto(final UserGroupEntity entity) {
        final UserGroupDto dto = toListDto(entity);
        final List<String> permKeys = groupPermissionRepository.findByGroupId(entity.getId()).stream()
                .map(GroupPermissionEntity::getPermission)
                .filter(p -> p != null)
                .map(PermissionEntity::getPermKey)
                .collect(Collectors.toCollection(ArrayList::new));
        dto.setPermissionKeys(permKeys);

        final List<String> members = new ArrayList<>();
        for (final UserGroupMemberEntity m : userGroupMemberRepository.findByGroupId(entity.getId())) {
            userRepository.findById(m.getUserId()).ifPresent(u -> members.add(u.getUsername()));
        }
        dto.setMemberUsernames(members);
        return dto;
    }

    private void evictMenuCaches() {
        EhCacheUtils.clearCache("userMenuList");
        EhCacheUtils.clearCache("mngrMenuList");
        EhCacheUtils.clearCache("userMenuMetaList");
        EhCacheUtils.clearCache("mngrMenuMetaList");
    }
}
