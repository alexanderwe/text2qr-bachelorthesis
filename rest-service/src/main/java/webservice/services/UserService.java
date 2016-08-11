package webservice.services;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import webservice.model.User;
import webservice.model.User_Authority;
import webservice.model.PasswordResetToken;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


/**
 * Created by alexanderweiss on 15.03.16.
 * MySQL database connection for users.
 */
public class UserService {



    private MysqlDataSource  dataSource;
    private Connection c;
    DateTimeFormatter dateTimeFormatter;
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(UserService.class);

    /**
     * Default constructor
     */
    public UserService(){
        dateTimeFormatter  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }


    /**
     * Connect to database
     * @return
     */
    public boolean connect() {
        try {
            dataSource = new MysqlDataSource();
            dataSource.setServerName("127.0.0.1");
            dataSource.setPort(8889);
            dataSource.setDatabaseName("users");
            dataSource.setUser("root");
            dataSource.setPassword("root");
            c = dataSource.getConnection();
            logger.info("Connection to user database established");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Disconnect from database
     * @return boolean
     */
    public boolean disconnect(){
        try{
            c.close();
            logger.info("Connection to user database closed ");
            return true;
        }catch (SQLException sqle){
            logger.error("Error closing connection to user database - " +"\n" + sqle);
            return false;
        }

    }

    /**
     * Get all users in database.
     * @return ArrayList
     */
    public ArrayList<User> getAllUsers(){
        ArrayList<User> users = new ArrayList<User>();

        connect();

        String username = null;
        String password = null;
        String email = null;
        boolean enabled = false;
        String uuid;
        String authority = null;
        int translation_count = 0;
        LocalDateTime age;
        boolean isAdmin = false;
        try{
            Statement stmt = c.createStatement();
            Statement stmt2 = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM users;");

            while ( rs.next() ) {
                isAdmin = false;
                username = rs.getString("username");
                password = rs.getString("password");
                email = rs.getString("email");
                enabled = rs.getBoolean("enabled");
                uuid = rs.getString("uuid");
                translation_count = rs.getInt("translation_count");


                String ageString = rs.getString("age");
                int dotindex= ageString.indexOf(".");
                age = LocalDateTime.parse(ageString.substring(0,dotindex), dateTimeFormatter);



                ResultSet rs2 = stmt2.executeQuery( "SELECT *  FROM authorities WHERE username = '"+username+"';");
                while (rs2.next()){
                    authority = rs2.getString("authority");
                    if (authority.equals("ROLE_ADMIN")){
                        isAdmin = true;
                    }
                }
                User user;
                if(isAdmin){
                    user = new User(username,password,email,enabled,uuid,User_Authority.ROLE_USER.valueOf("ROLE_ADMIN"),translation_count,age);
                }else{
                    user = new User(username,password,email,enabled,uuid,User_Authority.ROLE_USER.valueOf(authority),translation_count,age);
                }
                users.add(user);
                rs2.close();
            }
            rs.close();
            stmt.close();
            stmt2.close();
        }catch(SQLException sqle){
            logger.error("Error getting all users from user database- " +"\n" + sqle);
        }
        disconnect();
        return users;
    }


    /**
     * Insert a user.
     * @param username
     * @param password
     * @param email
     * @param user_authority
     * @param enabled
     * @param uuid
     * @param age
     * @throws SQLException
     */
    public void insertUser(String username, String password, String email, User_Authority user_authority,  boolean enabled, String uuid, LocalDateTime age) throws SQLException{
        connect();
        logger.info("Insert new user in database - " + uuid );
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fromattedAge = age.format(dateTimeFormatter);

        int enabled_value;
        if(enabled){
            enabled_value = 1;
        }else {
            enabled_value = 0;
        }

        Statement stmt = c.createStatement();
        String sql = "INSERT INTO users (username,password,email,enabled,uuid,age) " +
                "VALUES ('"+username+"' , '"+password+"','"+email+"','"+ enabled_value+"','"+ uuid +"','"+ fromattedAge+"' );";
        stmt.executeUpdate(sql);

        String sql2 = "INSERT INTO authorities (uuid,username,authority) " +
                "VALUES ('"+ uuid+"' , '"+username+"' , '"+user_authority.name()+"' );";

        stmt.executeUpdate(sql2);
        stmt.close();
        disconnect();
    }

    /**
     * Find user by uuid.
     * @param uuid
     * @return User
     */
    public User findUserByUUID(String uuid){
        connect();
        String username = null;
        String password = null;
        String email = null;
        boolean enabled = false;
        String authority = null;
        int translation_count = 0;
        LocalDateTime age =null;
        boolean isAdmin = false;
        try{
            Statement stmt = c.createStatement();
            Statement stmt2 = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM users WHERE uuid = '"+uuid+"';");

            while ( rs.next() ) {
                isAdmin = false;
                username = rs.getString("username");
                password = rs.getString("password");
                email = rs.getString("email");
                enabled = rs.getBoolean("enabled");
                translation_count = rs.getInt("translation_count");

                String ageString = rs.getString("age");
                int dotindex= ageString.indexOf(".");
                age = LocalDateTime.parse(ageString.substring(0,dotindex), dateTimeFormatter);

                ResultSet rs2 = stmt2.executeQuery( "SELECT *  FROM authorities WHERE username = '"+username+"';");
                while (rs2.next()){
                    authority = rs2.getString("authority");
                    if(authority.equals("ROLE_ADMIN")){
                        isAdmin = true;
                    }
                }
                rs2.close();
                stmt2.close();
            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding user by UUID - " +"\n" + sqle);
        }
        disconnect();
        User user;
        if(isAdmin){
            user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf("ROLE_ADMIN"), translation_count,age);
        }else{
            user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf(authority), translation_count,age);
        }
        return user;
    }

    /**
     * Find user by username
     * @param name
     * @return User
     */
    public User findUserByName(String name){
        connect();
        String username = null;
        String password = null;
        String email = null;
        String uuid = null;
        boolean enabled = false;
        int translation_count = 0;
        String authority = null;
        LocalDateTime age = null;
        boolean isAdmin = false;
        try{
            Statement stmt = c.createStatement();
            Statement stmt2 = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM users WHERE username = '"+name+"';");

            while ( rs.next() ) {
                isAdmin = false;
                username = rs.getString("username");
                password = rs.getString("password");
                email = rs.getString("email");
                enabled = rs.getBoolean("enabled");
                uuid = rs.getString("uuid");
                translation_count = rs.getInt("translation_count");

                String ageString = rs.getString("age");
                int dotindex= ageString.indexOf(".");
                age = LocalDateTime.parse(ageString.substring(0,dotindex), dateTimeFormatter);

                ResultSet rs2 = stmt2.executeQuery( "SELECT *  FROM authorities WHERE username = '"+username+"';");
                while (rs2.next()){
                    authority = rs2.getString("authority");
                    if (authority.equals("ROLE_ADMIN")){
                        isAdmin = true;
                    }
                }
                rs2.close();
                stmt2.close();
            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding user by name - " +"\n" + sqle);
        }
        disconnect();
        User user;
        if(isAdmin){
            user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf("ROLE_ADMIN"), translation_count,age);
        }else{
            user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf(authority), translation_count,age);
        }
        return user;
    }

