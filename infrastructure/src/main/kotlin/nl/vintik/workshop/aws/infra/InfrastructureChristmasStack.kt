package nl.vintik.workshop.aws.infra

import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
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
    }


}