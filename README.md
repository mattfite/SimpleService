# Simple Service Reference project
This project should illustrate the concepts of Continuous Deployment for AWS Lambda 
functions using CodePipeline, CodeBuild and Java unit and integration testing
defined within the project. This example is capable of being deployed in the VCAS 
servier training area.

## AWS Example Lambda proxy integration (Java)
This project is using the [AWS Lambda Java proxy integration][lambda-example] example 
provided in AWS Documentation.

## Create the initial pipeline stack
To create the initial pipeline, issue the following command:
```
aws cloudformation create-stack \
    --stack-name simple-service-pipeline \
    --template-body file://deploy/pipeline_serverless.yml \
    --capabilities CAPABILITY_IAM \
    --parameters file://deploy/params.json


{
    "StackId": "arn:aws:cloudformation:us-west-2:112428243612:stack/simple-service-pipeline/c4aa2110-6826-11e8-88f0-50a68a2012ba"
}
```

### Params
I've used a JSON parameters file to make the stack easier to launch. The parameters are documented in 
```deploy/pipeline_serverless.yml``` but are also listed here for convenience:

```json
[
  {
    "ParameterKey": "Owner",
    "ParameterValue": "mattfite"
  },
  {
    "ParameterKey": "OAuthToken",
    "ParameterValue": "3xxxxgetxxanxOAuthxxxTokenxxxxxxxxxxxxxx"
  },
  {
    "ParameterKey": "RepositoryName",
    "ParameterValue": "SimpleService"
  },
  {
    "ParameterKey": "BranchName",
    "ParameterValue": "master"
  }

]

```

## Update the pipeline stack
If you need to change the pipeline (e.g., any of the commands in the BuildSpec), it's best
to:
- make your changes locally
- run this cloudformation update-stack command
- then commit and push your changes

```
    aws cloudformation update-stack \
        --stack-name simple-service-pipeline \
        --template-body file://deploy/pipeline_serverless.yml \
        --capabilities CAPABILITY_IAM
```

[go watch the newly created or updated CodePipeline]

## Query Deployment stack
This shows a bash-style query to test the endpoint from the command-line.

```bash
STACK="simple-service-pipeline-production"

name=$(aws cloudformation describe-stacks \
    --stack-name $STACK \
    --query 'Stacks[0].Outputs[?OutputKey==`URL`]'.OutputValue \
    --output text)


http POST \
  $name/Seattle \
  time==evening \
  content-type:application/json \
  day:Thursday \
  x-amz-docs-region:us-west-2 \
  callerName=John
```

Example:

    "https://55ll302c7d.execute-api.us-west-2.amazonaws.com:443/Prod/Seattle"


### Gradle version
This project uses gradle 4.6 locally (required for JUnit 5 native Gradle integration), but 
can be configured to use Gradle version 4.3 (available in ```aws/codebuild/java:openjdk-9```
Docker container (CodeBuild).

## Local gradle version
    gradle wrapper --gradle-version 4.6
## CodeBuild gradle version
    gradle wrapper --gradle-version 4.3

Test reports don't work with gradle 4.3. They only seem to work with 4.6.

## BuildSpec
The BuildSpec files defined in the pipeline yaml define the build 'rules'. These
can be modified, or adjusted so that we have common build targets and practices. When 
we have developed the ability to download the ```build.gradle``` file from a file share,
one improvement may be to change the ```cp build.gradle-4.3 build.gradle``` BuildSpec 
command as follows:
```
    curl -o build.gradle http://gradle.example.com/services/build-targets/build.gradle
```

It is also possible to modify the pipeline so that the BuildSpec is included within 
the product source, in a file named ```buildspec.yml``` so that one doesn't have to 
update the pipeline CloudFormation stack in order to change the BuildSpec.

Keeping the BuildSpec in the pipeline seems like a reasonable choice to encourage
consistency within the services that we deploy.

## Gradle tasks
- test
- shadowJar (used to build uber jar) hope to use for SAM local testing
- copy2 (FIXME: bad name) used to create unzipped artifacts (third part .jar and .class) 
  and directory layout. SAM CFn stage will zip artifacts for Lambda deployment.

    cd app && ./gradlew clean test shadowJar

[only works with gradle 4.6. JUnit native support in gradle 4.6]
open app/build/reports/tests/test/index.html

    cd acceptance && ORG_GRADLE_PROJECT_endpoint=https://ta80n0v4og.execute-api.us-west-2.amazonaws.com/Prod ./gradlew clean test

[only works with gradle 4.6. JUnit native support in gradle 4.6]
open acceptance/build/reports/tests/test/index.html

## Monitoring
The CloudFormation stack that is created in ```infrastructure/serverless.yml``` creates 
alerts on CloudWatch metrics available to Lambda:
- Errors
- Throttles

[TODO] Create additional CloudWatch alerts for
- Duration
- Invocations
- DLQ Errors
- Iterator Age

## Best Practices
- [Lambda Best Practices][best]

## Testing
### Unit Testing
Unit testing is with [JUnit][junit-user] (```cd app/ && ./gradlew test```). The application 
has minimal testing, but derivative applications should aim higher. The test reports can be 
viewed after run, at ```app/build/reports/tests/test/index.html```.
### Integration (acceptance) Testing
Integration testing is performed with [Rest-assured][rest-assured], a Java integration test
framework that makes it easy to perform tests against a running REST endpoint.

Example

```java
    @Test
    public void whenRequestPostWithLogs_thenOK(){
        given().log().all().
            contentType("application/json").
            header("day", "Thursday").
            header("x-amz-docs-region", "us-west-2").
            body("{ \"callerName\": \"John\"}").
        when().
            post("/Seattle").
        then().
            assertThat().statusCode(200);
    }
```

## Improvements
- Create reference implementation for Lambda/API Gateway without proxy integration
- Update reference implementation to work with SAM Local
    - [SAM Local GitHub][samlocal]
    - [SAM Local AWS Documentation][samlocal-docs]
    - [Docker Lambda GitHub][docker-lambda]
- Update reference implemenation to promote the use versions and aliases
    - [Lambda Versioning and Aliases -- AWS Documentation][version]
- [Lambda in production][prod-lambda]
- Instrument the example with [AWS X-Ray][xray]

[xray]: https://docs.aws.amazon.com/xray/latest/devguide/aws-xray.html "AWS X-Ray"
[lambda-example]: https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-create-api-as-simple-proxy-for-lambda.html "AWS Java Lambda example"
[rest-assured]: http://rest-assured.io "REST-assured"
[junit-user]: https://junit.org/junit5/docs/current/user-guide/ "JUnit 5 User Guide"
[version]: https://docs.aws.amazon.com/lambda/latest/dg/versioning-aliases.html "Lambda Versioning and Aliases"
[best]: https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html "Lambda Best Practices"
[samlocal]: https://github.com/awslabs/aws-sam-cli "SAM Local"
[samlocal-docs]: https://docs.aws.amazon.com/lambda/latest/dg/test-sam-cli.html "AWS SAM Local Documentaiton"
[docker-lambda]: https://github.com/lambci/docker-lambda "Lambda Docker Images"
[prod-lambda]: https://www.concurrencylabs.com/blog/how-to-operate-aws-lambda/ "Lambda in production"
