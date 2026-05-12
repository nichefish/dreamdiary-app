package io.nicheblog.dreamdiary.auth.security.filter;

/**
 * XssFilter
 * <pre>
 *  Xss 방지 위해 request 전처리 (escaping)
 *  TODO: XssRequestWrapper 보완 후 주석 해제
 * </pre>
 *
 * @author nichefish
 */
// @Component
// public class XssFilter implements Filter {
//
//     public FilterConfig filterConfig;
//
//     @Override
//     public void init(FilterConfig filterConfig) throws ServletException {
//         this.filterConfig = filterConfig;
//     }
//
//     /** Request에 XssRequestWrappper 적용 */
//     @Override
//     public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
//         chain.doFilter(new XssRequestWrapper((HttpServletRequest) req), res);
//     }
//
//     @Override
//     public void destroy() {
//         this.filterConfig = null;
//     }
// }
