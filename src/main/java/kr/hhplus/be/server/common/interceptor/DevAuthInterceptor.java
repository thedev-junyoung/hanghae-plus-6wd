package kr.hhplus.be.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;


/**
 * {@code DevAuthInterceptor}는 로컬 개발 환경 및 인증 미도입 상황에서,
 * 요청 헤더의 {@code X-USER-ID} 값을 기준으로 최소한의 인증 로직을 수행하는 인터셉터입니다.
 *
 * <p>해당 인터셉터는 Spring Security가 설정되지 않은 경우,
 * 각 요청의 유저 식별을 보장하기 위해 사용됩니다. 주로 MVP, 내부 개발용 API, 테스트 환경 등에서 활용됩니다.
 *
 * <p>요청에 {@code X-USER-ID} 헤더가 존재하지 않거나 빈 값일 경우,
 * 401 Unauthorized 응답을 반환하고 컨트롤러 진입을 차단합니다.
 *
 * <pre>
 * 요청 예시:
 *   GET /api/balance
 *   Header: X-USER-ID: 777
 * </pre>
 *
 */

@Component
public class DevAuthInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-USER-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing X-USER-ID header");
            return false;
        }
        return true;
    }
}
