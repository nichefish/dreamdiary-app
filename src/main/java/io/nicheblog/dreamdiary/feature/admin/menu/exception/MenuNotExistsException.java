package io.nicheblog.dreamdiary.feature.admin.menu.exception;

import io.nicheblog.dreamdiary.global.exception.BaseException;
import lombok.experimental.StandardException;

/**
 * MenuNotExistsException
 * <pre>
 *  (공통/상속) 오류에 로깅 필요 값을 담아서 던질 수 있도록 만든 Custom Exception
 * </pre>
 *
 * @author nichefish
 */
@StandardException
public class MenuNotExistsException
        extends BaseException {
}
