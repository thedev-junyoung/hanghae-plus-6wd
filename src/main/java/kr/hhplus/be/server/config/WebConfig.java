package kr.hhplus.be.server.config;

import kr.hhplus.be.server.common.interceptor.DevAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DevAuthInterceptor devAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(devAuthInterceptor)
                .addPathPatterns("/api/**");
    }
}
