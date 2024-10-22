package mego.tests;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import mego.config.Config;

import static io.restassured.RestAssured.given;

public class BaseSpec {

    protected static final Config CFG = Config.getInstance();

    public static BaseSpec spec() {
        return new BaseSpec();
    }

    public RequestSpecification request() {
        return given()
                .baseUri(CFG.apiUrl())
                .contentType(ContentType.JSON)
                .log().uri()
                .log().body()
                .when();
    }

}