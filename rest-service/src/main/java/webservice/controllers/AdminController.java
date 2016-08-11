package webservice.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import webservice.services.TranslationService;
import webservice.services.UserService;
import webservice.model.Translation;
import webservice.model.User;

import java.util.ArrayList;

/**
 * Created by alexanderweiss on 25.03.16.
 * Class for mapping requests in admin area.
 */
@Controller
public class AdminController {

    private UserService userService = new UserService();
    private TranslationService translationService = new TranslationService();

    /**
     * Map request to /admin/dashboard.  Shows admin dahboard
     * @param model
     * @return
     */
    @RequestMapping(value="/admin/dashboard", method= RequestMethod.GET)
    public String showDashboard( Model model){
        ArrayList<User> users = userService.getAllUsers();
        ArrayList<Translation> translations = translationService.getTranslations();
        model.addAttribute("users", users);
        model.addAttribute("usersCount", users.size());
        model.addAttribute("translations", translations);
        model.addAttribute("translationsCount", translations.size());

        return "dashboard";
    }

    /**
     * Map requests to /admin/users/delete. Admin has the right to delete every user.
     * @param uuid
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/admin/users/delete", method= RequestMethod.GET)
    public String deleteUser(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid, @ModelAttribute User user, Model model){
        userService.deleteUserByUUID(uuid);
        model.addAttribute("user", new User());
        return "redirect:/admin/dashboard";
    }

    /**
     * Map requests to /admin/users/disable. Admin has the right to disable every user.
     * @param uuid
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/admin/users/disable", method= RequestMethod.GET)
    public String disableUser(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid, @ModelAttribute User user, Model model){
        userService.disableUser(uuid);
        model.addAttribute("user", new User());
        return "redirect:/admin/dashboard";
    }

    /**
     * Map requests to /admin/users/activate. Admin has the right to activate every user.
     * @param uuid
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value="/admin/users/activate", method= RequestMethod.GET)
    public String activateUser(@RequestParam(value="uuid", required=true, defaultValue="-1") String uuid, @ModelAttribute User user, Model model){
        userService.enableUser(uuid);
        model.addAttribute("user", new User());
        return "redirect:/admin/dashboard";
    }

}
