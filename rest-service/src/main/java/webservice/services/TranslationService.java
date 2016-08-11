package webservice.services;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import webservice.model.Translation;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by alexanderweiss on 15.03.16.
 * MySQL database connection for translations.
 */
public class TranslationService {


    private MysqlDataSource  dataSource;
    private Connection c;
    private SimpleDateFormat sdf;
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(TranslationService.class);

    /**
     * Default constructor
     */
    public TranslationService(){
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Connect to database
     * @return boolean
     */
    public boolean connect() {
        try {
            dataSource = new MysqlDataSource();
            dataSource.setServerName("127.0.0.1");
            dataSource.setPort(8889);
            dataSource.setDatabaseName("translations");
            dataSource.setUser("root");
            dataSource.setPassword("root");
            c = dataSource.getConnection();
            logger.info("Connection to translation database established");
            return true;
        } catch (Exception e) {
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
            logger.info("Connection to translation database closed ");
            return true;
        }catch (SQLException sqle){
            logger.error("Error closing connection to translation database - " +"\n" + sqle);
            return false;
        }
    }


    /**
     * Insert a translation in the database
     * @param sourceContent
     * @param targetContent
     * @param sourceLang
     * @param targetLang
     * @param match
     */
    public void insertTranslation(String sourceContent, String targetContent, String sourceLang, String targetLang, double match){
        connect();
        try{
            Statement  stmt = c.createStatement();
            String sql = "INSERT INTO translations (sourceContent,targetContent,sourceLang,targetLang,matchPerc,age) " +
                    "VALUES ('"+sourceContent+"' , '"+targetContent+"','"+sourceLang+"','"+targetLang+"' , "+ match +", NOW() );";
            logger.info("Insert new translation in translation database -" + sql);
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException sqle){
            logger.error("Error inserting translation" + "\n" + sqle);
        }
        disconnect();
    }

    /**
     * Insert a translation in the database
     * @param translation
     */
    public void insertTranslation(Translation translation){
        connect();
        try{
            Statement  stmt = c.createStatement();
            String sql = "INSERT INTO translations (sourceContent,targetContent,sourceLang,targetLang,matchPerc,age) " +
                    "VALUES ('"+translation.getSourceContent()+"' , '"+translation.getTargetContent()+"','"+translation.getSourceLang()+"','"+translation.getTargetLang()+"' , "+ translation.getMatch() +",  NOW() );";
            logger.info("Insert new translation -" + sql);
            stmt.executeUpdate(sql);
            stmt.close();
        }catch (SQLException sqle){
            logger.error("Error inserting translation in translation database" + "\n" + sqle);;
        }
        disconnect();
    }

    /**
     * Get all translation from the database
     * @return ArrayList
     */
    public ArrayList<Translation> getTranslations(){
        connect();
        ArrayList<Translation> translations = new ArrayList<Translation>();
        try{
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM translations;" );
            while ( rs.next() ) {
                int id = rs.getInt("ID");
                String sourceContent = rs.getString("sourceContent");
                String targetContent = rs.getString("targetContent");
                String sourceLang = rs.getString("sourceLang");
                String targetLang = rs.getString("targetLang");
                double match = rs.getDouble("matchPerc");
                String age = rs.getString("age");
                int requests = rs.getInt("requests");
                translations.add(new Translation(id,sourceContent,targetContent,sourceLang,targetLang, match, sdf.parse(age),requests+1));
            }
            rs.close();

            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error getting all translations from translation database- " +"\n" + sqle);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        disconnect();
        return translations;
    }

    /**
     * Find a translation with a given id
     * @param id
     * @return Translation
     */
    public Translation findTranslationById(int id){
        connect();
        logger.info("Find translation by ID " +  id);
        Translation translation = null;
        try{
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM translations WHERE id ="+id+";" );
            while ( rs.next() ) {
                id = rs.getInt("id");
                String sourceContent = rs.getString("sourceContent");
                String targetContent = rs.getString("targetContent");
                String sourceLang = rs.getString("sourceLang");
                String targetLang = rs.getString("targetLang");
                double match = rs.getDouble("matchPerc");
                String age = rs.getString("age");
                int requests = rs.getInt("requests");
                translation = new Translation(id,sourceContent,targetContent,sourceLang,targetLang, match, sdf.parse(age),requests+1);
            }
            logger.info("UPDATE translations SET requests = requests + 1 WHERE id ="+id+";" );
            stmt.executeUpdate("UPDATE translations SET requests = requests + 1 WHERE id ="+id+";" );
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding translation by id "+id+" - " +"\n" + sqle);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        disconnect();
        return translation;
    }

    /**
     * Update a translation in database with a given id
     * @param id
     * @param translation
     */
    public boolean updateTranslation(int id, Translation translation){
        connect();
        try{
            Statement stmt = c.createStatement();
            String newsourceContent = translation.getSourceContent();
            String newtargetContent = translation.getTargetContent();
            String newsourceLang = translation.getSourceLang();
            String newtargetLang = translation.getTargetLang();
            double newMatch = translation.getMatch();

            Translation translationDB = findTranslationById(id);



            if(!(translationDB.getSourceContent().equals(newsourceContent))&& (!isNull(newsourceContent))){
                translationDB.setSourceContent(newsourceContent);
            }
            if(!(translationDB.getTargetContent().equals(newtargetContent))&& (!isNull(newtargetContent))){
                translationDB.setTargetContent(newtargetContent);
            }
            if(!(translationDB.getSourceLang().equals(newsourceLang))&& (!isNull(newsourceLang))){
                translationDB.setSourceLang(newsourceLang);
            }
            if(!(translationDB.getTargetLang().equals(newtargetLang))&& (!isNull(newtargetLang))){
                translationDB.setTargetLang(newtargetLang);
            }
            if(!(translationDB.getMatch()==newMatch) && newMatch!=0.0){
                translationDB.setMatch(newMatch);
            }
            String sqlQuery = new String("UPDATE translations SET sourceContent = '"+translationDB.getSourceContent()+"', targetContent ='"+translationDB.getTargetContent()+"', sourceLang = '"+translationDB.getSourceLang()+"', targetLang ='"+translationDB.getTargetLang()+"', matchPerc = "+translationDB.getMatch()+" WHERE ID = "+id+";");
            logger.info("Update translation with id "+id+" - " + sqlQuery);
            stmt.executeUpdate(sqlQuery);
            stmt.close();

        }catch(SQLException sqle){
            logger.error("Error updating translation with id "+id+" - " +"\n" + sqle);
            disconnect();
            return false;
        }
        disconnect();
        return true;
    }

    /**
     * Delete a translation in database with a given id
     * @param id
     * @return boolean
     */
    public boolean deleteTranslation(int id){
        connect();

        if(findID(id)){
            try{
                Statement stmt = c.createStatement();
                logger.info("Delete translation with id "+id+" - " + "DELETE FROM translations WHERE id ="+id+";" );
                stmt.executeUpdate( "DELETE FROM translations WHERE id ="+id+";" );
                stmt.close();
                disconnect();
                return true;
            }catch(SQLException sqle){
                logger.error("Error deleting translation with id "+id+" - " +"\n" + sqle);
                disconnect();
                return false;

            }
        }else{
            disconnect();
            logger.error("Error translation with id "+id+"  not found");
            return false;
        }
    }


    /**
     * Check if id is available
     * @param id
     * @return boolean
     */
    public boolean findID(int id){
        Translation translation = null;
        try{
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT id FROM translations;" );
            while ( rs.next() ) {
                if(id == rs.getInt("ID")){
                    return true;
                }
            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.info("Error finding translation ID");
        }
        logger.info("Find id: "+ id);
        return false;
    }

    /**
     * Get the last saved id in the database
     * @return int
     */
    public int getLastID(){
        connect();
        int id = 0;
        try{
            logger.info("Find last used id - " + "SELECT *  FROM translations WHERE id=(SELECT max(id) FROM translations);" );
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT *  FROM translations WHERE id=(SELECT max(id) FROM translations);");
            while ( rs.next() ) {
                id = rs.getInt("ID");
            }
            rs.close();
            stmt.close();
        }catch(SQLException sqle){
            logger.error("Error finding last inserted ID"+"\n" + sqle);
        }


        disconnect();
        return id;

    }

    /**
     * Check if object is null
     * @param obj
     * @return boolean
     */
    private boolean isNull(Object obj) {
        return obj == null;
    }


}
