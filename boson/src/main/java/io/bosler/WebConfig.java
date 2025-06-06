package io.bosler;

import io.bosler.config.LogFilter;
import io.bosler.fractal.interceptors.FractalInterceptor;
import io.bosler.kepler.interceptors.KeplerInterceptor;
import io.bosler.platform.interceptor.BaseInterceptor;
import io.bosler.platform.library.services.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final List<String> LICENSE_EXCLUDE_PATH_PATTERNS = Arrays.asList(
            "/api/ping",
            "/api/passport/login",
            "/error",
            "/api/passport/users/isPlatformAdministrator",
            "/api/passport/users/me",
            "/api/platform/config",
            "/api/platform/generateLicenseKey",
            "/api/platform/decryptLicenseKey"
    );

    private final String KEPLER_PATH_PATTERN = "/api/kepler/**";

    private final String FRACTAL_PATH_PATTERN = "/api/fractal/**";

    private final PlatformConfigService platformConfigService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new BaseInterceptor(platformConfigService))
                .excludePathPatterns(LICENSE_EXCLUDE_PATH_PATTERNS).order(1);
        registry.addInterceptor(new KeplerInterceptor(platformConfigService))
                .addPathPatterns(KEPLER_PATH_PATTERN);
        registry.addInterceptor(new FractalInterceptor(platformConfigService))
                .addPathPatterns(FRACTAL_PATH_PATTERN);
    }

    @Bean
    public FilterRegistrationBean<LogFilter> requestIdFilter() {
        FilterRegistrationBean<LogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogFilter());
        registrationBean.addUrlPatterns("/*"); // Apply the filter to all URLs
        return registrationBean;
    }
}
