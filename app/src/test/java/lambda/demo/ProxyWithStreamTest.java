package lambda.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ProxyWithStreamTest {

    private static InputStream input;
    private static OutputStream ostream;

    @BeforeAll
    public static void createInput() throws IOException {
        // set up your sample input object here.
        JSONObject body = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("callerName", "John");
        body.put("body", name );
        input = new ByteArrayInputStream(body.toString().getBytes(StandardCharsets.UTF_8));
        ostream = new ByteArrayOutputStream();
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testLambdaFunctionHandler() {
        ProxyWithStream handler = new ProxyWithStream();
        Context ctx = createContext();

        try {
            handler.handleRequest(input, ostream, ctx);
        }
        catch(IOException e) {
            // handle exception
            System.err.println("Caught IOException: " + e.getMessage());
        }

        // validate output here if needed.
        JSONObject result = new JSONObject(ostream.toString());

        Assertions.assertEquals(result.get("statusCode"), "200");
    }
}
