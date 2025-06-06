package io.bosler.fractal.interceptors;

import io.bosler.platform.enums.ProductType;
import io.bosler.platform.library.models.LicenseInfoModel;
import io.bosler.platform.library.services.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@RequiredArgsConstructor
public class FractalInterceptor implements HandlerInterceptor {

    Set<String> FRACTAL_USE_CASE_PRODUCT_TYPES = Set.of(ProductType.DATA_PLATFORM.getName(), ProductType.DATA_HUB.getName());

    private final PlatformConfigService platformConfigService;
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        LicenseInfoModel licenseInfoModel = platformConfigService.getLicenseInfoModel();

        if(licenseInfoModel == null || !FRACTAL_USE_CASE_PRODUCT_TYPES.contains(licenseInfoModel.getProduct())) {
            response.sendError(HttpStatus.PAYMENT_REQUIRED.value(),"SUBSCRIBE TO DATA_PLATFORM OR DATA_HUB TO USE THIS FEATURE");
            return false;
        }
        return true;
    }
}
