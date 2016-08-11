package webservice.services;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import webservice.model.PasswordResetToken;
import webservice.model.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

/**
 * Created by alexanderweiss on 14.03.16.
 * Class for sending emails to users and admin
 */
public class MailService {

    private JavaMailSender mailSender;

    public MailService(){
        try {
            this.mailSender = mailSender();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new mail sender
     * @return
     * @throws IOException
     */
    @Bean
    public JavaMailSender mailSender() throws IOException {
        Properties properties = configProperties();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getProperty("mail.server.host"));
        mailSender.setPort(Integer.parseInt(properties.getProperty("mail.server.port")));
        mailSender.setProtocol(properties.getProperty("mail.server.protocol"));
        mailSender.setUsername(properties.getProperty("mail.server.username"));
        mailSender.setPassword(properties.getProperty("mail.server.password"));
        mailSender.setJavaMailProperties(javaMailProperties());
        return mailSender;
    }

    /**
     * Configuration properties
     * @return Properties
     * @throws IOException
     */
    private Properties configProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource("configuration.properties").getInputStream());
        return properties;
    }

    /**
     * Configure mail properties
     * @return Properties
     * @throws IOException
     * @throws IOException
     */
    private Properties javaMailProperties() throws IOException, IOException {
        Properties properties = new Properties();
        properties.load(new ClassPathResource("javamail.properties").getInputStream());
        return properties;
    }

    /**
     * Sen registration email
     * @param user
     * @throws MessagingException
     */
    public void sendRegisterMail(User user) throws MessagingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        message.setFrom("registration@text2qr.com");
        message.setTo(user.getEmail());
        message.setSubject("Your registration at Text2QR-Translator");

        String activateLink = PropertyService.getServerUrl()+"/register/activate/?uuid="+user.getUuid();
        String body = ResourceManager.loadRegisterEmailTemplate().replace("${user.userName}",user.getUsername()).replace("${activateLink}",activateLink);

        message.setText(body,true);
        this.mailSender.send(mimeMessage);
    }


    /**
     * Send a notification that user request password reset
     * @param token
     * @throws MessagingException
     */
    public void sendResetPasswordMail(PasswordResetToken token) throws MessagingException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        message.setFrom("registration@text2qr.com");
        message.setTo(token.getUser().getEmail());
        message.setSubject("Password Reset");

        String passwordResetLink = PropertyService.getServerUrl()+"/login/changepassword?uuid="+token.getUser().getUuid()+"&token="+token.getToken();
        String body = ResourceManager.loadPasswordResetEmailTemplate().replace("${user.userName}",token.getUser().getUsername()).replace("${passwordResetLink}",passwordResetLink);

        message.setText(body,true);
        this.mailSender.send(mimeMessage);
    }


    /**
     * Send mail to user, that his password has changed
     * @param user
     * @throws MessagingException
     */
    public void sendPasswordChangedMail(User user) throws MessagingException{
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        message.setFrom("registration@text2qr.com");
        message.setTo(user.getEmail());
        message.setSubject("Password changed");
        String body = ResourceManager.loadPasswordChangedEmailTemplate().replace("${user.userName}",user.getUsername());

        message.setText(body,true);
        this.mailSender.send(mimeMessage);
    }

    /**
     * Send mail to admin, that a new contact form was submitted
     * @param name
     * @param useremail
     * @param usermessage
     * @throws MessagingException
     */
    public void sendContactFormSubmitToAdmin(String name, String useremail, String usermessage) throws MessagingException, IOException {
        final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
        message.setFrom("server@text2qr.com");
        message.setTo(configProperties().getProperty("mail.admin.mail"));
        message.setSubject("New contact form submit");

        String body = "<strong>NEW CONTACT FORM SUBMIT</strong> <br /> <br />  " +
                "Name: " + name + "<br />"+
                "Email: " + useremail + "<br />"+
                "Message: " + usermessage ;
        message.setText(body,true);
        this.mailSender.send(mimeMessage);
    }
}
