package io.movetodata.passport.security;

import io.movetodata.notifications.library.models.Notification;
import io.movetodata.notifications.library.repository.NotificationRepository;
import io.movetodata.passport.enums.AuthRole;
import io.movetodata.passport.library.service.AuthzService;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {
    private AuthzService authzService;
    private NotificationRepository notificationRepository;
    private HttpServletRequest request;
    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }
    /**
     checks if the authenticated user have access to the user detail with the id
     Only a user have access to his own user details or an admin
     */
    public boolean hasAccess(UUID resourceId, AuthRole role) {
        if(Objects.isNull(resourceId)) return false;

        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        UUID userId = authUser.getId();

        switch (role) {
            case OWNER:
                return authzService.isOwner(userId, resourceId);
            case EDITOR:
                return authzService.isEditor(userId, resourceId);
            case VIEWER:
                return authzService.isViewer(userId, resourceId);
            default:
                return false;
        }
    }

    public boolean isPlatformAdmin() {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isPlatformAdmin(authUser.getId());
    }

    public boolean isUserAdmin() {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isUserAdmin(authUser.getId());
    }

    public boolean isProjectAdmin() {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isProjectAdmin(authUser.getId());
    }

    public boolean isOwner(UUID resourceId) {
        if(Objects.isNull(resourceId)) return false;
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isOwner(authUser.getId(), resourceId);
    }

    public boolean isEditor(UUID resourceId) {
        if(Objects.isNull(resourceId)) return false;
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isEditor(authUser.getId(), resourceId);
    }

    public boolean isViewer(UUID resourceId) {
        if(Objects.isNull(resourceId)) return false;
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isViewer(authUser.getId(), resourceId);
    }

    public boolean isConnectAdmin() {
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authzService.isConnectAdmin(authUser.getId());
    }

    public boolean isSubscriberOrPlatformAdmin(UUID notificationId){
        if(Objects.isNull(notificationId)) return false;
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        if(!notificationRepository.existsById(notificationId))
            return false;
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        return authzService.isPlatformAdmin(authUser.getId()) || notification.getSubscriber().equals(authUser.getId());
    }

    //We need this setter method to set the UserService from another class because this one dosen't have access to Application Context.
    public void setAuthzService(AuthzService authzService){
        this.authzService = authzService;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return target;
    }

    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
}