package nl.vintik.workshop.aws.lambda.model

data class Reindeer (
    var id: String = "",
    var name: String = "",
    var speed: Int = 0,
    var skill: String? = null,
    var description: String? = null
)