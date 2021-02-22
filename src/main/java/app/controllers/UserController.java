package app.controllers;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<User> updateUserById(@PathVariable("id") final Long id,
                    @RequestBody final User user,
                    @RequestHeader(name="Authorization") String token
                    ) {

        if (jwtUtil.extractUserId(token) == id) {

            return ResponseEntity.ok(userService.saveUser(user));

        } else {
            // unauthorized
            return ResponseEntity.status(403).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") Long id,
    @RequestHeader(name="Authorization") String token) {

        if(jwtUtil.extractUserId(token) == id || userService.isAdmin(token)){
            userService.delete(id);
            return ResponseEntity.ok(null);
        }
        else return ResponseEntity.status(403).body(null);
    }

    @PostMapping("/avatar")
    public void handleFileUpload(@RequestParam("file") final MultipartFile file,
    @RequestHeader(name="Authorization") String token) {

        userService.handleFileUpload(file, token);
    }

}
