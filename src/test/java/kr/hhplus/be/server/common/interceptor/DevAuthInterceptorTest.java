package kr.hhplus.be.server.common.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class DevAuthInterceptorTest {

    private final DevAuthInterceptor interceptor = new DevAuthInterceptor();

    @Test
    @DisplayName("X-USER-ID 헤더가 없으면 401 Unauthorized 반환")
    void should_block_when_user_id_header_missing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Missing X-USER-ID header");
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 있으면 통과")
    void should_pass_when_user_id_present() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-USER-ID", "100");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
    }
}
