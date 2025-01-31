package io.nicheblog.dreamdiary.global;

import lombok.RequiredArgsConstructor;

/**
 * DatabaseVendor
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum DatabaseVendor {
	MYSQL("MySQL", "mysql"),
	MARIADB("MariaDB", "mysql"),
	POSTGRESQL("PostgreSQL", "postgres"),
	ORACLE("Oracle", "oracle"),
	TIBERO("Tibero", "oracle"),
	MSSQL("Microsoft SQL Server", "sqlserver");

	public final String name;
	public final String vendor;

	/* ------ */

	/**
	 * DatabaseProductName을 받아서 vendor 문자열을 반환한다.
	 *
	 * @param databaseProductName String
	 * @return {@link String} vendor 문자열 (일치하는 것이 없으면 빈 문자열 반환)
	 */
	public static String get(final String databaseProductName) {
		for (final DatabaseVendor vendor : DatabaseVendor.values()) {
			if (vendor.name.equalsIgnoreCase(databaseProductName)) {
				return vendor.vendor;
			}
		}
		return "";
	}
}