package app.services;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import app.configs.ApplicationConfig;
import app.models.collections.User;

@Service
@EnableAsync
public class MailService {

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private MailSender mailSender;

    public static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    @Async
    public void sendMail(final String to, final String subject, final String text) {

        try {
            if (!config.isEmailMock()) {

                final SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(to);
                email.setSubject(subject);
                email.setFrom(config.getEmailFrom());
                email.setText(text);
                mailSender.send(email);
            }
            LOGGER.info("SENT EMAIL: TO={}|SUBJECT:{}|TEXT:{}", to, subject, text);

        } catch (final Exception e) {

            LOGGER.error("Error sending email", e);
        }
    }
    @Async
    public void sendResetPassword(final String to, String username, final String token) {

        final String url = config.getFrontendUrl() + "/reset-password-change?token=" + token;
        final String subject = "Reset Password";
        final String text = "hey " + username + " !\n\n" + "Welcome to game-hub \n"
                + "Please click the following link to reset your password: " + url
                + "\n\n Happy Coding! \n The game-Hub developer Team";
        sendMail(to, subject, text);
    }
    @Async
    public void sendNewRegistration(final String to, final String token) {

        final String url = config.getUrl() + "/auth/activate?activation=" + token;
        final String subject = "Please activate your account";
        final String text = "\nWelcome to game-hub \n\n" + "Please click the following link to activate your account "
                + url + "\n\n Happy Coding! \n The game-Hub developer Team";
        sendMail(to, subject, text);
    }
    @Async
    public void sendNewActivationRequest(final String to, final String token) {

        sendNewRegistration(to, token);
    }

    @Async
    public void sendErrorEmail(final Exception exception, final HttpServletRequest req, final User user) {

        final String subject = "Application Error: " + req.getRequestURL();
        final String text = "An error occured in your application: " + exception + "\r\nFor User:  " + user.getEmail();
        sendMail(config.getEmailSupport(), subject, text);
    }

    @Async
    public void sendInvite(final String to, String username, String groupName, String groupAdmin, final String token) {

        final String url = config.getUrl() + "/groups/join?token=" + token;
        final String subject = "Group Invite";
        final String text = "hey " + username + " !\n\n" + "Welcome to TaskTeam, \n"
                + "You have been invited by " + groupAdmin + " to join " + groupName + " \njoining link " + url;
        sendMail(to, subject, text);
    }

    @Async
    public void sendTaskNotification( String to, String username, String groupName, String taskAuthor, String taskShortDescription,
                                    String taskId, String groupId ) {

        final String url = config.getUrl() + "/tasks/" + taskId + "?groupId=" + groupId;
        final String subject = "Group Invite";
        final String text = "hey " + username + ",\n"
                 + taskAuthor + " has posted a new task in " + groupName + " \n task Link => " + url;
        sendMail(to, subject, text);
    }

}
