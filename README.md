# undergraduate-thesis-neo4j
The Database Scheduler monitors transaction actions using Validation-based Concurrency Control.
## using the Customized Graph Database Service

To access the graph database service, we use the GraphDatabaseService class as taught in the [neo4j tutorial](https://neo4j.com/docs/java-reference/current/java-embedded/hello-world/). However, in our implementation, we use a customized version that has an embedded database monitor:
```
private CustomGraphDatabaseService customGraphDb;
private CustomDatabaseManagementService customManagementService;
```
To initialize these functions, use the following code:
```
customManagementService = new CustomDatabaseManagementService(new DatabaseManagementServiceBuilder(databaseDirectory).build());
customGraphDb = customManagementService.database(DEFAULT_DATABASE_NAME);
```
After initialization, you can use these functions as shown in the following example:
```
CustomTransaction Tx = customGraphDb.beginTx()
ResourceIterable<Node> Nodes = Tx.getAllNodes();
```
This is similar to using any Neo4j Java code.
## code structure
The `GraphDatabaseMonitor` is responsible for monitoring the data accessed (read, write) by every transaction. The monitor is implemented as an extension of the `TransactionEventListener<TransactionInformation>` interface, which acts as a hook triggered before or after the transaction commits or rolls back.

The GraphDatabaseMonitor holds a `Map<Integer, TransactionInformation> transactionDataMap` that stores all the transaction data, including the transaction ID, timestamp (start, end, and before-commit), and data accessed by each transaction. In Neo4j 5.x, we no longer have access to the `transactionId`, so the hashcode for each transaction is used as its unique reference.

For non-read-only transactions, we can obtain the data modified by using the beforeCommit function. This function takes a TransactionData object as input, which contains detailed logs of the modified data. We can upload this information to the TransactionDataMap, and then call the Validation test to perform actions (rollback/commit) according to the test result.
For read-only transactions, the `TransactionEventListener` cannot be triggered before the transaction commits because no changes are going to be submitted to the database. Therefore, we need to trigger the validation test in the `customTx.commit()` function.

## validation test method
to be continued :P
