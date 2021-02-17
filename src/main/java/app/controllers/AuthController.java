package app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.models.entity.AuthenticationRequest;
import app.models.entity.AuthenticationResponse;
import app.models.entity.User;
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
	private JwtUtil jwtTokenUtil;
    
	@Autowired
	private UserService userDetailsService;
    
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

		final String jwt = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

    // try catch dosnt work properly
    @PostMapping("/register")
    public String registerUser(@RequestBody User user) {

        User registeredUser = userService.register(user);

        if (registeredUser != null) {
            try{

                mailService.sendNewRegistration(user.getEmail(), registeredUser.getToken());
            }
            catch(Error er) {
                userService.delete(registeredUser.getId());
                return er.toString();
            }

            return "activation mail sent successfully";
        }
        return "user already registered";
    }

    @PostMapping("/reset-password")
    // type email to receive input
    public String resetPasswordEmailPost(@RequestBody User user) {

        final User u = userRepository.findOneByEmail(user.getEmail());

        if (u == null) {

            // return error
            return "user/reset-password";

        } else {

            final String resetToken = userService.createActivationToken(u, true);

            // LOGGER.debug("Resetting password for user: {}, new token: {}",
            // user.getEmail(), resetToken);
            
            mailService.sendResetPassword(user.getEmail(), user.getFirstName(), resetToken);
        }
        return "user/reset-password-sent";
    }
    
    @GetMapping("/user/reset-password-change")
    
    public String resetPasswordChange(final User user, final BindingResult result, final Model model) {

        final User u = userRepository.findOneByToken(user.getToken());
        
        if (user.getToken().equals("1") || u == null) {
        	
            result.rejectValue("activation", "error.doesntExist", "We could not find this reset password request.");
            
        } else {
        	
            model.addAttribute("userName", u.getUserName());
        }
        return "user/reset-password-change";
    } 
    
    // post new password
    @PostMapping("/reset-password-change")
    public String resetPasswordChangePost(@RequestBody User user) {

        final boolean isChanged = userService.resetPassword(user);

        if (isChanged) {
            return "success";

        } else {

            return "user/reset-password-change";
        }
    }

    @GetMapping("/activate")
    public String activate(@RequestParam String activation) {

        final User u = userService.activate(activation);

        if (u != null) {

            return "Account successfully activated, please login";
        }
        return "redirect:/error?message=Could not activate with this activation code, please contact support";
    }
    
    // for changing verified mail for registered user
    @PostMapping("/activation-send")
    public String activationSendPost(final User user)
    {

        final User u = userService.resetActivation(user.getEmail());

        if (u != null){

        mailService.sendNewActivationRequest(u.getEmail(), u.getToken());

        return "/activation-sent";

        } else {

        // result.rejectValue("email", "error.doesntExist", "We could not find this
        // email in our databse");

        return "/activation-send";
        }
    }

}
