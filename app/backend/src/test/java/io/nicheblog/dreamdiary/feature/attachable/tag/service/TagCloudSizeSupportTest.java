package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TagCloudSizeSupportTest
 * <pre>
 *  forceMax → ts-9 고정 계약 단위 테스트.
 * </pre>
 */
class TagCloudSizeSupportTest {

    @Test
    void applyForceMax_pinsToTs9WhenEnabled() {
        assertEquals("ts-9", TagCloudSizeSupport.applyForceMax("ts-1", true));
        assertEquals("ts-9", TagCloudSizeSupport.applyForceMax("ts-5", true));
        assertEquals("ts-9", TagCloudSizeSupport.applyForceMax(null, true));
    }

    @Test
    void applyForceMax_keepsFrequencyClassWhenDisabled() {
        assertEquals("ts-3", TagCloudSizeSupport.applyForceMax("ts-3", false));
        assertEquals("ts-3", TagCloudSizeSupport.applyForceMax("ts-3", null));
        assertEquals("ts-1", TagCloudSizeSupport.applyForceMax(null, false));
    }
}
