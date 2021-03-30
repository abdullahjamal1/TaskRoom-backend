package app.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.models.collections.User;
import app.repositories.UserRepository;
import app.services.UserService;
import app.util.JwtUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @GetMapping("")
    public Flux<List<String>> getAllUsers() {

        return userService.findAll();
    }

    @GetMapping("/{username}")
    public Mono<User> getUserById(@PathVariable("username") final String username) {

        return userRepository.findByUsername(username);
    }

    @PutMapping("/{username}")
    public ResponseEntity<Mono<User>> updateUserById(@PathVariable("username") final String username,
                    @RequestBody final User user,
                    @RequestHeader(name="Authorization") String token
                    ) {

        if (jwtUtil.extractUsername(token) == username) {

            return ResponseEntity.ok(userService.saveUser(user));

        } else {
            // unauthorized
            return ResponseEntity.status(403).body(null);
        }
    }

    @DeleteMapping("/{username}")
    public Mono<Object> delete(@PathVariable("username") String username,
    @RequestHeader(name="Authorization") String token) {

        return userService.isAdmin(token).map(isAdmin ->{

            if(jwtUtil.extractUsername(token) == username || isAdmin){
                userService.delete(username);
                return ResponseEntity.ok(null);
            }
            else return ResponseEntity.status(403).body(null);
        });
    }


}
