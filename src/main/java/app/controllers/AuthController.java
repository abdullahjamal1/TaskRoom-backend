package app.controllers;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.configs.ApplicationConfig;
import app.models.entity.AuthenticationRequest;
import app.models.entity.AuthenticationResponse;
import app.models.entity.RegisterationRequest;
import app.models.collections.User;
import app.repositories.UserRepository;
import app.services.MailService;
import app.services.UserService;
import app.util.JwtUtil;

/*
 * 
 * GET  /login
 * GET  /register
 * POST /register  ???????????
 * GET  /reset-password
 * POST /reset-password
 * GET  /reset-password-change
 * GET  /activation-send
 * POST /activation-send
 * GET  /activate
 * 
 */

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;
    
    @Autowired
	private AuthenticationManager authenticationManager;
    
	@Autowired
	private JwtUtil jwtUtil;
    
	@Autowired
	private UserService userDetailsService;

    @Autowired
    private ApplicationConfig config;
    
    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
			);
		}
		catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}


		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String jwt = jwtUtil.generateToken(userDetails);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

    // try catch dosnt work properly
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterationRequest user) {

        User registeredUser = new User(user);
        registeredUser = userService.register(registeredUser);

        if (registeredUser != null) {

            try{

                mailService.sendNewRegistration(user.getEmail(), registeredUser.getToken());
            }
            catch(Error er) {

                userService.delete(registeredUser.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(er.toString());
            }

            return ResponseEntity.ok("activation mail sent successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user already registered");
    }

    @PostMapping("/reset-password")
    // type email to receive input
    public ResponseEntity<String> resetPasswordEmail(@RequestParam String email) {

        final User user = userRepository.findOneByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found");

        } else {

            final String resetToken = userService.createActivationToken(user, true);
            mailService.sendResetPassword(user.getEmail(), user.getName(), resetToken);
        }
        return ResponseEntity.ok("password-reset link sent successfully to " + email);
    }
    
    // post new password
    @PostMapping("/reset-password-change")
    public ResponseEntity<String> resetPassword(@RequestParam String password,
    @RequestParam String token) {

        User user = userService.activate(token);

        if(user == null){

            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("activation code not valid");
        }

        user.setPassword(password);
        userService.resetPassword(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(config.getFrontendUrl() + "/games"));
        // to redirect user
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);

    }

    @GetMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String activation) {

        final User u = userService.activate(activation);
 
        if (u != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(config.getFrontendUrl() + "/games"));

            // to redirect user
            return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("activation token is not valid");
    } 

    // ==============================================================================================
    
    // for re-sending verification mail
    @PostMapping("/resend-activation")
    public ResponseEntity<Object> activationSendPost(@RequestParam String email)
    {
        final User u = userService.resetActivation(email);

        if (u != null){

        mailService.sendNewActivationRequest(u.getEmail(), u.getToken());

        return ResponseEntity.ok("activation mail sent !");

        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user with email "+ email + " not found");
    }

}
