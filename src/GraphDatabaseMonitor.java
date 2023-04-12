import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionEventListener;
import org.neo4j.graphdb.Node;
public interface GraphDatabaseMonitor extends TransactionEventListener<TransactionInformation>{
    /**
     * when connect to database, this function will be called.
     * this function will only be called once because now we only focus in one database.
     */
    public void connect(String DatabaseName);

    /**
     * in database when one transaction is created,
     * this function will be called to log the data related to this function
     * @param Tx the transaction created
     */
    public void startTransaction(CustomTransaction Tx);

    /**
     * the transaction has been committed successfully.
     * now we need to delete its data in transactionDataMap
     * @param transactionId the hashCode() for the committed transaction
     */
    public void endTransaction(Integer transactionId);
    public Boolean ValidationTest();
    public void UpdateTransactionDataMap(Transaction Tx,
                                         Iterable<Node> createdNodes, Iterable<Node> deletedNodes,
                                         Iterable<Relationship> createdRelationships, Iterable<Relationship> deletedRelationships,
                                         Iterable<PropertyEntry<Node>> assignedNodeProperties, Iterable<PropertyEntry<Node>> removedNodeProperties,
                                         Iterable<PropertyEntry<Relationship>> assignedRelationshipProperties, Iterable<PropertyEntry<Relationship>> removedRelationshipProperties,
                                         Iterable<LabelEntry> assignedLabels, Iterable<LabelEntry> removedLabels);
    public void UpdateTransactionDataMap(Transaction Tx, Iterable<Node> readNodes);
    // we need a function to monitor the read-only transaction.
    // read-only transactoin can not be hooked by beforeCommit function.
    // but we need to delete its data after it ends.
    // HOW?????
}
