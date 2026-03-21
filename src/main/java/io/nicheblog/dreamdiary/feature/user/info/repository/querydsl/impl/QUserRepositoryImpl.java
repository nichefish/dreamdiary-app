/*
package io.nicheblog.dreamdiary.feature.web.user.info.repository.querydsl.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.nicheblog.dreamdiary.feature.user.info.entity.QUserEntity;
import io.nicheblog.dreamdiary.feature.web.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.web.user.info.repository.querydsl.QUserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

*/
/**
 * QUserRepositoryImpl
 * <pre>
 *  사용자 관리 > 사용자(계정) 관리 (querydsl) Repository 인터페이스 구현체.
 * </pre>
 *
 * @author nichefish
 *//*

@RequiredArgsConstructor
public class QUserRepositoryImpl
        implements QUserRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<UserEntity> queryUsersByUserIdTest() {
        final QUserEntity user = QUserEntity.userEntity;
        return jpaQueryFactory.selectFrom(user)
                .where(user.userId.eq("nichefish"))
                .fetch();
    }
}*/
