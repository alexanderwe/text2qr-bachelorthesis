package model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexanderweiss
 * Class for representing an translation. Simply a DTO.
 */
public class Translation {

    private JSONObject json_representation;
    private int id;
    private String sourceContent;
    private String targetContent;
    private String sourceLang;
    private String targetLang;
    private Double match;


    public Translation(JSONObject json_representation){
        this.json_representation = json_representation;
        try{
            init();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

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
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
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
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    /**
     * Constructor for translation which we get from our webservice
     * @param id
     * @param sourceContent
     * @param targetContent
     * @param sourceLang
     * @param targetLang
     * @param match
     */
    public Translation( int id,String sourceContent, String targetContent, String sourceLang, String targetLang, double match) {
        setId(id);
        setSourceContent(sourceContent);
        setTargetContent(targetContent);
        setSourceLang(sourceLang);
        setTargetLang(targetLang);
        setMatch(match);
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    /**
     * Initialize the json_respresentatation of this translation
     * @throws JSONException
     */
    private void init() throws JSONException{
        JSONObject translation = new JSONObject();
        translation.put("sourceContent", getSourceContent());
        translation.put("targetContent",getTargetContent());
        translation.put("sourceLang", getSourceLang());
        translation.put("targetLang", getTargetLang());
        translation.put("match", getMatch());
        translation.put("id", getId());
        this.json_representation = translation;
    }

    public JSONObject getJson_representation() {
        return json_representation;
    }

    private void setJson_representation(JSONObject json_representation) {
        this.json_representation = json_representation;
    }

    public String getSourceContent() {
        return sourceContent;
    }

    public void setSourceContent(String sourceContent) {
        this.sourceContent = sourceContent;
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    public String getTargetContent() {
        return targetContent;
    }

    public void setTargetContent(String targetContent) {
        this.targetContent = targetContent;
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    public String getSourceLang() {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
        this.sourceLang = sourceLang;
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    public String getTargetLang() {
        return targetLang;
    }

    public void setTargetLang(String targetLang) {
        this.targetLang = targetLang;
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
    }

    public Double getMatch() {
        return match;
    }

    public void setMatch(Double match) {
        this.match = +match;
        try{
            init();
        }catch(JSONException jsone){
            jsone.printStackTrace();
        }
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

    public int getId() {
        return id;

    }

    public void setId(int id) {
        this.id = id;
        try {
            init();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
