package nl.vintik.workshop.aws.lambda.model

data class EventBridgeMessage <T> (
    var detail: T? = null,
    var region: String = "",
    var source: String = "",
    var time: String = ""
)
