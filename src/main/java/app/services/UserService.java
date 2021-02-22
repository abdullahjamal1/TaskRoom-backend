package app.services;

import java.io.File;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import app.Application;
import app.configs.ApplicationConfig;
import app.controllers.AuthController;
import app.models.entity.User;
import app.repositories.UserRepository;
import app.util.JwtUtil;

@Service
public class UserService implements UserDetailsService {

    private static final int INVALID_ACTIVATION_LENGTH = 5;

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AmazonS3ClientService amazonS3ClientService;

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    /**
     * overriding method in UserDeatilsService
     * 
     * Authenticates if a user with given userName exists in dataBase
     * 
     * @param username
     * @return UserDetails
     */

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {

        final User user = userRepo.findOneByUsernameOrEmail(username, username);

        // if user not found
        if (user == null) {

            throw new UsernameNotFoundException(username);
        }
        if (config.isUserVerification() && !user.getToken().equals("1")) {

            Application.LOGGER.error("User [{}] tried to login but account is not activated yet", username);

            throw new UsernameNotFoundException(username + " has not been activated yet");
        }

        final List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), auth);
    }

    /**
     * 
     * registers a user in the database if any other user with same username or
     * Email is not already present. it also assigns a new activation token for the
     * user and saves it in the database so that it can be sent to the user's Email
     * later for Email verification .
     * 
     * @param user
     * @return
     */

    public User register(User user) {

        user.setPassword(encodeUserPassword(user.getPassword()));

        if (userRepo.findOneByUsername(user.getUsername()) == null
                && userRepo.findOneByEmail(user.getEmail()) == null) {

            final String activation = createActivationToken(user, false);

            user.setToken(activation);

            this.userRepo.save(user);

            return user;
        }
        else{
            return null;
        }
    }

    /**
     * encodes the password using BCryptPasswordEncoder
     * 
     * @param password takes input clear text password
     * @return returns encoded password
     */

    public String encodeUserPassword(final String password) {

        final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return passwordEncoder.encode(password);
    }

    /**
     * Deletes user from the database with particular id
     * 
     * @param id Long : id of user to be deleted
     * @return returns true if user deletion is successful
     */

    public void delete(Long id) {
        
        userRepo.deleteById(id);

    }

    /**
     * returns NULL if user's Email id is already verified ( that is activation
     * token = 1).
     * 
     * Verifies user's Email ( by searching from database if any unVerified user has
     * same authentication token as input, if found then authenticates the user by
     * setting token in database as 1 ) if not yet verified.
     * 
     * @param activation takes as input activation token
     * @return
     */

    public User activate(final String activation) {

        if ("1".equals(activation) || activation.length() < INVALID_ACTIVATION_LENGTH) {

            return null;
        }
        final User u = userRepo.findOneByToken(activation);

        if (u != null) {

            u.setToken("1");
            userRepo.save(u);
            return u;
        }
        return null;
    }

    /**
     * encodes a new activation token for an un-Authenticated user by combination of
     * userName, Email and appSecret if save is TRUE then saves the newly generated
     * token corresponding to the user in the database too Returns the newly created
     * activation token of type String
     * 
     * @param user
     * @param save Set it to TRUE if token is to be saved in database
     * @return encoded activation token of type String
     */

    public String createActivationToken(final User user, final boolean save) {

        String toEncode = user.getEmail() + user.getUsername() + config.getSecret();

        final String activationToken = DigestUtils.md5DigestAsHex(toEncode.getBytes(Charsets.UTF_8));

        if (save) {

            user.setToken(activationToken);

            userRepo.save(user);
        }
        return activationToken;
    }

    /**
     * finds user by Email and creates activation token for that user returns user
     * object if it exists in database, else returns NULL
     * 
     * @param email
     * @return
     * @see createActivationToken()
     */

    public User resetActivation(final String email) {

        final User user = userRepo.findOneByEmail(email);

        if (user != null) {

            createActivationToken(user, true);
            return user;
        }
        return null;
    }

    /**
     * resets password for a particular user in database
     * 
     * @param user
     * @return TRUE if user found else FALSE
     */

    public Boolean resetPassword(User user) {

        final User u = userRepo.findOneByUsername(user.getUsername());

        if (u != null) {

            u.setPassword(encodeUserPassword(user.getPassword()));
            u.setToken("1");
            userRepo.save(u);
            return true;
        }
        return false;
    }

    /**
     * updates login TIME_STAMP for userName in database
     * 
     * @param userName
     */

    public void updateLastLogin(String username) {

        userRepo.updateLastLogin(username);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public boolean isAdmin(String token){

        if(userRepo.findOneByUsername(jwtUtil.extractUsername(token)).getRole().equals("ADMIN"))
            return true;
        else return false;
    }

    public void handleFileUpload(MultipartFile file, String token) {

        final User user = userRepo.findOneByUsername(jwtUtil.extractUsername(token));
        String userPath = "users/" + user.getId() +"/";

        this.amazonS3ClientService.uploadFileToS3Bucket(file, userPath + user.getId() + ".jpg", true);      
    }

}