package app.services;

import javax.servlet.http.HttpServletRequest;

import app.models.collections.Task;
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
import reactor.core.publisher.Mono;

@Service
@EnableAsync
public class MailService {

    @Autowired
    private ApplicationConfig config;

    @Autowired
    private MailSender mailSender;

    public static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    @Async
    public void sendMail(final String string, final String subject, final String text) {

        try {
            if (!config.isEmailMock()) {

                final SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(string);
                email.setSubject(subject);
                email.setFrom(config.getEmailFrom());
                email.setText(text);
                mailSender.send(email);
            }
            LOGGER.info("SENT EMAIL: TO={}|SUBJECT:{}|TEXT:{}", string, subject, text);

        } catch (final Exception e) {

            LOGGER.error("Error sending email", e);
        }
    }
    @Async
    public void sendResetPassword(final Mono<String> mono, Mono<String> mono2, final String token) {

        final String url = config.getFrontendUrl() + "/reset-password-change?token=" + token;
        final String subject = "Reset Password";
        final String text = "hey " + mono2 + " !\n\n" + "Welcome to game-hub \n"
                + "Click the following link to reset your password: "
                + renderButton("Reset Password", url)+ "\n\n TaskRoom";
        sendMail(mono.block(), subject, text);
    }
    @Async
    public void sendNewRegistration(final String to, final String token) {

        final String url = config.getUrl() + "/auth/activate?activation=" + token;
        final String subject = "Please activate your account";
        final String text = "\n<h6>Welcome to TaskRoom<h6> \n\n" + "Click the following link to activate your account "
                + renderButton("Activate", url) + "\n\n TaskRoom";
        sendMail(to, subject, text);
    }
    @Async
    public void sendNewActivationRequest(final Mono<String> mono, final Mono<String> mono2) {

        sendNewRegistration(mono.block(), mono2.block());
    }

    @Async
    public void sendErrorEmail(final Exception exception, final HttpServletRequest req, final User user) {

        final String subject = "Application Error: " + req.getRequestURL();
        final String text = "An error occured in your application: " + exception + "\r\nFor User:  " + user.getEmail();
        sendMail(config.getEmailSupport(), subject, text);
    }

    @Async
    public void sendInvite(final Mono<String> mono, String username, String groupName, String groupAdmin, final String token) {

        final String url = config.getUrl() + "/groups/join?token=" + token;
        final String subject = "Group Invite";
        final String text = "hey " + username + " !\n\n" + "Welcome to TaskTeam, \n"
                + "You have been invited by " + groupAdmin + " to join " + groupName + " \n" +
                renderButton("Join", url);
        sendMail(mono.block(), subject, text);
    }

    // refactor these methods .. to remove parameters

    private String renderButton(String label, String link){

        return "<a href=\"" + link + "\" style = \" border: none;\n" +
                "  color: white;\n" +
                "  background-color: #4CAF50;\n" +
                "  padding: 15px 32px;\n" +
                "  text-align: center;\n" +
                "  text-decoration: none;\n" +
                "  display: inline-block;\n" +
                "  font-size: 16px;\n" +
                "  margin: 4px 2px;\n" +
                "  cursor: pointer\" >"+ label +"</a>";
    }
    
    @Async
    public void sendTaskNotification( Mono<String> mono, String username, String groupName, Task task) {

        final String url = config.getUrl() + "/tasks/" + task.get_id() + "?groupId=" + task.getGroupId();
        final String subject = "<h5>Group Invite<h5>";
        final String text = "hey " + username + ",\n"
                 + task.getAuthor() + " has posted a new task in " + groupName + " on" + task.getUpdateTime()
                + " \n " + renderButton("View Task", url);
        sendMail(mono.block(), subject, text);
    }

}
