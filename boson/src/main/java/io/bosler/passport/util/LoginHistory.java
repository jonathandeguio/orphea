//package io.bosler.passport.util;
//
//import io.bosler.passport.library.models.User;
//import io.bosler.passport.library.repository.UserRepository;
//import io.bosler.passport.library.service.UserService;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Date;
//
//@RequiredArgsConstructor
//public class LoginHistory {
//
//
//    public void update(UserService userService, UserRepository userRepository, String username) {
//
//        LoginHistory loginHistory = new LoginHistory();
//        User userId = userService.getUser(username);
//
//        userId.setLastLoginAt(new Date());
//
//        userRepository.save(userId);
//
//        loginHistory.setAgent(request.getHeader("User-Agent"));
//
//        String ipAddress = request.getHeader("X-FORWARDED-FOR");
//        if (ipAddress == null) {
//            ipAddress = request.getRemoteAddr();
//        }
//
//        loginHistory.setRemoteAddr(ipAddress);
//
//        loginHistory.setUserId(userId.getId());
//        loginHistory.setLastLoginAt(new Date());
//
//        loginHistoryRepository.save(loginHistory);
//    }
//
//
//}
