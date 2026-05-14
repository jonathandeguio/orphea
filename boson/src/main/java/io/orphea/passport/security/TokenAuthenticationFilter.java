package io.orphea.passport.security;

import io.orphea.passport.util.AuthUtils;
import io.orphea.sharedutils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthenticationFilter.class);
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = AuthUtils.getAccessTokenFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromAccessToken(jwt);

                AuthUser userDetails = customUserDetailsService.loadUserById(userId);

                try {
                    if (userDetails != null && "auto".equals(userDetails.getLanguage())) {
                        List<Utils.LanguageQuality> languageQualityList = Utils.parseAcceptLanguage(request.getHeader("Accept-Language"));
                        userDetails.setLanguage(languageQualityList.get(0).language.split("-")[0]);
                    }
                } catch (Exception ignored) {}

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }
}
