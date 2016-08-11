package webservice.controllers;

import java.util.ArrayList;
import java.util.List;

import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import webservice.services.TranslationService;

import webservice.services.UserService;
import webservice.model.Translation;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by alexanderweiss
 * Class for mapping https request on translations. Our RESTFul API.
 */

@RestController
public class APIController {

    private TranslationService translationService = new TranslationService();
    private UserService userService = new UserService();

    /**
     * Map GET request for /translations
     * @return
     */

    @RequestMapping(value="/admin/api/translations",method= RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Translation>> getTranslations() {
        List<Translation> translations =  translationService.getTranslations();
        return new ResponseEntity<List<Translation>>(translations, HttpStatus.OK);
    }

    /**
     * Map DELETE request for /translations/{id}
     * @param id
     * @return
     */
    @RequestMapping(value="admin/api/translations/{id}", method= RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable("id") int id) {
        if(translationService.deleteTranslation(id)){
            return new ResponseEntity<>(new String("Delete successfull"), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(new String("Translation not found, delete not successfull!"), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Map GET request for /translations/{id}
     * @return
     */
    @RequestMapping(value="api/translations/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> getTranslation(@PathVariable("id") String id, HttpServletRequest request) {
        Translation translation = translationService.findTranslationById(Integer.valueOf(id));

        if(translation == null){
            return new ResponseEntity<>(jsonError(request, HttpStatus.NOT_FOUND).toString(), HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(translation, HttpStatus.OK);
        }
    }

    /**
     * Map POST for /translations
     * @param translation
     * @return
     */
    @RequestMapping(value="api/translations", method= RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> post(@Valid @RequestBody Translation translation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        userService.increaseTranslationCount(username);
        translationService.insertTranslation(translation);
        translation.setId(translationService.getLastID());

        return new ResponseEntity<>(translation, HttpStatus.OK);
    }

    /**
     * Map PATCH request for /translations/{id}
     * @param id
     * @param translation
     * @return
     */
    @RequestMapping(value="api/translations/{id}", method= RequestMethod.PATCH, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> patch(@PathVariable("id") int id ,@RequestBody Translation translation , HttpServletRequest request) {

        if(translationService.updateTranslation(id,translation)){
            Translation patchedTranslation = translationService.findTranslationById(id);
            return new ResponseEntity<>(patchedTranslation, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(jsonError(request, HttpStatus.NOT_FOUND).toString(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Map api/checkuser. Is used to check if user is enabled and ready to use service.
     * @return
     */
    @RequestMapping(value="api/checkuser/", method= RequestMethod.GET)
    public ResponseEntity<?> checkUser() {
        return new ResponseEntity<>(new String("User enabled and ok"), HttpStatus.OK); // is only returned when user is registered. Otherwise HTTP error 403 is returned.
    }


    /**
     * Create a json encoded not found exception
     * @param request
     * @param statuses
     * @return
     */
    private JSONObject jsonError(HttpServletRequest request, HttpStatus... statuses){
        //Create json error response
        JSONArray jsonErrors = new JSONArray();

        for(HttpStatus status: statuses){
            JSONObject jsonError =  new JSONObject();
            jsonError.put("source", request.getRequestURL());
            jsonError.put("status", status);

            switch (status.value()){
                case 404: jsonError.put("title" ,status.getReasonPhrase()); jsonError.put("detail","Requested resource not found");break;
                case 400: jsonError.put("title" ,status.getReasonPhrase()); jsonError.put("detail","Request was not formatted correctly. Please check your request.");break;
                default: jsonError.put("title" ,"error"); jsonError.put("detail","Something went wrong");break;
            }
            jsonErrors.put(jsonError);
        }

        JSONObject errors = new JSONObject();
        errors.put("errors", jsonErrors);
        return errors;
    }

}
