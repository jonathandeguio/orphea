package io.orphea.snap.passport.library.service;

import io.orphea.snap.passport.DTO.UserDTO;
import io.orphea.snap.passport.library.models.User;
import io.orphea.snap.passport.library.repository.UserRepository;
import io.orphea.snap.sharedutils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
        if (user == null) {
            log.error("User not found in the database");
            throw new UsernameNotFoundException(("User not found in the database"));
        } else {
            log.info("User found in the database: {}", username);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
//        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }


    @Override
    //saving users by saving raw passwords
    //public AppUser saveUser(AppUser user) {
    //  log.info("Saving new user {} to the database", user.getName());
    //return userRepo.save(user);
    public User saveUser(User user) {
        log.info("Saving new user {} to the database", user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }


    @Override
    public User getUser(String username) {
//        log.info("Fetching user {} ", username);
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("User not found with username : " + username)
        );
    }

    @Override
    public User getUserById(UUID id) {
//        log.info("Fetching user {} ", username);
        return userRepository.getReferenceById(id);
    }

    @Override
    public List<User> getUsers() {
        log.info("Fetching all users");
        return userRepository.findAllByOrderByNameAsc();
    }



    @Override
    public UserDTO convertUserIntoDTO(User user) throws IOException {
        if(user == null)
            return null;
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFamilyName(user.getFamilyName());
        userDTO.setGivenName(user.getGivenName());
        userDTO.setLocation(user.getLocation());
        userDTO.setLastLoginAt(user.getLastLoginAt());
        userDTO.setProfileImage(compressProfileImage(user.getProfileImage(), 0.5f));
        return userDTO;
    }

    @Override
    public String compressProfileImage(String profileImage, float compressionQuality) throws IOException {
        if (profileImage == null || profileImage.isEmpty()) return null;
        String[] strings = profileImage.split(",");
        if (strings.length < 2)
            return null;

        String extension;
        switch (strings[0]) {//check image's extension
            case "data:image/jpeg;base64":
                extension = "jpeg";
                break;
            case "data:image/png;base64":
                extension = "png";
                break;
            default://should write cases for more images types
                extension = "jpg";
                break;
        }

        byte[] decodedImg = Utils.decodeBase64Tobytes(strings[1]);
        // Read image
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(decodedImg));

        if(extension.equals("png")) {
            BufferedImage jpegImg = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            jpegImg.createGraphics().drawImage( img, 0, 0, Color.BLACK, null);
            img = jpegImg;
            extension = "jpeg";
        }

        // Create output stream for compressed image data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Write image to output stream with compression
        ImageWriteParam imageWriteParam = createJPEGImageWriteParam(compressionQuality);
        ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
        ImageWriter writer = ImageIO.getImageWritersByFormatName(extension).next();
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), imageWriteParam);

        // Get the compressed image data
        byte[] compressedImageData = bos.toByteArray();
        return "data:image/" + extension + ";base64," + Utils.encodeBase64(compressedImageData);
    }

    private static ImageWriteParam createJPEGImageWriteParam(float compressionQuality) {
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(compressionQuality);
        return jpegParams;
    }

}
