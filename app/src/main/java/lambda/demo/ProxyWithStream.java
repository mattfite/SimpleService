package lambda.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.Writer;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

/*
public class LambdaFunctionHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);

        // TODO: implement your handler
        return "Hello from Lambda!";
    }

}
*/

public class ProxyWithStream implements RequestStreamHandler {
    JSONParser parser = new JSONParser();


    @SuppressWarnings("unchecked")
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        String name = "you";
        String city = "World";
        String time = "day";
        String day = null;
        String responseCode = "200";

        try {
            JSONObject event = (JSONObject)parser.parse(reader);
            if (event.get("queryStringParameters") != null) {
                JSONObject qps = (JSONObject)event.get("queryStringParameters");
                if ( qps.get("name") != null) {
                    name = (String)qps.get("name");
                }
            }

            if (event.get("pathParameters") != null) {
                JSONObject pps = (JSONObject)event.get("pathParameters");
                if ( pps.get("proxy") != null) {
                    city = (String)pps.get("proxy");
                }
            }

            if (event.get("headers") != null) {
                JSONObject hps = (JSONObject)event.get("headers");
                if ( hps.get("day") != null) {
                    day = (String)hps.get("day");
                }
            }

            if (event.get("body") != null) {
                JSONObject body = (JSONObject)parser.parse(event.get("body").toString());
                if ( body.get("time") != null) {
                    time = (String)body.get("time");
                }
                if ( body.get("callerName") != null) {
                    name = (String)body.get("callerName");
                }
            }

            String greeting = "Good " + time + ", " + name + " of " + city + ". ";
            if (day!=null && day != "") greeting += "Happy " + day + "!";


            JSONObject responseBody = new JSONObject();
            responseBody.put("input", event);
            responseBody.put("message", greeting);

            JSONObject headerJson = new JSONObject();
            headerJson.put("x-custom-header", "my custom header value");

            responseJson.put("isBase64Encoded", false);
            responseJson.put("statusCode", responseCode);
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());

        } catch(ParseException pex) {
            responseJson.put("statusCode", "400");
            responseJson.put("exception", pex);
        }

        logger.log(responseJson.toString());
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }
}
