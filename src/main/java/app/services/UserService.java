package app.services;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.springframework.security.core.context.SecurityContextHolder;
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

        final User user = userRepo.findOneByUserNameOrEmail(username, username);

        // if user not found
        if (user == null) {

            throw new UsernameNotFoundException(username);
        }
        if (config.isUserVerification() && !user.getToken().equals("1")) {

            Application.LOGGER.error("User [{}] tried to login but account is not activated yet", username);

            throw new UsernameNotFoundException(username + " has not been activated yet");
        }

        final List<GrantedAuthority> auth = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole());

        return new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), auth);
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

    public User register(final User user) {

        user.setPassword(encodeUserPassword(user.getPassword()));

        if (userRepo.findOneByUserName(user.getUserName()) == null
                && userRepo.findOneByEmail(user.getEmail()) == null) {

            final String activation = createActivationToken(user, false);

            user.setToken(activation);

            this.userRepo.save(user);

            return user;
        }

        return null;
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

        String toEncode = user.getEmail() + user.getUserName() + config.getSecret();

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

        final User u = userRepo.findOneByUserName(user.getUserName());

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

    public void updateLastLogin(String userName) {

        userRepo.updateLastLogin(userName);
    }


    public void createSaveDirectory(final String saveDirectory) {

        final File test = new File(saveDirectory);

        if (!test.exists()) {

            try {

                test.mkdirs();

            } catch (final Exception e) {

                LOGGER.error("Error creating user directory", e);
            }
        }
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public String handleFileUpload(MultipartFile file, String token) {

        // final Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        // final String fileName = formatter.format(Calendar.getInstance().getTime()) + "_thumbnail.jpg";
        final User user = userRepo.findOneByUserName(jwtUtil.extractUsername(token));

        if (!file.isEmpty()) {

            try {
                final String saveDirectory = config.getUserRoot() + File.separator + "users" + File.separator + user.getId() + ".jpg";
                createSaveDirectory(saveDirectory);

                final byte[] bytes = file.getBytes();

                final ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bytes);
                final BufferedImage image = ImageIO.read(imageInputStream);
                final BufferedImage thumbnail = Scalr.resize(image, 200);

                final File thumbnailOut = new File(saveDirectory);
                ImageIO.write(thumbnail, "png", thumbnailOut);

                // log.debug("Image Saved::: {}", fileName);

            } catch (final Exception e) {

                // log.error("Error Uploading File", e);
            }
        }
        return "redirect:/user/edit/" + user.getId();
    }

    public byte[] handleFileSend(Long user_id) throws FileNotFoundException, IOException {

        final String profilePicture = config.getUserRoot() + File.separator + "users" + File.separator + user_id + ".jpg";

        try{

            if (new File(profilePicture).exists()) {
    
                return IOUtils.toByteArray(new FileInputStream(profilePicture));
    
            }
        }
         catch (FileNotFoundException f) {
            //TODO: handle exception
            LOGGER.info("file not found" + f);
        }
        catch(IOException io){
            LOGGER.info("io" + io);
        }

        return null;
    }
}