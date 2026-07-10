package io.movetodata.snap.passport.library.service;

import io.movetodata.snap.passport.DTO.UserDTO;
import io.movetodata.snap.passport.library.models.User;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {


    User getUser(String username);

    User getUserById(UUID id);

    List<User> getUsers();

    User saveUser(User user);

    User changePassword(User user, String newPassword);

    UserDTO convertUserIntoDTO(User user) throws IOException;

    String compressProfileImage(String profileImage, float compressionQuality) throws IOException;
}

