
package kr.hhplus.be.server.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * {@code LoggingFilter}는 모든 HTTP 요청에 대해 기본적인 로깅을 수행하는 서블릿 필터입니다.
 *
 * <p>요청의 HTTP 메서드, URI, 클라이언트 IP를 콘솔에 출력하여,
 * API 접근 내역 및 흐름을 추적하는 데 사용됩니다.
 *
 * <p>실제 운영 환경에서는 로그 파일 또는 APM 연동을 통해 더욱 정교한 추적이 가능하며,
 * 디버깅 또는 API 접근 모니터링 목적의 로컬 테스트에서도 유용합니다.
 *
 * <pre>
 * 출력 예시:
 *   [Request] GET /api/products from 127.0.0.1
 * </pre>
 *
 */
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String clientIp = request.getRemoteAddr();

        System.out.printf("[Request] %s %s from %s%n", method, uri, clientIp);

        chain.doFilter(request, response);
    }
}
