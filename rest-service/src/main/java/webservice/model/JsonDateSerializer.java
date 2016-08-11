package webservice.model;


import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class, so that the age of translations and user is correctly formatted returned in responses from the webservice
 * Created by alexanderweiss on 13.03.16.
 */
@Component
public class JsonDateSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Date> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    @Override
    public void serialize(Date date, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonProcessingException {
        String formattedDate = dateFormat.format(date);
        jsonGenerator.writeString(formattedDate);
    }
}