    /**
     * Find user by email
     * @param email
     * @return User
     */
    public User findUserByEmail(String email){
        connect();
        String username = null;
        String password = null;
        String uuid = null;
        boolean enabled = false;
        int translation_count = 0 ;
        LocalDateTime age = null;
        String authority = null;
        boolean isAdmin = false;
        try{
            Statement stmt = c.createStatement();
            Statement stmt2 = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM users WHERE email = '"+email+"';");

            while ( rs.next() ) {
                isAdmin = false;
                username = rs.getString("username");
                password = rs.getString("password");
                email = rs.getString("email");
                enabled = rs.getBoolean("enabled");
                uuid = rs.getString("uuid");
                translation_count = rs.getInt("translation_count");

                String ageString = rs.getString("age");
                int dotindex= ageString.indexOf(".");
                age = LocalDateTime.parse(ageString.substring(0,dotindex), dateTimeFormatter);
                ResultSet rs2 = stmt2.executeQuery( "SELECT *  FROM authorities WHERE username = '"+username+"';");
                while (rs2.next()){
                    authority = rs2.getString("authority");
                    if (authority.equals("ROLE_ADMIN")){
                        isAdmin = true;
                    }
                }
                rs2.close();
                stmt2.close();
            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding user by email - " +"\n" + sqle);
        }
        disconnect();
        try{
            User user;
            if(isAdmin){
                user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf("ROLE_ADMIN"), translation_count, age);
            }else{
                user = new User(username ,password,email,enabled,uuid, User_Authority.valueOf(authority), translation_count, age);
            }
            return user;
        }catch (NullPointerException nulle){
            nulle.printStackTrace();
            return null;
        }

    }

    /**
     * Update password of specific user
     * @param uuid
     * @param password
     * @return boolean
     */
    public boolean updatePassword(String uuid, String password){
        connect();
        logger.info("Update user password for user " + uuid);
        try{
            Statement stmt = c.createStatement();
            stmt.executeUpdate( "UPDATE users SET password ='"+password+"' WHERE uuid ='"+uuid+"';");
            stmt.close();

        }catch(SQLException sqle){
            logger.error("Error updating password for user "+uuid+" - " +"\n" + sqle);
            return false;
        }
        disconnect();
        return true;
    }

    /**
     * Update email of specific user
     * @param uuid
     * @param email
     * @return boolean
     */
    public boolean updateEmail(String uuid, String email){
        connect();
        logger.info("Update email of user " + uuid);
        try{
            Statement stmt = c.createStatement();
            stmt.executeUpdate( "UPDATE users SET email ='"+email+"' WHERE uuid ='"+uuid+"';");
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error updating email for user "+ uuid +" - " +"\n" + sqle);
            return false;
        }
        disconnect();
        return true;


    }

