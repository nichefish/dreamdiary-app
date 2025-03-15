package io.nicheblog.dreamdiary.global.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * UUIDUtils
 * <pre>
 *  UUID 관련 유틸리티
 * </pre>
 *
 * @author nichefish
 */
public class UUIDUtils {

	/**
	 * UUID 생성
	 * @return {@link String} -- 생성된 UUID
	 */
	public static String getUUID() {
		final UUID uuid = UUID.randomUUID();
		final String[] tokens = uuid.toString().split("-");
		return tokens[2] + tokens[1] + tokens[0] + tokens[3] + tokens[4];
	}

	/**
	 * 짧은 UUID 생성
	 * @return {@link String} -- 생성된 짧은 UUID
	 */
	public static String getShortUUID() {
		  final UUID uuid = UUID.randomUUID();
		  return Long.toString(ByteBuffer.wrap(uuid.toString().getBytes()).getLong(), Character.MAX_RADIX);
	}
}
