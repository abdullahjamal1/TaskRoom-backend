package app.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.models.entity.User;
import app.models.projections.UserListResponse;
import app.models.projections.UserResponse;
import app.repositories.UserRepository;
import app.services.UserService;
import app.util.JwtUtil;

/*
 *  GET  /
 *  GET  /user/edit/{id}    ----> now PUT
 *  GET  /user/list
 *  GET  /user/delete/{id}  -----> now DELETE
 *  GET  /user/autologin
 *  POST  /user/edit  
 * 	POST  /user/upload
 *  GET  /user/profile-picture
 * 
 */

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    
    // validator
    @GetMapping("/username")
    public boolean isUserNameUnique(@RequestParam("username") String username){
        if(userRepository.findOneByEmail(username) != null)
            return false;
        else return true;
    }

    @GetMapping("")
    public Iterable<UserListResponse> list() {

        return userRepository.findAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") final Long id) {

        return userRepository.findOneById(id);
    }

    @PutMapping("/{id}")
    public User updateUserById(@PathVariable("id") final Long id,
                    @RequestBody final User user,
                    @RequestHeader(name="Authorization") String token
                    ) {

        if (jwtUtil.extractUserId(token) == id) {

            return userService.saveUser(user);

        } else {
            // unauthorized
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id,
    @RequestHeader(name="Authorization") String token) {

        if(jwtUtil.extractUserId(token) == id)
            userService.delete(id);
    }

    @PostMapping("/avatar")
    public String handleFileUpload(@RequestParam("file") final MultipartFile file,
    @RequestHeader(name="Authorization") String token) {

        return userService.handleFileUpload(file, token);

    }

    @GetMapping(value = "{user_id}/avatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] profilePicture(@PathVariable("user_id") Long user_id) throws IOException {

        return userService.handleFileSend(user_id);

    }
}
