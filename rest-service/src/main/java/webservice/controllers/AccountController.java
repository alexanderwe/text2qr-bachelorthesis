package webservice.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import webservice.services.UserService;
import webservice.model.User;
import webservice.services.MailService;
import webservice.model.PasswordResetToken;


import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by alexanderweiss on 14.03.16.
 */
@Controller
public class AccountController {

    private UserService userService = new UserService();
    private MailService mailService = new MailService();

    /**
     * Map requests to /login
     * @param error
     * @param logout
     * @param passwordreset
     * @param passwordchanged
     * @return
     */
    @RequestMapping(value="/login", method= RequestMethod.GET)
    public ModelAndView showLogin(@RequestParam(value = "error", required = false) String error , @RequestParam(value = "logout", required = false) String logout, @RequestParam(value = "passwordreset", required = false) String passwordreset,@RequestParam(value = "passwordchange", required = false) String passwordchanged , @RequestParam(value = "deletesuccess", required = false) String deletesuccess ) {

        ModelAndView model = new ModelAndView();
        model.addObject("user", new User());
        if (error != null) {
            model.addObject("error",true);
        }

        if (passwordreset != null) {
            model.addObject("passwordreset",true);
        }

        if (passwordchanged != null) {
            model.addObject("passwordchanged",true);
        }

        if (deletesuccess != null) {
            model.addObject("deletesuccess",true);
        }

        if (logout != null) {
            model.addObject("logout",true);
            manualLogout();
        }

        model.setViewName("login");

        return model;
    }

    /**
     * Map requests to /login
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/login", method= RequestMethod.POST)
    public String showLogin(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "redirect:/account";
    }

    /**
     * Map POST requests to /login/resetpassword. Resets the password of a specific user.
     * @param userEmail
     * @return
     */
    @RequestMapping(value="/login/resetpassword", method= RequestMethod.POST)
    public String resetPassword(@RequestParam("email") String userEmail) {
        User user = userService.findUserByEmail(userEmail);
        if(user != null){
            PasswordResetToken token = new PasswordResetToken(user, UUID.randomUUID().toString());
            userService.insertPasswordResetToken(token);

            try {
                mailService.sendResetPasswordMail(token);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            return "redirect:../login?passwordreset";
        }else{
            return "redirect:../forgotpassword?error";
        }
    }

    /**
     * Map requests to /login/changepassword. Checks if password reset token is valid. If invalid inform user.
     * @param uuid
     * @param token
     * @param model
     * @return
     */
    @RequestMapping(value="/login/changepassword", method= RequestMethod.GET)
    public String changePassword( @RequestParam("uuid") String uuid, @RequestParam("token") String token, Model model) {
        PasswordResetToken passwordResetToken = userService.findPasswordResetTokenByToken(token);

        if(LocalDateTime.now().isAfter(passwordResetToken.getExpires())){  // check if token expires date is in the past --> TOKEN INVALID
            return "redirect:/forgotpassword?tokenerror";
        }else{
            model.addAttribute("user", passwordResetToken.getUser());
            return "changepassword";
        }
    }

    /**
     * Map requests to /login/savepassword. Save a new password for a specific user.
     * @param user
     * @return
     */
    @RequestMapping(value="/login/savepassword", method= RequestMethod.POST)
    public String savePassword(@ModelAttribute User user) {

        userService.updatePassword(user.getUuid(), new BCryptPasswordEncoder().encode(user.getPassword()));
        userService.makePasswordTokenInvalidByUuid(user.getUuid());
        try {
            mailService.sendPasswordChangedMail(userService.findUserByUUID(user.getUuid()));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "redirect:../login?passwordchange";
    }


    /**
     * Map requests to /forgotpassword
     * @param error
     * @param tokenerror
     * @return
     */
    @RequestMapping(value="/forgotpassword", method= RequestMethod.GET)
    public ModelAndView showForgottpassword(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "tokenerror", required = false) String tokenerror ) {

        ModelAndView model = new ModelAndView();
        if (error != null) {
            model.addObject("error",true);
        }

        if (tokenerror != null) {
            model.addObject("tokenerror",true);
        }

        model.setViewName("forgotpassword");
        return model;
    }

    /**
     * Map requests to /account.
     * @param user
     * @param model
     * @param editsuccess
     * @param editerror
     * @return
     */
    @RequestMapping(value="/account", method= RequestMethod.GET)
    public ModelAndView showAccount(@ModelAttribute User user, Model model, @RequestParam(value = "editsuccess", required = false) String editsuccess, @RequestParam(value = "editerror", required = false) String editerror) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        user = userService.findUserByName(username);

        ModelAndView model2 = new ModelAndView();
        if (editsuccess != null) {
            model2.addObject("editsuccess",true);
        }
        if (editerror!= null) {
            model2.addObject("editerror",true);
        }

        model.addAttribute("user", user);
        return model2;
    }

    /**
     * Map POST requets to /account/changedetails. Redirects to /account after changing account details. If an error occurs inform user on website.
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/account/changedetails", method= RequestMethod.POST)
    public String changeDetails(@ModelAttribute User user, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User userToChange = userService.findUserByName(username);

        if(userToChange.getPassword().equals(user.getPassword())){
            //doNothing, because the password stays the same
        }else{
            userService.updatePassword(userToChange.getUuid(), new BCryptPasswordEncoder().encode(user.getPassword()));
        }

        if(userService.updateEmail(userToChange.getUuid(), user.getEmail())){

        }else{
            user = userService.findUserByName(username);
            model.addAttribute("user", user);
            return "redirect:../account?editerror";
        }

        user = userService.findUserByName(username);
        model.addAttribute("user", user);
        return "redirect:../account?editsuccess";
    }


    /**
     * Map requests to /account/delete. Delete user if existing.
     * @param uuid
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/account/delete", method= RequestMethod.GET)
    public String deleteAccount(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid, @ModelAttribute User user, Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        if(username.equals(userService.findUserByUUID(uuid).getUsername())){ // check if user is the same as the user requested to be deleted
            userService.deleteUserByUUID(uuid);
        }else{
            return "redirect:/index";
        }
        model.addAttribute("user", new User());
        manualLogout();
        return "redirect:/login?deletesuccess";
    }


    /**
     * Logout session user manually, beacuse spring framework saves a session cookie when logging out
     */
    public void manualLogout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
