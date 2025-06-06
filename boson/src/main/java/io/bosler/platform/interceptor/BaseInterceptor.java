package io.bosler.platform.interceptor;

import io.bosler.platform.library.models.LicenseInfoModel;
import io.bosler.platform.library.services.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
public class BaseInterceptor implements HandlerInterceptor {

    private final PlatformConfigService platformConfigService;

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {
        LicenseInfoModel licenseInfo = platformConfigService.getLicenseInfoModel();

        if(licenseInfo != null) {
            if (new Date().compareTo(licenseInfo.getExpiresOn()) > 0) {
                response.sendError(HttpStatus.PAYMENT_REQUIRED.value(), "PLATFORM EXPIRED!!!");
                return false;
            }
        }

        final String PRODUCTION = "production";
        final String ENVIRONMENT = "Environment";
        final String SOURCE = "Source";
//        if (request.getHeader(ENVIRONMENT).equals(PRODUCTION)) {
//            final String REQUEST_URL = request.getHeader(SOURCE);
//            final String[] ALLOWED_URLS = licenseInfo.getBaseUrl().split(";");
//            if(Arrays.stream(ALLOWED_URLS).noneMatch(REQUEST_URL::equals)) {
//                response.sendError(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS.value(), "YOU ARE USING A LICENSE KEY NOT MEANT FOR YOU!!!");
//                return false;
//            }
//        }
        return true;
    }
}
