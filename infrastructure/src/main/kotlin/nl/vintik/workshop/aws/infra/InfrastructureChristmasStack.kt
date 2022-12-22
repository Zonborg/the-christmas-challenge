package nl.vintik.workshop.aws.infra

import org.w3c.dom.Attr
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.appsync.alpha.*
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.events.EventBus
import software.amazon.awscdk.services.events.EventPattern
import software.amazon.awscdk.services.events.Rule
import software.amazon.awscdk.services.events.targets.LambdaFunction
import software.amazon.awscdk.services.lambda.Architecture
import software.amazon.awscdk.services.lambda.Code
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.lambda.Runtime
import software.amazon.awscdk.services.logs.RetentionDays
import software.constructs.Construct

class InfrastructureChristmasStack(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {
    init {
        val functionId = "christmas-lambda"
        val function = Function.Builder.create(this, functionId)
            .description("Kotlin Lambda for Christmas")
            .handler("nl.vintik.workshop.aws.lambda.KotlinLambda::handleRequest")
            .runtime(Runtime.JAVA_11)
            .code(Code.fromAsset("../build/dist/function.zip"))
            .architecture(Architecture.ARM_64)
            .logRetention(RetentionDays.ONE_WEEK)
            .memorySize(512)
            .timeout(Duration.seconds(120))
            .build()

        val eventBus = EventBus.Builder.create(this, "eventBus").eventBusName("ChristmasEventBus").build()

        Rule.Builder.create(this, "eventRule")
            .eventBus(eventBus)
            .eventPattern(EventPattern.builder()
                .source(listOf("Santa"))
                .build())
            .targets(listOf(LambdaFunction(function)))
            .build()

        val tableName = "Reindeer"
        val reindeerTable = Table.Builder.create(this,tableName)
            .tableName(tableName)
            .partitionKey(
                Attribute.builder()
                    .type(AttributeType.STRING)
                    .name("id")
                    .build()
            )
            .partitionKey(
                Attribute.builder()
                    .type(AttributeType.STRING)
                    .name("name")
                    .build()
            )
            .removalPolicy(RemovalPolicy.DESTROY)
            .pointInTimeRecovery(false)
            .billingMode(BillingMode.PROVISIONED)
            .readCapacity(12)
            .writeCapacity(12)
            .build()

        reindeerTable.grantWriteData(function)

        //Add GraphQL API to get Reindeers
        val apiName = "ReindeerApi"
        val reindeerApi = GraphqlApi.Builder.create(this, apiName)
            .name(apiName)
            .schema(SchemaFile.fromAsset(this::class.java.getResource("/schemas/reindeer.graphql")!!.path))
            .authorizationConfig(
                AuthorizationConfig.builder()
                    .defaultAuthorization(
                        AuthorizationMode.builder()
                            //API Key is the simplest authorisation option, good enough for our workshop
                            .authorizationType(AuthorizationType.API_KEY).build()
                    ).build()
            ).logConfig(
                LogConfig
                    .builder()
                    .fieldLogLevel(FieldLogLevel.ERROR)
                    .build()
            )
            .build()

        // This is a resolver definition for our GraphQL query
        reindeerApi.addDynamoDbDataSource("getReindeerById", reindeerTable).createResolver(
            "resolveById",
            BaseResolverProps.builder()
                .typeName("Query")
                .fieldName("getReindeerById")
                .requestMappingTemplate(MappingTemplate.dynamoDbGetItem("id", "id"))
                .responseMappingTemplate(MappingTemplate.dynamoDbResultItem())
                .build()
        )

        reindeerApi.addDynamoDbDataSource("getReindeerByName", reindeerTable).createResolver(
            "resolveByName",
            BaseResolverProps.builder()
                .typeName("Query")
                .fieldName("getReindeerByName")
                .requestMappingTemplate(MappingTemplate.dynamoDbGetItem("name", "name"))
                .responseMappingTemplate(MappingTemplate.dynamoDbResultItem())
                .build()
        )
    }
}