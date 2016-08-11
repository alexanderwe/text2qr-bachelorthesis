package webservice.model;

import webservice.model.User;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for password reset tokens
 * Created by alexanderweiss on 20.03.16.
 */
public class PasswordResetToken {


    private User user;
    private String token;
    private LocalDateTime age;
    private LocalDateTime expires;


    /**
     * Constructor for creating new password reset tokens
     * @param user
     * @param token
     */
    public PasswordResetToken(User user, String token){
        this.user = user;
        this.token = token;
        this.age = LocalDateTime.now();
        this.expires = age.plusHours(12);
    }


    /**
     * Constructor for getting password reset tokens from database
     * @param user
     * @param token
     * @param age
     * @param expires
     */
    public PasswordResetToken(User user, String token, LocalDateTime age, LocalDateTime expires){
        this.user = user;
        this.token = token;
        this.age = age;
        this.expires = expires;
    }



    public  User getUser() {
        return user;
    }

    public  void setUser(User user) {
        this.user = user;
    }

    public  String getToken() {
        return token;
    }

    public  void setToken(String token) {
       this.token = token;
    }

    public  LocalDateTime getAge() {
        return age;
    }

    public  void setAge(LocalDateTime age) {
        this.age = age;
    }

    public  LocalDateTime getExpires() {
        return expires;
    }

    public  void setExpires(LocalDateTime expires) {
        this.expires = expires;
    }
}
