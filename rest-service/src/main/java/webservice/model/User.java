package webservice.model;

import java.time.LocalDateTime;

/**
 * User DTO
 * Created by alexanderweiss on 11.03.16.
 */
public class User {
    private String username;
    private String email;
    private String password;
    private boolean enabled;
    private String uuid;
    private User_Authority user_authority;
    private int translation_count;
    private LocalDateTime age;


    public User(String username, String password, String email, boolean enabled, String uuid, User_Authority user_authority, int translation_count, LocalDateTime age){
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.uuid = uuid;
        this.user_authority = user_authority;
        this.translation_count = translation_count;
        this.age = age;
    }

    public User(){
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public User_Authority getUser_authority() {
        return user_authority;
    }

    public void setUser_authority(User_Authority user_authority) {
        this.user_authority = user_authority;
    }

    @Override
    public String toString(){
        return  this.username + System.lineSeparator()
                + this.password + System.lineSeparator()
                + this.email + System.lineSeparator()
                + this.isEnabled() + System.lineSeparator()
                + this.uuid + System.lineSeparator()
                + this.translation_count;
    }

    public int getTranslation_count() {
        return translation_count;
    }

    public void setTranslation_count(int translation_count) {
        this.translation_count = translation_count;
    }

    public LocalDateTime getAge() {
        return age;
    }

    public void setAge(LocalDateTime age) {
        this.age = age;
    }
}
