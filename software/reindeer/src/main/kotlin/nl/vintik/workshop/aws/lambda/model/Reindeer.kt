package nl.vintik.workshop.aws.lambda.model

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

@DynamoDbBean
data class Reindeer (
    @get:DynamoDbPartitionKey
    var id: String = "",
    var name: String = "",
    var speed: Int = 0,
    var skill: String? = null,
    var description: String? = null
)
{
    companion object ReindeerTable {
        private const val TABLE_NAME = "Reindeer"

        private val schema: TableSchema<Reindeer> = TableSchema.fromClass(Reindeer::class.java)

        private val dynamoDbAsyncClient: DynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
            .dynamoDbClient(
                DynamoDbAsyncClient.builder()
                    .region(Region.EU_WEST_1)
                    .build()
            ).build()

        val reindeerTable: DynamoDbAsyncTable<Reindeer> = dynamoDbAsyncClient.table(
            TABLE_NAME,
            schema
        )
    }

}