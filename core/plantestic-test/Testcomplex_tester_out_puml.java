package com.plantestic.test;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.http.ContentType;
import org.hamcrest.collection.IsIn;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.text.StringSubstitutor;
import com.moandjiezana.toml.Toml;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.atlassian.oai.validator.OpenApiInteractionValidator.ApiLoadException;

public class Testcomplex_tester_out_puml {

	Map<String, Object> paramsMap = new HashMap();
	Map<String, Filter> validationFilterMap = new HashMap();
	ScriptEngine engine;
	StringSubstitutor substitutor;
	boolean escape = false;

	// FIXME: no IS_WINDOWS please
	private static final boolean IS_WINDOWS = System.getProperty( "os.name" ).contains( "indow" );

	public Testcomplex_tester_out_puml(String configFile) throws Exception {
		try {
			String osAppropriatePath = IS_WINDOWS ? configFile.substring(0) : configFile;
			Path path = Paths.get(osAppropriatePath);

			paramsMap = unnestTomlMap("", new Toml().read(new String(Files.readAllBytes(path))).toMap());
			substitutor = new StringSubstitutor(paramsMap);
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
		} catch(Exception exception) {
			System.out.println("An Error occured, possible during reading the TOML config file: " + exception);
			throw exception;
		}
	}

    @Test
	public void test() throws Exception {
        if (!validationFilterMap.containsKey("B")){
            try {
                validationFilterMap.put("B",
                    new OpenApiValidationFilter(subst("${B.swagger_json_path}")));
            } catch(ApiLoadException e){
                System.out.println(e);
                validationFilterMap.put("B", new Filter() {
                    @Override
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        return ctx.next(requestSpec, responseSpec);
                    }
                });
            }
        }

        {
            
            try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
            Response roundtrip1 = RestAssured.given().contentType(ContentType.JSON)
                    .auth().basic(subst("${B.username}"), subst("${B.password}"))
                    .filter(validationFilterMap.get("B"))
                .when()
                    .post(subst("${B.path}") + subst("/hello"))
                .then()
                    .assertThat()
                        .statusCode(IsIn.isIn(Arrays.asList(200)))        
                    	.and().extract().response();
        }


        if (!validationFilterMap.containsKey("C")){
            try {
                validationFilterMap.put("C",
                    new OpenApiValidationFilter(subst("${C.swagger_json_path}")));
            } catch(ApiLoadException e){
                System.out.println(e);
                validationFilterMap.put("C", new Filter() {
                    @Override
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        return ctx.next(requestSpec, responseSpec);
                    }
                });
            }
        }

        if(eval("true")) {
            
            Response roundtrip2 = RestAssured.given().contentType(ContentType.JSON)
                    .auth().basic(subst("${C.username}"), subst("${C.password}"))
                    .filter(validationFilterMap.get("C"))
                .when()
                    .get(subst("${C.path}") + subst("/bye"))
                .then()
                    .assertThat()
                        .statusCode(IsIn.isIn(Arrays.asList(200)))        
                    	.and().extract().response();
        }


        if (!validationFilterMap.containsKey("C")){
            try {
                validationFilterMap.put("C",
                    new OpenApiValidationFilter(subst("${C.swagger_json_path}")));
            } catch(ApiLoadException e){
                System.out.println(e);
                validationFilterMap.put("C", new Filter() {
                    @Override
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        return ctx.next(requestSpec, responseSpec);
                    }
                });
            }
        }

        if(eval("false")) {
            
            Response roundtrip3 = RestAssured.given().contentType(ContentType.JSON)
                    .auth().basic(subst("${C.username}"), subst("${C.password}"))
                    .filter(validationFilterMap.get("C"))
                .when()
                    .get(subst("${C.path}") + subst("/bye"))
                .then()
                    .assertThat()
                        .statusCode(IsIn.isIn(Arrays.asList(400)))        
                    	.and().extract().response();
        }


        if (!validationFilterMap.containsKey("A")){
            try {
                validationFilterMap.put("A",
                    new OpenApiValidationFilter(subst("${A.swagger_json_path}")));
            } catch(ApiLoadException e){
                System.out.println(e);
                validationFilterMap.put("A", new Filter() {
                    @Override
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        return ctx.next(requestSpec, responseSpec);
                    }
                });
            }
        }

        if(eval("false")) {
            
            Response roundtrip4 = RestAssured.given().contentType(ContentType.JSON)
                    .auth().basic(subst("${A.username}"), subst("${A.password}"))
                    .filter(validationFilterMap.get("A"))
                .when()
                    .get(subst("${A.path}") + subst(""))
                .then()
                    .assertThat()
                        .statusCode(IsIn.isIn(Arrays.asList(200)))        
                    	.and().extract().response();
        }


        if (!validationFilterMap.containsKey("A")){
            try {
                validationFilterMap.put("A",
                    new OpenApiValidationFilter(subst("${A.swagger_json_path}")));
            } catch(ApiLoadException e){
                System.out.println(e);
                validationFilterMap.put("A", new Filter() {
                    @Override
                    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                        return ctx.next(requestSpec, responseSpec);
                    }
                });
            }
        }

        if(eval("true")) {
            
            try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
            Response roundtrip5 = RestAssured.given().contentType(ContentType.JSON)
                    .auth().basic(subst("${A.username}"), subst("${A.password}"))
                    .filter(validationFilterMap.get("A"))
                .when()
                    .get(subst("${A.path}") + subst(""))
                .then()
                    .assertThat()
                        .statusCode(IsIn.isIn(Arrays.asList(400)))        
                    	.and().extract().response();
        }
	}

    /// Helper method to make to templating in string variables above more clean.
	private String subst(String source) {
	    assert substitutor != null;
	    return substitutor.replace(source);
	}

	/// Helper method to make evaluation of conditions more clean.
	private boolean eval(String condition) throws ScriptException {
	    assert engine != null;
	    // First, run the templating engine over the condition.
	    // This is the whole reason why we do this "evaluate a JS string at runtime" at all.
	    // escape strings
	    escape = true;
	    String substCondition = subst(condition);
	    escape = false;
	    // Second, we can simply pipe the string through the JavaScript engine and get a result.
	    return (Boolean) engine.eval(substCondition);
	}

    /// Helper method to flatten the tree-like structure of a TOML file.
    /// Here, we use the path of an item as the key and the item itself as the value.
    /// The path of an item separated by dots, e.g. "A.B.item".
    private Map<String, Object> unnestTomlMap(String prefix, Map<String, Object> tree) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : tree.entrySet()) {
            String identifierPath = prefix + entry.getKey();
            if (entry.getValue() instanceof Map) {
                resultMap.putAll(unnestTomlMap(identifierPath + ".", (Map<String, Object>) entry.getValue()));
            } else {
                resultMap.put(identifierPath, parseJsonObject(entry.getValue()));
            }
        }
        return resultMap;
    }

    /// Helper method to parse JSON Objects.
    private Object parseJsonObject(Object o) {
        if (o instanceof String) {
            return new StringEscaper((String) o);
        } else {
            return o;
        }
    }

    /// Helper class to handle string escaping dynamically
    /// It obtains a String value and a reference to an Boolean that tells
    /// whether String needs escaping (i.e., eval) or not (i.e. path substitution).
    private class StringEscaper {
        String value;

        StringEscaper(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            if (escape) {
                return "\"" + value + "\"";
            } else {
                return value;
            }
        }
    }
}
