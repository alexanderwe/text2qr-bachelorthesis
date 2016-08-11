package webservice.controllers;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import webservice.services.UserService;
import webservice.model.User;
import webservice.model.User_Authority;
import webservice.services.MailService;

import javax.mail.MessagingException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Created by alexanderweiss on 13.03.16.
 * Class for mapping the registration process.
 */
@Controller
public class RegisterController {

    private UserService userService = new UserService();
    private MailService mailService = new MailService();

    @RequestMapping(value="/register", method=RequestMethod.GET)
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }


    /**
     * Start register process
     * 1. Validate email
     * 2. Validate password
     * 3. Check if user email already exists
     * 4. Try to insert user --> if sql error --> username already exists
     * 5. if everything works, construct double opt in mail and send it to user
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/register", method=RequestMethod.POST)
    public String registerSubmit(@ModelAttribute User user, Model model) {

        if(userService.findUserByEmail(user.getEmail())!=null){
            model.addAttribute("userInsertError", true);
            return "register";
        }else{
            try{
                String uuid = UUID.randomUUID().toString();
                userService.insertUser(user.getUsername(), new BCryptPasswordEncoder().encode(user.getPassword()),user.getEmail(), User_Authority.ROLE_USER, false,uuid, LocalDateTime.now() );

                user.setUuid(uuid);
                model.addAttribute("user", user);

                try {
                    mailService.sendRegisterMail(user);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return "result";
            }catch(SQLException mysqle){ // User exists
                mysqle.printStackTrace();
                model.addAttribute("userInsertError", true);
                return "register";
            }
        }
    }

    /**
     * Map request to /register/activate/sendmail. Sends the registration mail to user with uuid.
     * @param uuid
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value = "/register/activate/sendmail")
    public String sendMailAgain(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid, @ModelAttribute User user, Model model){
       User dbuser =  userService.findUserByUUID(uuid);

    if(dbuser.isEnabled()){
        model.addAttribute("user",dbuser);
        model.addAttribute("enabled",true);
        return "send_mail_again";
        }else{
            try {
                mailService.sendRegisterMail(dbuser);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            model.addAttribute("user",dbuser);
            return "send_mail_again";
        }
    }

    /**
     * Map request to /register/activate. Activates user with uuid. Link is mostly clicked in the registration email.
     * @param uuid
     * @return
     */
    @RequestMapping(value = "/register/activate")
    public String activateUser(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid){
        userService.enableUser(uuid);
        return "activation_success";
    }

}