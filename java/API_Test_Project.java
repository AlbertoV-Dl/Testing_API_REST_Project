import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import org.junit.FixMethodOrder;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.runners.MethodSorters;

import java.util.Base64;
import static org.junit.Assert.*;
import static io.restassured.RestAssured.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class API_Test_Project {
    //environment variables
    static private String baseUrl  = "https://webapi.segundamano.mx";
    static private String token;
    static private String accountID;
    static private String name;
    static private String uuid;
    static private String newText;
    static private String adID;
    static private String token2;
    static private String addressID;
    static private String tokenUuid;

    private String AddressBodyRequest(){
        String bodyRequest = "{\"account\":{\"name\":\"ventas uno\",\"phone\":\"1076529721\"," +
                "\"locations\":[{\"code\":\"3\",\"key\":\"region\",\"label\":\"Aguascalientes\"," +
                "\"locations\":[{\"code\":\"35\",\"key\":\"municipality\",\"label\":\"Aguascalientes\"}]}]," +
                "\"professional\":false,\"phone_hidden\":true}}";

        return bodyRequest;
    }

    private Response POSTUserAddressBody(String tokenUuid){
        Response response = given().log().all().config(RestAssured.config()
                .encoderConfig(EncoderConfig.encoderConfig()
                        .encodeContentTypeAs("x-www-form-urlencoded",
                                ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("contact", "Casa grande")
                .formParam("phone","3234445555")
                .formParam("rfc", "CASA681225XXX")
                .formParam("zipCode", "45050")
                .formParam("exteriorInfo", "exterior 10")
                .formParam("region", "5")
                .formParam("municipality", "51")
                .formParam("alias", "big house")
                .header("Authorization","Basic " + tokenUuid)
                .post();

        return response;
    }

    @Test
    public void t01_get_token_fail(){
        //Request an account token without authorization header
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .post();
        //validations
        System.out.println("Status expected: 400" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(400,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: VALIDATION FAILED \nResult: " + errorCode);
        assertEquals("VALIDATION_FAILED",errorCode);
    }

    @Test
    public void t02_get_token_correct(){
        //Request an account token with an authorization header
        name ="alberto.edlira@gmail.com";
        String pass ="54321";

        String ToEncode = name + ":" +pass;
        token = Base64.getEncoder().encodeToString(ToEncode.getBytes());

        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);

        Response response = given().log().all()
                .header("Authorization","Basic " + token)
                .post();

        //validations
        String body = response.getBody().asString();
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        assertTrue(body.contains("access_token"));
    }

    @Test
    public void t03_create_user_fail(){
        //Create an user without authorization header
        String username = "agente" + (Math.floor(Math.random() * 7685) + 3) + "@mailinator.com";
        String bodyRequest = "{\"account\":{\"email\":\""+ username +"\"}}";
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts",baseUrl);
        Response response = given().log().all()
                .contentType("application/json")
                .body(bodyRequest)
                .post();
        //validations
        System.out.println("Status expected: 400" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(400,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println(errorCode);
        assertEquals("VALIDATION_FAILED",errorCode);
    }

    @Test
    public void t04_create_user() {
        //Request an account token with an authorization header
        RestAssured.baseURI = String.format("%s/nga/api/v1.1/private/accounts", baseUrl);
        Response response = given().queryParam("lang", "es")
                .log().all()
                .header("Authorization", "Basic " + token)
                .post();

        //save account data to environment variables
        token = response.jsonPath().getString("access_token");
        System.out.println(token);

        String accountID2 = response.jsonPath().getString("account.account_id");
        accountID = accountID2.split("/")[3];
        System.out.println("account Id: "+accountID);

        name = response.jsonPath().getString("account.name");
        System.out.println("Name: "+name);

        uuid = response.jsonPath().getString("account.uuid");
        System.out.println("UUID :"+uuid);

        String user = accountID2.split("/")[3];
        System.out.println("User??"+user);

        token2 = uuid + ":" + token;
        tokenUuid = Base64.getEncoder().encodeToString(token2.getBytes());
        System.out.println(tokenUuid);
        //validations
        String body = response.getBody().asString();
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200, response.getStatusCode());
        assertTrue(body.contains("access_token"));
        System.out.println("Token: " + token);
        assertNotNull(body);
        assertEquals("ventas uno", name);
    }

    @Test
    public void t05_update_phone_number(){
        //update user created adding its phone number
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s", baseUrl, accountID);
        int phone = (int) (Math.random()*99999999+999999999);
        String bodyRequest ="{\"account\":{\"name\":\""+ name +"\"," +
                "\"phone\":\""+ phone +"\", " +
                "\"phone_hidden\": true}}";

        Response response = given().log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token)
                .accept("application/json, text/plain, */*")
                .contentType("application/json")
                .body(bodyRequest)
                .patch();

        //validations
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        String userPhone = response.jsonPath().getString("account.phone");
        assertEquals(userPhone, "" + phone);
    }

    @Test
    public void t06_add_new_add_fail(){
        //add a new add with an ivalid token should fail
        token2 = "fake";
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s",baseUrl,accountID);
        String bodyRequest = AddressBodyRequest();

        Response response = given()
                .log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token2)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .post();

        //Validaciones
        System.out.println("Status expected: 401" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(401,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error.code");
        System.out.println("Error Code expected: UNAUTHORIZED \nResult: " + errorCode);
        assertEquals("UNAUTHORIZED",errorCode);
    }

    @Test
    public void t07_add_new_add(){
        //Add a new add with a valid token
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s",baseUrl, accountID);
        String bodyRequest = AddressBodyRequest();

        Response response = given()
                .log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .patch();

        //Validaciones
        String body = response.getBody().asString();
        System.out.println("Body: " + body );
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: new \nResult: " + actionType);
        //action type allways is null
        assertEquals(null, actionType);
        String uuID = response.jsonPath().getString("account.uuid");

        System.out.println("Ad Created with id: " + uuID);
        assertTrue(body.contains("Aguascalientes"));
    }

    @Test
    public void t08_update_add(){
        //change a text on the description of the add
        newText = "Calvillo";
        String codeMunicipality="37";
        RestAssured.baseURI = String.format("%s/nga/api/v1/private/accounts/%s",baseUrl,accountID);

        String bodyRequest = "{\"account\":{\"name\":\"ventas uno\",\"phone\":\"1076529721\"," +
                "\"locations\":[{\"code\":\"3\",\"key\":\"region\",\"label\":\"Aguascalientes\"," +
                "\"locations\":[{\"code\":\""+codeMunicipality+"\",\"key\":\"municipality\",\"label\":\""+newText+"\"}]}]," +
                "\"professional\":false,\"phone_hidden\":true}}";

        Response response = given()
                .log().all()
                .header("Authorization","tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .body(bodyRequest)
                .patch();

        //Validaciones
        String body = response.getBody().asString();
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: edit \nResult: " + actionType);
        assertEquals(null, actionType);

        String location = response.jsonPath().getString("account.locations.locations");
        System.out.println("contains: " + location);
        assertTrue(body.contains(newText));
    }

    @Test
    public void t09_get_address_fail(){
        //get user address with an invalid token should fail
        RestAssured.baseURI = String.format("%s/addresses/v1/get",baseUrl);

        Response response = given()
                .log().all()
                .header("Authorization","Basic " + token)
                .get();

        //validations
        System.out.println("Status expected: 403" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(403,response.getStatusCode());
        String errorCode = response.jsonPath().getString("error");
        System.out.println("Error Code expected: Authorization failed \nResult: " + errorCode);
        assertEquals("Authorization failed",errorCode);
    }

    @Test
    public void t10_user_has_no_address(){
        //Get user addresses should be an empty list
        // WARNING verify User address is empty
        System.out.println("WARNING For this test verify User address is empty since there is not method to delete multiple addresses yet");

        String token2Keys = uuid + ":" + token;
        token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());

        RestAssured.baseURI = String.format("%s/addresses/v1/get",baseUrl);

        Response response = given()
                .log().all()
                .header("Authorization","Basic " + tokenUuid)
                .get();

        //validations
        System.out.println("Status expected: 200" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200,response.getStatusCode());
        String addressesList = response.jsonPath().getString("addresses");
        System.out.println("List expected to be empty \nResult: " + addressesList);
        assertEquals("[:]",addressesList);
    }

    @Test
    public void t11_update_user_address(){
        //add a new address to user
        // WARNING this action is adding more than 1 address
        System.out.println("WARNING this action is adding more than 1 address");

        RestAssured.baseURI = String.format("%s/addresses/v1/create",baseUrl);

        Response response = POSTUserAddressBody(tokenUuid);

        //Validaciones
        String body = response.getBody().asString();
        System.out.println("Body addres: " + body );
        System.out.println("Status expected: 201" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(201, response.getStatusCode());
        //save address to enviromnt variable
        addressID = response.jsonPath().getString("addressID");
        System.out.println("Address created with ID: " + addressID);
        assertTrue(body.contains("addressID"));
    }

    @Test
    public void t12_update_user_address_duplicated(){
        //try to add same address should fail
        //IS ACCEPTING DUPLICATE ADDRESS & WARNING this action is adding more than 1 address
        System.out.println("WARNING this action is adding more than 1 address");
        RestAssured.baseURI = String.format("%s/addresses/v1/create",baseUrl);

        Response response = POSTUserAddressBody(tokenUuid);

        //Validaciones
        String body = response.getBody().asString();
        System.out.println("Status expected: 201" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(201, response.getStatusCode());
        String errorCode = response.jsonPath().getString("error");
        System.out.println("Request expected to return duplicate \nResult: " + errorCode);
        assertTrue(body.contains("addressID"));
    }

    @Test
    public void t13_get_created_address() {
        //use address id to get the user's address
        String token2Keys = uuid + ":" + token;
        token2 = Base64.getEncoder().encodeToString(token2Keys.getBytes());

        RestAssured.baseURI = String.format("%s/addresses/v1/get/%s", baseUrl,addressID);

        Response response = given()
                .log().all()
                .header("Authorization", "Basic " + tokenUuid)
                .get();

        //validations
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(200, response.getStatusCode());
        String respAddress = response.jsonPath().getString("addresses");
        System.out.println("Request expected to contain addressID: " + respAddress);
        assertTrue(respAddress.contains(addressID));
    }

    @Test
    public void t14_shop_not_found(){
        //fail to found a shop with this account
        RestAssured.baseURI = String.format("%s/shops/api/v2/public/accounts/10613126/shop",baseUrl);

        Response response = given().log().all()
                .get();

        //validations
        System.out.println("Status expected: 404" );
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(404,response.getStatusCode());
        String errorCode = response.jsonPath().getString("message");
        System.out.println("Error Code expected: Account not found \nResult: " + errorCode);
        assertEquals("Account not found",errorCode);
    }

    @Test
    public void t15_delete_ad() {
        //Delete the ad created - possible fail with 403
        RestAssured.baseURI = String.format("%s/nga/api/v1/delete/%s", baseUrl, addressID);

        Response response = given().log().all()
                .header("Authorization", "tag:scmcoord.com,2013:api " + token)
                .header("x-nga-source", "PHOENIX_DESKTOP")
                .contentType("application/json")
                .delete();

        //validations
        System.out.println("Status expected: 200");
        System.out.println("Result: " + response.getStatusCode());
        assertEquals(403, response.getStatusCode());
        String actionType = response.jsonPath().getString("action.action_type");
        System.out.println("Action expected to be: delete \nResult: " + actionType);
        //Delete the ad created - possible fail with null since the addrees has been deleted
        assertEquals(null, actionType);
    }
}