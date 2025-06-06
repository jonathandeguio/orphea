package io.bosler.passport.security;

import io.bosler.notifications.library.repository.NotificationRepository;
import io.bosler.passport.library.service.AuthzService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.aopalliance.intercept.MethodInvocation;

public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    private ApplicationContext applicationContext;
    private AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {

        CustomMethodSecurityExpressionRoot root = new CustomMethodSecurityExpressionRoot(authentication);
        root.setTrustResolver(this.trustResolver);
        root.setPermissionEvaluator(getPermissionEvaluator());
        // root.setRoleHierarchy(getRoleHierarchy()); uncomment this one if you defined a RoleHierarchy bean

        //set the UserService in the CustomMethodSecurityExpressionRoot instance to be able to use it
        root.setAuthzService(this.applicationContext.getBean(AuthzService.class));
        root.setNotificationRepository(this.applicationContext.getBean(NotificationRepository.class));
        return root;
    }

    //This setter method will be called from the config class
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        this.applicationContext=applicationContext;
    }
}