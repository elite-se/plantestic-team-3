# Plantestic - V2
This project is a continuation and extension of the [plantestic project](https://github.com/FionaGuerin/plantestic).
This README gives an overview of plantestic's core features and of the new features we added.

##Contents
* [Description]('#Description')
* [Motivation]('#Motivation')
* [Core Features (V1)]('#Motivation')
* [Extension Features (V2, new in this repo)]('#Motivation')
* [Installation]('#Installation')
* [Usage]('#Usage')
* [Limitations]('#Limitations')
* [Credits]('#Credits')


## Description
The test case generator Plantestic produces test cases from a sequence diagram. 
A sequence diagram models a sequence of interactions between objects. 
A test case then checks for such an interaction whether it is implemented as the sequence diagram defines it. 
In an example sequence diagram called `Hello`, let there be two actors Alice and Bob. 
Alice sends Bob the request `GET /hello ` and Bob answers with `Hello World`. 
The corresponding test case now sends an HTTP request `GET /hello` to the backend. 
The test case then expects a response with status `200 OK` and date `Hello World`.

![./core/src/test/resources/minimal_hello.png](./core/src/test/resources/minimal_hello.png)

```
public void test() throws Exception {
		try {
			Response roundtrip1 = RestAssured.given()
					.auth().basic(substitutor.replace("${B.username}"), substitutor.replace("${B.password}"))
				.when()
					.get(substitutor.replace("${B.path}") + substitutor.replace("/hello"))
				.then()
					.assertThat()
					    .statusCode(IsIn.isIn(Arrays.asList(200)));
		} catch (Exception exception) {
			System.out.println("An error occured during evaluating the communication with testReceiver: ");
			exception.printStackTrace();
			throw exception;
		}
	}
```

## Motivation
The implementation of user requirements often deviates from the specification of the same user requirements. 
Individual work, teamwork, and collaboration between teams can produce such a divergence. 
For example, requirements may be misinterpreted or overlooked. 
Teamwork, especially with multiple teams, causes interface errors. 
For example, subsystems of the same product may use conflicting technologies or conflicting data formats.

Our test case generator detects deviations at an early stage: 
The test case generator derives test cases directly from the specification. 
If the implementation fulfills these test cases, then the implementation fulfills the specification. 
If the implementation does not fulfill these test cases, the implementation deviates from the specification. 
With our test case generator, developers can quickly uncover inconsistencies, fix them, and save costs.## Demo

## Core Features
* gradle-based. Runs in any IDE
* requires only Java to be installed
* single line user friendly CLI
* powerful condition evaluation evaluates JavaScript conform conditions within sequence diagrams
* parameters can be passed into sequence diagrams using external `.toml` config files and templating

## Extension Features (V2, new in this repo)
* Restructure sequence diagrams to inspect only interactions of a specific actor. This enables users to have fewer,
 more granular sequence diagrams and automatically generate specific sequence diagrams that test only the interactions of one actor.
* Annotate requests to be delayed during the testcase execution. This enables the user to test a endpoint

## Installation
1. Install Java SE Development Kit 8 or higher. 
You can find Java SE Development Kit 8 under the website [https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
2. Clone the Plantestic repository.
3. Run git submodule init and git submodule update.
4. Install maven (if not already installed).
5. `cd openapi-metamodel/openapi2` and run `mvn install`.
6. `cd plugins/edu.uoc.som.openapi2.io` and run `mvn install`.
7. `cd ../edu.uoc.som.openapi2.mm` and run `mvn install`.
8. go back to the project root directory
9. Run `./gradlew build`.

## Usage
### Input requirements
The input is a PlantUML sequence diagram. 
This sequence diagram contains several participants and interactions between the participants. 
One participiant is the client who calls the test cases. The other participants are services of the implementation. 
In the example diagram, the client is `CCC` and the services are `CRS` and `Voicemanager`.

An interaction contains a request from the client and a response from a service. 
A request contains an HTTP method, a URL, and possibly parameters. 
A response contains an HTTP status code and, if applicable, data. A hyphen separates the HTTP status codes and the data. 

The HTTP method is `GET`, `POST`, or `PUT`. 

The URL path is a String. In it, slashes separate the path components. 
A parameter name in curly braces, preceded by a dollar symbol, thereby specifies parameterized path components. 
Example: ```/path/${param}```

We specify the request parameters in a tuple: 
An opening bracket is followed by the first request parameter. 
This request parameter is followed - comma-separated - by the second request parameter, and so on. 
The last request parameter is followed by a closing bracket.
We specify the request parameter as a `Name: Value` pair: 
The name of the request parameter is followed by a space, a colon, a space, and the value of the request parameter as a string. 
We define the value of the request parameter in curly brackets, preceded by a dollar symbol. 
Example: ```(name1 : "${value1}", name2 : "${value2}")```

We specify the response data in a tuple: 
An opening bracket is followed by the first response datum. 
This response datum is followed - comma-separated - by the second response datum, and so on. 
The last response datum is followed by a closing bracket.
We specify the response datum as a `Name: XPath` pair: 
The name of the response datum is followed by a space, a colon, a space, and the xpath of the response datum as a string. 
In the xpath, slashes separate the path components. . 
Example: ```(name1 : "/value/value1", name2 : "/value2")```

![./core/src/test/resources/rerouting.png](./core/src/test/resources/rerouting.png)

### Execution
#### Via the CLI
1. Create a PlantUML sequence diagram. Note the input requirements above. 
2. Save the sequence diagram. 
3. Call the command `./gradlew run --args="--input=<path/to/sequence/diagram/diagram_name.puml>"`.
4. The generated test cases are in `<path/to/sequence/diagram/generatedCode/<diagramName>.java>`.

Preprocessing (i.e. Swagger Extraction and tester generation) can also be triggered via the CLI.

* Swagger extraction: `./gradlew run --args="-p --input=<xyz.puml> --config=<xyz_conf.toml>"`
* Tester generation: `./gradlew run --args="-p --input=<xyz.puml> --tester=A"`
* Both preprocessing steps: `./gradlew run --args="-p --input=<xyz.puml> --tester=A --config=<xyz_conf.toml>"`
* Starting the server, which serves the GUI `./gradlew run --args="-s"`

The `-s` flag can always be appended to  any call and starts the server in the background.
All other steps (preprocessing or entire pipeline) are executed and the server continues running until the process is actively killed.

##### Example
Take the following test case generation from a minimal sequence diagram as an example:

1. You can find the sequence diagram `minimal_hello.puml` in the Plantestic project under `./core/src/test/resources/minimal_hello.puml`:

![./core/src/test/resources/minimal_hello.png](./core/src/test/resources/minimal_hello.png)

2. In the Plantestic console, call `./gradlew run --args="--input=./core/src/test/resources/minimal_hello.puml"`.
This will generate test cases for the provided diagram.

3. You will find the test case in the Plantestic project under `./core/build/resources/main/code-generation/generatedCode/minimal_hello_puml.java`:

```
package com.plantestic.test;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.hamcrest.collection.IsIn;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.text.StringSubstitutor;
import com.moandjiezana.toml.Toml;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Test {

	Map<String, Object> paramsMap = new HashMap();
	ScriptEngine engine;
	StringSubstitutor substitutor;
	private static final boolean IS_WINDOWS = System.getProperty( "os.name" ).contains( "indow" );

	public Test(String configFile) throws Exception {
		try {
			String osAppropriatePath = IS_WINDOWS ? configFile.substring(1) : configFile;
			Path path = Paths.get(osAppropriatePath);
			String paramsFileContent = new String(Files.readAllBytes(path));
			paramsMap = unnestTomlMap(new Toml().read(paramsFileContent).toMap());
			substitutor = new StringSubstitutor(paramsMap);
			ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName("JavaScript");
		} catch(Exception exception) {
			System.out.println("An Error occured, possible during reading the TOML config file: " + exception);
			throw exception;
		}
	}

	public void test() throws Exception {
		try {
			Response roundtrip1 = RestAssured.given()
					.auth().basic(substitutor.replace("${B.username}"), substitutor.replace("${B.password}"))
				.when()
					.get(substitutor.replace("${B.path}") + substitutor.replace("/hello"))
				.then()
					.assertThat()
					    .statusCode(IsIn.isIn(Arrays.asList(200)));
		} catch (Exception exception) {
			System.out.println("An error occured during evaluating the communication with testReceiver: ");
			exception.printStackTrace();
			throw exception;
		}
	}

	public static Map<String, Object> unnestTomlMap(Map<String, Object> tomlMap) {
		Map<String, Object> resultMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : tomlMap.entrySet()){
			if(entry.getValue() instanceof Map){
				Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();
				for (Map.Entry<String, Object> nestedEntry : innerMap.entrySet()){
					resultMap.put(entry.getKey() + "." + nestedEntry.getKey(), nestedEntry.getValue());
				}
			} else {
				resultMap.put(entry.getKey(), entry.getValue());
			}
		}
		return resultMap;
	}
}
```

## Limitations
- When actor A sends actor B a request, Plantestic expects actor B to send actor A a response. 
    Actors A and B must be different actors.
- Plantestic neither supports options nor loops.
- Plantestic can handle alternatives as long as they are not nested.
- We only support authenticated requests with username and password.

## Credits
### Contributors (V2)
- Jorge Quintero
- Tobias Schmidt
- Paula Wikidal
- Fabian Wildgrube

### Contributors (V1)
- [Stefan Grafberger](https://github.com/stefan-grafberger) *
- [Fiona Guerin](https://github.com/FionaGuerin) *
- [Michelle Martin](https://github.com/MichelleMar) *
- [Daniela Neupert](https://github.com/danielaneupert) *
- [Andreas Zimmerer](https://github.com/Jibbow) *

\* contributed equally

### Repositories
#### plantuml-eclipse-xtext
The repository [plantuml-eclipse-xtext](https://github.com/Cooperate-Project/plantuml-eclipse-xtext) defines the grammar of PlantUML. 
We pass this grammar to Xtext.
   
#### qvto-cli
The repository [qvto-cli](https://github.com/mrcalvin/qvto-cli) demonstrates how QVT operations can be performed without Eclipse.
   
### Literature
#### Standalone Parsing with Xtext
From the article [Standalone Parsing with Xtext](http://www.davehofmann.de/different-ways-of-parsing-with-xtext/) we learned how to use Xtext without Eclipse.
    
#### QVTOML/Examples/InvokeInJava
From the Wiki article [QVTOML/Examples/InvokeInJava](https://wiki.eclipse.org/QVTOML/Examples/InvokeInJava) we learned how to call QVT from our pipeline.

#### Grammar-based Program Generation Based on Model Finding
From the paper [Grammar-based Program Generation Based on Model Finding](http://www.informatik.uni-bremen.de/agra/doc/konf/13_idt_program_generation.pdf) we learned about the Eclipse Modeling Framework.
   
## License
Copyright [2019] [Stefan Grafberger, Fiona Guerin, Michelle Martin, Daniela Neupert, Andreas Zimmerer]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