    /**
     * Increase translation count for user.
     * @param username
     */
    public void increaseTranslationCount(String username){
        User user = findUserByName(username);
        connect();
        logger.info("Increase translation count for user " + user.getUuid());
        try{
            Statement smt = c.createStatement();
            smt.executeUpdate("UPDATE users SET translation_count = translation_count + 1 WHERE username='"+username+"';");
            smt.close();
        }catch(SQLException sqle){
            logger.error("Error increasing translation count for user "+user.getUuid() +" - " +"\n" + sqle);
        }
        disconnect();
    }


    /**
     * Acvivate user by uuid
     * @param uuid
     */
    public void enableUser(String uuid){
        connect();
        logger.info("Enable user " +uuid);
        try{
            Statement stmt = c.createStatement();
            stmt.executeUpdate( "UPDATE users SET enabled = 1 WHERE uuid ='"+uuid+"';");
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error activating user "+ uuid +" - " +"\n" + sqle);
        }
        disconnect();
    }

    /**
     * Disable user by uuid
     * @param uuid
     */
    public void disableUser(String uuid){
        connect();
        logger.info("Disable user " + uuid);
        try{
            Statement stmt = c.createStatement();
            stmt.executeUpdate( "UPDATE users SET enabled = 0 WHERE uuid ='"+uuid+"';");
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error disable user "+ uuid +" - " +"\n" + sqle);
        }
        disconnect();
    }

    /**
     * Delete user by uuid
     * @param uuid
     */
    public void deleteUserByUUID(String uuid){

        User userToDelete  = findUserByUUID(uuid);
        connect();
        logger.info("Delete user " + uuid);
        try{
            Statement stmt = c.createStatement();
            Statement stmt2 = c.createStatement();

            stmt.executeUpdate("DELETE FROM authorities WHERE username ='"+userToDelete.getUsername()+"';");
            stmt.executeUpdate("DELETE FROM passwordresettokens WHERE uuid ='"+uuid+"';");
            stmt2.executeUpdate("DELETE FROM users WHERE username ='"+userToDelete.getUsername()+"';");
            stmt.close();
            stmt2.close();
        }catch(SQLException sqle){
            logger.error("Error deleting "+ uuid + "- " +"\n" + sqle);
        }
        disconnect();
    }

    /**
     * Insert password reset token
     * @param token
     */
    public void insertPasswordResetToken(PasswordResetToken token){
        connect();
        logger.info("Insert password reset token");
        String fromattedAge = token.getAge().format(dateTimeFormatter);
        String fromattedExpires = token.getExpires().format(dateTimeFormatter);

        try{
            Statement stmt = c.createStatement();
            String sql = "INSERT INTO passwordresettokens (uuid,token,age,expires) " +
                    "VALUES ('"+token.getUser().getUuid()+"' , '"+token.getToken()+"','"+fromattedAge+"','"+ fromattedExpires+"' );";
            stmt.executeUpdate(sql);
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error insert password reset token - " +"\n" + sqle);
        }
        disconnect();
    }

    /**
     * Find password reset token by token string
     * @param token
     * @return PasswordResetToken
     */
    public PasswordResetToken findPasswordResetTokenByToken(String token){
        connect();
        logger.info("Find passsword token by token string");
        User user;
        String uuid = null;

        LocalDateTime age = null;
        LocalDateTime expires = null;


        try{
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM passwordresettokens WHERE token = '"+token+"';");

            while ( rs.next() ) {
                uuid = rs.getString("uuid");
                token = rs.getString("token");


                String ageString = rs.getString("age");
                int dotindex= ageString.indexOf(".");
                age = LocalDateTime.parse(ageString.substring(0,dotindex), dateTimeFormatter);


                String expiresString = rs.getString("expires");
                dotindex = expiresString.indexOf(".");
                expires = LocalDateTime.parse(expiresString.substring(0,dotindex), dateTimeFormatter);

            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding password token by token - " +"\n" + sqle);
        }
        disconnect();
        try{
            user = findUserByUUID(uuid);
            PasswordResetToken passwordResetToken = new PasswordResetToken(user,token,age,expires);
            return passwordResetToken;
        }catch (NullPointerException nulle){
            logger.error("Error while returning password token. User not in databse - " +"\n" + nulle);
            return null;
        }
    }


    /**
     * Make password reset token invalid by uuid.
     * Every existing reset token is made invalid. Their expire date will be set to the same date. This is a little
     * uncomfortable.
     * @param uuid
     */
    public void makePasswordTokenInvalidByUuid(String uuid){
        connect();
        logger.info("Make password token invalid for uuid " +uuid);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String newExpires = LocalDateTime.now().format(dateTimeFormatter);
        try{
            Statement smt = c.createStatement();
            smt.executeUpdate("UPDATE passwordresettokens SET expires = '"+newExpires+"' WHERE uuid='"+uuid+"';");
            smt.close();
        }catch(SQLException sqle){
            logger.error("Error making password token invalid for uuid "+ uuid +" - " +"\n" + sqle);
        }
        disconnect();
    }
}
