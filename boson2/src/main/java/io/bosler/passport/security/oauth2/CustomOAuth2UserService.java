//package io.bosler.passport.security.oauth2;
//
//import io.bosler.passport.exception.OAuth2AuthenticationProcessingException;
//import io.bosler.passport.library.models.AuthProvider;
//import io.bosler.passport.library.models.LoginHistory;
//import io.bosler.passport.library.models.User;
//import io.bosler.passport.library.repository.LoginHistoryRepository;
//import io.bosler.passport.library.repository.UserRepository;
//import io.bosler.passport.library.service.UserService;
//import io.bosler.passport.security.UserPrincipal;
//import io.bosler.passport.security.oauth2.user.OAuth2UserInfo;
//import io.bosler.passport.security.oauth2.user.OAuth2UserInfoFactory;
//import org.checkerframework.checker.units.qual.A;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.InternalAuthenticationServiceException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.Date;
//import java.util.Optional;
//
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    LoginHistoryRepository loginHistoryRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
//
//        try {
//            return processOAuth2User(oAuth2UserRequest, oAuth2User);
//        } catch (AuthenticationException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
//            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
//        }
//    }
//
//    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
//        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
//        if(StringUtils.isEmpty(oAuth2UserInfo.getId())) {
//            throw new OAuth2AuthenticationProcessingException("Name not found from OAuth2 provider");
//        }
//
//        Optional<User> userOptional = userRepository.findByProviderId(oAuth2UserInfo.getId());
//        User user;
//        if(userOptional.isPresent()) {
//            user = userOptional.get();
//            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
//                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
//                        user.getProvider() + " account. Please use your " + user.getProvider() +
//                        " account to login.");
//            }
//            user = updateExistingUser(user, oAuth2UserInfo);
//        } else {
//            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
//        }
//
//        return UserPrincipal.create(user, oAuth2User.getAttributes());
//    }
//
//    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
//        User user = new User();
//
//        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
//        user.setProviderId(oAuth2UserInfo.getId());
//        user.setUsername(oAuth2UserInfo.getUsername());
//
//        if(oAuth2UserInfo.getName() == null) {
//            user.setName(oAuth2UserInfo.getUsername());  // github does not give name
//        } else {
//            user.setName(oAuth2UserInfo.getName());
//        }
//        user.setGivenName(oAuth2UserInfo.getGivenName());
//        user.setFamilyName(oAuth2UserInfo.getFamilyName());
//
//        user.setEmail(oAuth2UserInfo.getEmail());
//        user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
//        user.setSsoAttributes(oAuth2UserInfo.getAttributes());
//        user.setCreatedAt(new Date());
//        user.setLastLoginAt(new Date());
//
//        User savedUser = userRepository.save(user);
//
//        // Login history
//        LoginHistory loginHistory = new LoginHistory();
//
////        loginHistory.setAgent(request.getHeader("User-Agent"));
////
////        String ipAddress = request.getHeader("X-FORWARDED-FOR");
////        if (ipAddress == null) {
////            ipAddress = request.getRemoteAddr();
////        }
//
////        loginHistory.setRemoteAddr(ipAddress);
//
//        loginHistory.setUserId(savedUser.getId());
//        loginHistory.setLastLoginAt(new Date());
//        loginHistory.setLastLoginAt(new Date());
//
//        loginHistoryRepository.save(loginHistory);
//
//        // Login history
//
//        return savedUser;
//    }
//
//    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
//
//        existingUser.setUsername(oAuth2UserInfo.getUsername());
//
//        existingUser.setName(oAuth2UserInfo.getName());
//        existingUser.setGivenName(oAuth2UserInfo.getGivenName());
//        existingUser.setFamilyName(oAuth2UserInfo.getFamilyName());
//
//        existingUser.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
//        existingUser.setSsoAttributes(oAuth2UserInfo.getAttributes());
//
//        existingUser.setUpdatedAt(new Date());
//        existingUser.setLastLoginAt(new Date());
//
//        userRepository.save(existingUser);
//
//
//        // Login history
//        LoginHistory loginHistory = new LoginHistory();
//
//
////        loginHistory.setAgent(request.getHeader("User-Agent"));
////
////        String ipAddress = request.getHeader("X-FORWARDED-FOR");
////        if (ipAddress == null) {
////            ipAddress = request.getRemoteAddr();
////        }
//
////        loginHistory.setRemoteAddr(ipAddress);
//
//        loginHistory.setUserId(existingUser.getId());
//        loginHistory.setLastLoginAt(new Date());
//        loginHistory.setLastLoginAt(new Date());
//
//        loginHistoryRepository.save(loginHistory);
//
//        // Login history
//
//        return existingUser;
//    }
//
//}
