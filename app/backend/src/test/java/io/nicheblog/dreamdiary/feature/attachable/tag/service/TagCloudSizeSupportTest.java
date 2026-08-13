package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import io.nicheblog.dreamdiary.global.type.CloudSizeLock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TagCloudSizeSupportTest
 * <pre>
 *  CloudSizeLock → 크기 클래스 고정 계약 단위 테스트.
 *  MAX=ts-9, MIN=ts-1, AUTO=빈도 산출 유지(빈 값은 ts-1 보정).
 * </pre>
 */
class TagCloudSizeSupportTest {

    @Test
    void applyCloudSizeLock_pinsToMaxWhenMax() {
        assertEquals("ts-9", TagCloudSizeSupport.applyCloudSizeLock("ts-1", CloudSizeLock.MAX));
        assertEquals("ts-9", TagCloudSizeSupport.applyCloudSizeLock("ts-5", CloudSizeLock.MAX));
        assertEquals("ts-9", TagCloudSizeSupport.applyCloudSizeLock(null, CloudSizeLock.MAX));
    }

    @Test
    void applyCloudSizeLock_pinsToMinWhenMin() {
        assertEquals("ts-1", TagCloudSizeSupport.applyCloudSizeLock("ts-9", CloudSizeLock.MIN));
        assertEquals("ts-1", TagCloudSizeSupport.applyCloudSizeLock("ts-5", CloudSizeLock.MIN));
        assertEquals("ts-1", TagCloudSizeSupport.applyCloudSizeLock(null, CloudSizeLock.MIN));
    }

    @Test
    void applyCloudSizeLock_keepsFrequencyClassWhenAuto() {
        assertEquals("ts-3", TagCloudSizeSupport.applyCloudSizeLock("ts-3", CloudSizeLock.AUTO));
        assertEquals("ts-3", TagCloudSizeSupport.applyCloudSizeLock("ts-3", null));
        assertEquals("ts-1", TagCloudSizeSupport.applyCloudSizeLock(null, CloudSizeLock.AUTO));
    }
}
