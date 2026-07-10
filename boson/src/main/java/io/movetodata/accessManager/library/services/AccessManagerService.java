package io.movetodata.accessManager.library.services;

import io.movetodata.accessManager.library.enums.AccessRequestStatus;
import io.movetodata.accessManager.library.enums.AccessRequestType;
import io.movetodata.accessManager.library.models.AccessManagerFilters;
import io.movetodata.accessManager.library.models.AccessRequestModel;
import io.movetodata.accessManager.library.models.CloseRequest;
import io.movetodata.accessManager.library.models.RequestAccessModel;
import io.movetodata.accessManager.library.repository.AccessManagerRepository;
import io.movetodata.accessManager.library.specification.AccessManagerSpecification;
import io.movetodata.build.library.models.SocketMessage;
import io.movetodata.kitab.library.services.ResourceService;
import io.movetodata.notifications.library.enums.NotificationType;
import io.movetodata.notifications.library.models.Notification;
import io.movetodata.notifications.library.repository.NotificationRepository;
import io.movetodata.passport.enums.AuthRole;
import io.movetodata.passport.library.models.*;
import io.movetodata.passport.library.repository.GroupsRepository;
import io.movetodata.passport.library.repository.PermissionMappingRepository;
import io.movetodata.passport.library.repository.RoleRepository;
import io.movetodata.passport.library.repository.UserRepository;
import io.movetodata.passport.library.service.GroupService;
import io.movetodata.passport.library.service.GroupServiceImpl;
import io.movetodata.passport.library.service.NotificationPreferencesService;
import io.movetodata.passport.library.service.PermissionMappingService;
import io.movetodata.scheduler.library.services.MailService;
import io.movetodata.sharedutils.models.PageSettings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccessManagerService {
    private final AccessManagerRepository accessManagerRepository;
    private final GroupsRepository groupsRepository;
    private final PermissionMappingRepository permissionMappingRepository;
    private final UserRepository userRepository;
    private final ResourceService resourceService;
    private final NotificationRepository notificationRepository;
    private final GroupService groupService;
    private final MailService mailService;
    private final NotificationPreferencesService notificationPreferencesService;

    @Autowired
    SimpMessagingTemplate template;
    @Autowired
    private GroupServiceImpl groupServiceImpl;
    @Autowired
    private PermissionMappingService permissionMappingService;
    private final RoleRepository roleRepository;

    public Page<AccessRequestModel> getAccessRequestPage(@NonNull PageSettings pageSetting,
                                                         AccessManagerFilters filterCriteria,
                                                         UUID userId) {
        Sort accessRequestSort = pageSetting.buildSort();
        Pageable accessRequestPage = PageRequest.of(pageSetting.getPage(), pageSetting.getElementPerPage(), accessRequestSort);

        Specification<AccessRequestModel> spec = Specification.where(null);

        // Add conditions based on the filter criteria
        if (filterCriteria.getSearchText() != null && !filterCriteria.getSearchText().isEmpty()) {
            spec = spec.and(AccessManagerSpecification.partialSearchOnId(filterCriteria.getSearchText()));
        }

        if (filterCriteria.getStatus() != null && !filterCriteria.getStatus().isEmpty()) {
            spec = spec.and(AccessManagerSpecification.hasStatus(filterCriteria.getStatus()));
        }

        if (filterCriteria.getRangeFrom() != null) {
            spec = spec.and(AccessManagerSpecification.isInRangeFrom(filterCriteria.getRangeFrom()));
        }

        if (filterCriteria.getRangeTo() != null) {
            spec = spec.and(AccessManagerSpecification.isInRangeTo(filterCriteria.getRangeTo()));
        }

        if (filterCriteria.getType() != null) {
            spec = spec.and(AccessManagerSpecification.typeEquals(filterCriteria.getType()));
        }

        if (filterCriteria.getRequesters() != null && !filterCriteria.getRequesters().isEmpty()) {
            spec = spec.and(AccessManagerSpecification.hasRequester(filterCriteria.getRequesters()));
        }

        if (filterCriteria.isShowMyRequestsOnly()) {
            spec = spec.and(AccessManagerSpecification.isMyRequestOnly(userId));
        } else {
            spec = spec.and(AccessManagerSpecification.isAssignedToMe(userId));
        }

        return accessManagerRepository.findAll(spec, accessRequestPage);

    }

    private Set<UUID> getAllUserIdsFromGroup(Groups group) {
        List<UUID> owners = group.getOwners().stream().map(User::getId).collect(Collectors.toList());
        List<UUID> managers = group.getManagers().stream().map(User::getId).collect(Collectors.toList());
        List<UUID> members = group.getMembers().stream().map(User::getId).collect(Collectors.toList());
        return Stream.of(owners, managers, members).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<UUID> getOwnersForAccessRequestTarget(UUID requestTargetId, AccessRequestType type) {
        switch (type) {
            case ADMINISTRATOR:
                return groupsRepository.getReferenceById(requestTargetId).getOwners().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());
            case PROJECT:
                List<PermissionsMapping> permissionsMappings = permissionMappingRepository.
                        findByResourceId(requestTargetId);
                Set<UUID> assignees = new HashSet<>();
                permissionsMappings.forEach((permissionMapping) -> {
                    if (permissionMapping.getRole().getName().equals(AuthRole.OWNER.getDisplayName())) {
                        if (groupsRepository.findById(permissionMapping.getIdentityId()).isPresent()) {
                            Groups group = groupsRepository.getReferenceById(permissionMapping.getIdentityId());
                            assignees.addAll(getAllUserIdsFromGroup(group));
                        } else assignees.add(permissionMapping.getIdentityId());
                    }
                });
                return assignees;
            default:
                return new HashSet<>();

        }
    }

    public String getRequestTargetNameBasedOnType(UUID requestTargetId, AccessRequestType type) {
        switch (type) {
            case ADMINISTRATOR:
                return groupsRepository.getReferenceById(requestTargetId).getName();
            case PROJECT:
                return resourceService.getResourceModel(requestTargetId).getName();
            default:
                return "";

        }
    }

    public AccessRequestModel createAccessRequest(RequestAccessModel requestAccessModel, UUID userId) {
        List<UUID> assignees = new ArrayList<>(getOwnersForAccessRequestTarget(requestAccessModel.getRequestTargetId(),
                requestAccessModel.getType()));
        AccessRequestModel accessRequestModel = AccessRequestModel.builder()
                .requestTargetId(requestAccessModel.getRequestTargetId())
                .requestTargetName(getRequestTargetNameBasedOnType(requestAccessModel.getRequestTargetId(),
                        requestAccessModel.getType()))
                .title(requestAccessModel.getTitle())
                .description(requestAccessModel.getDescription())
                .requesters(requestAccessModel.getRequesters())
                .status(AccessRequestStatus.OPEN)
                .type(requestAccessModel.getType())
                .assignees(assignees)
                .role(requestAccessModel.getRole())
                .createdBy(userId)
                .createdAt(new Date())
                .build();
        accessManagerRepository.save(accessRequestModel);
        List<UUID> subscribers = accessRequestModel.getAssignees().stream()
                .filter((assignee) -> !assignee.equals(userId))
                .collect(Collectors.toList());
        subscribers.forEach((subscriber) -> triggerCommunication(accessRequestModel, userId, subscriber));
        return accessRequestModel;
    }

    public void dispatchCommunication(AccessRequestModel accessRequestModel, UUID influencer, UUID subscriber) {
        // Create and save notification
        Notification notification = new Notification();
        notification.setMessage(accessRequestModel.getTitle());
        notification.setInfluencer(influencer);
        notification.setSubscriber(subscriber);
        notification.setType(NotificationType.ACCESS_REQUEST);
        notification.setResourceId(accessRequestModel.getId());
        notification.setTimestamp(new Date());

        notificationRepository.save(notification);

        // Send socket message to the user
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", notification.getId());
        jsonObject.put("message", accessRequestModel.getTitle());
        jsonObject.put("influencer", influencer);
        jsonObject.put("subscriber", subscriber);
        jsonObject.put("type", NotificationType.ACCESS_REQUEST);
        jsonObject.put("resourceId", accessRequestModel.getId());

        SocketMessage socketMessage = new SocketMessage();
        socketMessage.setMessage(jsonObject.toString());
        template.convertAndSend("/topic/notification/" + subscriber, socketMessage);

        NotificationPreferences notificationPreferences = notificationPreferencesService.
                getNotificationsPreferences(subscriber);

        if(notificationPreferences.getAccessManager()) {
            // Send mail
            String link = System.getenv("BASE_URL") + "/portal/accessManager/" + accessRequestModel.getId();
            User userDetails = userRepository.findById(subscriber).orElse(null);
            try {
                mailService.sendMail(userDetails.getEmail(), accessRequestModel.getTitle(), accessRequestModel.getDescription(), userDetails.getName(), link);
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void triggerCommunication(AccessRequestModel accessRequestModel, UUID influencer, UUID subscriber) {
        Thread backgroundThread = new Thread(() -> {
            // Function to be run in the background
            try {
                dispatchCommunication(accessRequestModel, influencer, subscriber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        backgroundThread.start();
    }

    public AccessRequestModel getAccessRequest(UUID requestId) {
        return accessManagerRepository.getReferenceById(requestId);
    }

    public UUID getRoleIdUsingName(AuthRole authRole) {
        log.info("What was the role:{}", authRole.getDisplayName());
        List<Role> roles = roleRepository.getByName(authRole.getDisplayName());
        if (roles == null || roles.isEmpty()) {
            throw new NoSuchElementException("No role found with the provided name.");
        }
        log.info("Got the role:{}", roles.get(0));
        return roles.get(0).getId();
    }

    public void dispatchGrantingAccess(AccessRequestModel accessRequestModel, UUID ownerId) {
        List<UUID> requesters = accessRequestModel.getRequesters();
        UUID requestTargetId = accessRequestModel.getRequestTargetId();
        switch (accessRequestModel.getType()) {
            case ADMINISTRATOR:
                switch (accessRequestModel.getRole()) {
                    case VIEWER:
                        groupService.addUsersToGroupMembers(ownerId, requesters, requestTargetId);
                        return;
                    case EDITOR:
                        groupService.addUsersToGroupManagers(ownerId, requesters, requestTargetId);
                        return;
                    case OWNER:
                        groupService.addUsersToGroupOwners(ownerId, requesters, requestTargetId);
                        return;
                }
                return;
            case PROJECT:
                permissionMappingService.handleCreatePermissionMapping(ownerId, requesters, requestTargetId,
                        getRoleIdUsingName(accessRequestModel.getRole()));
        }
    }

    public AccessRequestModel closeAccessRequest(UUID userId, CloseRequest closeRequest) {
        AccessRequestModel accessRequestModel = getAccessRequest(closeRequest.getRequestId());
        accessRequestModel.setClosedBy(userId);
        accessRequestModel.setClosedAt(new Date());
        accessRequestModel.setStatus(closeRequest.getStatus());
        accessRequestModel.setClosingRemarks(closeRequest.getClosingRemarks());

        // Initiating Granting Permissions
        if(closeRequest.getStatus().equals(AccessRequestStatus.ACCEPTED)) {
            log.info("Request to gain access was accepted: {}", closeRequest.getRequestId());
            log.info("Request had: {} : {}", accessRequestModel.getRequestTargetName(), accessRequestModel.getRequestTargetName());
            dispatchGrantingAccess(accessRequestModel, userId);
        }
        return accessManagerRepository.save(accessRequestModel);
    }
}
