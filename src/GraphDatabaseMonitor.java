import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.LabelEntry;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionEventListener;

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
    void endTransaction(Integer transactionId);
    Boolean ValidationTest(Integer transactionId);
    void commitInBackground(Integer transactionId);
    void UpdateTransactionDataMap(Integer transactionId,
                                         Iterable<Node> createdNodes, Iterable<Node> deletedNodes,
                                         Iterable<Relationship> createdRelationships, Iterable<Relationship> deletedRelationships,
                                         Iterable<PropertyEntry<Node>> assignedNodeProperties, Iterable<PropertyEntry<Node>> removedNodeProperties,
                                         Iterable<PropertyEntry<Relationship>> assignedRelationshipProperties, Iterable<PropertyEntry<Relationship>> removedRelationshipProperties,
                                         Iterable<LabelEntry> assignedLabels, Iterable<LabelEntry> removedLabels);

    void uploadRelationshipRead(Integer transactionId, ResourceIterable<Relationship> relationships);
    void uploadRelationshipRead(Integer transactionId, Relationship relationship);
    void uploadNodeRead(Integer transactionId, ResourceIterable<Node> nodes);
    void uploadNodeRead(Integer transactionId, Node node);

    boolean hasTransaction(Integer transactionId);


}
