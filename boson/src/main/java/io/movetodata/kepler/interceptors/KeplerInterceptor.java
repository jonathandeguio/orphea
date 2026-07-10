package io.movetodata.kepler.interceptors;

import io.movetodata.platform.enums.ProductType;
import io.movetodata.platform.library.models.LicenseInfoModel;
import io.movetodata.platform.library.services.PlatformConfigService;
import io.movetodata.sharedutils.Response.ErrorDTO;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@RequiredArgsConstructor
public class KeplerInterceptor implements HandlerInterceptor {

    Set<String> KEPLER_USE_CASE_PRODUCT_TYPES = Set.of(ProductType.DATA_PLATFORM.getName(), ProductType.DATA_VIZ.getName());

    private final PlatformConfigService platformConfigService;
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        LicenseInfoModel licenseInfoModel = platformConfigService.getLicenseInfoModel();

        if(licenseInfoModel == null || !KEPLER_USE_CASE_PRODUCT_TYPES.contains(licenseInfoModel.getProduct())) {
            response.sendError(HttpStatus.PAYMENT_REQUIRED.value(),"SUBSCRIBE TO DATA_PLATFORM OR DATA_VIZ TO USE THIS FEATURE");
            return false;
        }
        return true;
    }
}
