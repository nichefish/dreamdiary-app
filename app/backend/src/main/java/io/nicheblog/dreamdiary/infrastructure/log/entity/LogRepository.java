package io.nicheblog.dreamdiary.infrastructure.log.entity;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository
        extends BaseStreamRepository<LogEntity, Integer> {
}
