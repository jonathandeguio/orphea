//package io.movetodata.passport.security;
//
//import io.movetodata.passport.exception.ResourceNotFoundException;
//import io.movetodata.passport.library.models.User;
//import io.movetodata.passport.library.repository.UserRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Override
//    @Transactional
//    public UserDetails loadUserByUsername(String username)
//            throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("User not found with username : " + username)
//                );
//
//        return UserPrincipal.create(user);
//    }
//
//    @Transactional
//    public UserDetails loadUserById(UUID id) {
//        User user = userRepository.findById(id).orElseThrow(
//                () -> new ResourceNotFoundException("User", "id", id)
//        );
//
//        return UserPrincipal.create(user);
//    }
//}
