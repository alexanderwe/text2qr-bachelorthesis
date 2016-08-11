package webservice.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Created by alexanderweiss
 * DTO for our translations on the webservice
 */
public class Translation {

    private int id;

    @NotNull
    private String sourceContent;
    @NotNull
    private String targetContent;
    @NotNull
    private String sourceLang;
    @NotNull
    private String targetLang;
    private double match;
    private Date age; //Date used, because of JsonDateSerializer
    private int requests;


    public Translation(){
    }


    /**
     * Constrcutor for normal translation
     * @param sourceContent
     * @param targetContent
     * @param sourceLang
     * @param targetLang
     */
    public Translation(String sourceContent, String targetContent, String sourceLang, String targetLang) {
        setSourceContent(sourceContent);
        setTargetContent(targetContent);
        setSourceLang(sourceLang);
        setTargetLang(targetLang);
    }


    /**
     * Constrcutor for translation we get from translation api
     * @param sourceContent
     * @param targetContent
     * @param sourceLang
     * @param targetLang
     * @param match
     */
    public Translation( String sourceContent, String targetContent, String sourceLang, String targetLang, double match) {
        setSourceContent(sourceContent);
        setTargetContent(targetContent);
        setSourceLang(sourceLang);
        setTargetLang(targetLang);
        setMatch(match);
    }

    public Translation(String sourceContent, String targetContent, String sourceLang, String targetLang, double match, Date age) {
        setSourceContent(sourceContent);
        setTargetContent(targetContent);
        setSourceLang(sourceLang);
        setTargetLang(targetLang);
        setMatch(match);
        setAge(age);
    }

    /**
     * Constructor for translations from our database
     * @param id
     * @param sourceContent
     * @param targetContent
     * @param sourceLang
     * @param targetLang
     * @param match
     * @param age
     * @param requests
     */
    public Translation(int id, String sourceContent, String targetContent, String sourceLang, String targetLang, double match, Date age, int requests) {
        setId(id);
        setSourceContent(sourceContent);
        setTargetContent(targetContent);
        setSourceLang(sourceLang);
        setTargetLang(targetLang);
        setMatch(match);
        setAge(age);
        setRequests(requests);
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent){
        this.sourceContent = sourceContent;
    }

    public String getTargetContent() {
        return targetContent;
    }

    public void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMatch() {
        return match;
    }

    public void setMatch(double match) {
        this.match = match;
    }

    @JsonSerialize(using=JsonDateSerializer.class)
    public Date getAge() {
        return age;
    }

    public void setAge(Date age) {
        this.age = age;
    }

    @Override
    public String toString(){
        return "ID: " + this.getId() + System.lineSeparator()
                + "Content: " + this.getSourceContent() + System.lineSeparator()
                + "Translated content: " + this.getTargetContent() + System.lineSeparator()
                + "From: " + this.getSourceLang() + System.lineSeparator()
                + "To: " + this.getTargetLang() + System.lineSeparator()
                + "Match: " +this.getMatch();
    }


    public int getRequests() {
        return requests;
    }

    public void setRequests(int requests) {
        this.requests = requests;
    }
}
