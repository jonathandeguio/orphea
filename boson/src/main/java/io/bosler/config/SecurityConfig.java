package io.bosler.config;

import io.bosler.passport.security.CustomUserDetailsService;
import io.bosler.passport.security.RestAuthenticationEntryPoint;
import io.bosler.passport.security.TokenAuthenticationFilter;
import io.bosler.passport.security.oauth2.CustomOAuth2UserService;
import io.bosler.passport.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import io.bosler.passport.security.oauth2.OAuth2AuthenticationFailureHandler;
import io.bosler.passport.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    @Autowired
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


//    @Bean
//    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
//        try (InputStream inputStream = metadataFile.getInputStream()) {
//
//            Map<String, String> xmlProperties = SamlMetaDataParser.parseMetadata(inputStream);
//
//            RelyingPartyRegistration registration = RelyingPartyRegistration
//                    .withRegistrationId("azure-ad-saml")
//                        .entityId("http://localhost:8080")
//                    .assertionConsumerServiceLocation("http://localhost:8080/api/sso/callback")
//                    .assertingPartyDetails(party -> party
//                            .entityId("http://localhost:8080")
//                            .singleSignOnServiceLocation(xmlProperties.get("SingleSignOnServiceLocation"))
//                            .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
//                            .verificationX509Credentials(c -> c.add(SSOUtils.getCertificate()))
//                            .wantAuthnRequestsSigned(false)
//                    )
//                    .build();
//
//            return new InMemoryRelyingPartyRegistrationRepository(registration);
//        } catch (ParserConfigurationException e) {
//            throw new RuntimeException(e);
//        } catch (SAXException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // This will enable the m
        Converter<HttpServletRequest, RelyingPartyRegistration> relyingPartyRegistrationResolver =
                new DefaultRelyingPartyRegistrationResolver(relyingPartyRegistrationRepository);

        Saml2MetadataFilter filter = new Saml2MetadataFilter(
                relyingPartyRegistrationResolver,
                new OpenSamlMetadataResolver());
        // Add SAML2 Login configuration

        http.authorizeRequests()
                .antMatchers("/api/sso/**").permitAll() // Allow access to the callback endpoint
                .and()
                .saml2Login(saml2 -> saml2
                        .successHandler((request, response, authentication) -> {
                            // Log the exception for signature validation failure
                            System.out.println("SAML Validation SUCCESS: ");
                            response.sendRedirect("/api/sso/callback");
                        })
                        .failureHandler((request, response, exception) -> {
                            // Log the exception for signature validation failure
                            System.out.println("SAML Validation Failed: " + exception.getMessage());
                            response.sendRedirect("/api/sso/callback");
                        })
                ) .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Disable session creation
                .and()
                .addFilterBefore(filter, Saml2WebSsoAuthenticationFilter.class)
                .csrf()
                .ignoringAntMatchers("/api/ws/**")
                .and()
                .cors()
                .and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/error",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/resources/**",
//                        "/**",
                        "/", "/error", "/webjars/**",
                        "/api/passport/login/**",
                        "/api/passport/logout/**",
                        "/api/passport/refresh-token",
                        "/api/passport/verify",
                        "/api/passport/recovery",
                        "/api/passport/token/refresh",
                        "/api/passport/sso/available",
                        "/api/connect/agent/download/**",
                        "/api/connect/agent/download/**",
                        "/api/connect/agent/install/**",
                        "/api/connect/agent/install/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",

                        // Get file for dashboard markdown
                        "/api/file/**",

                        // Socket testing
                        "/api/ws/**",
                        "/topic/**",
                        "/app/**",
                        "/send/**",
                        "/sendMessage/**",
                        // Socket testing

                        "/o/oauth2/v2/auth/**",
                        "/oauth2/authorization/google",
                        "/login/oauth2/authorization/google",
                        "/oauth2/authorization/github",
                        "/oauth2/**",
                        "/api/sso/**",
                        "/api/sso/list"
                )
                .permitAll()
                .antMatchers("/auth/**", "/oauth2/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/api/oauth2/authorize")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                .redirectionEndpoint()
                .baseUri("/api/oauth2/callback/*")
                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        // Add our custom Token based authentication filter
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
