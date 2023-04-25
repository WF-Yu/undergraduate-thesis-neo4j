import org.apache.commons.collections.CollectionUtils;
import org.neo4j.cypher.internal.expressions.In;
import org.neo4j.fabric.executor.Location;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.PropertyEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Contains all the data related to one transaction:
 * a. modified/read entities/properties/labels.
 * b. start/commit/end time
 * c. transactionId (? we need the id to delete the corresponding data after commit. don't know if we can pass the id by here?)
 */
public class TransactionInformation {
    private Integer txId;
    private LocalDateTime startTime;
    private LocalDateTime validationTestTime;
    private LocalDateTime commitTime; // quite useless because after commit we will delete this TransactionInformation object

    // now it's unsure how should we store the data.
    // Just test if the monitor can receive the data successfully -> it can!
    // the way we store data should rely on how we do the validation test

    // we don't need to test deleted/created entities this one because neo4j guarantees committed-read isolation level
    private List<Integer> deletedEntityIdList;
    private List<Integer> createdEntityIdList;
    private List<Integer> modifiedNodeIdList; // Entities whose properties are modified. it's the W set we need for validation test
    private List<Integer> modifiedRelationshipIdList;

    // for labels, don't know how to monitor them. because multiple entities share the same label, and it's complex

    private List<Integer> readNodeIdList;
    private List<Integer> readRelationshipIdList;
    // invalid read data. R1(x) W2(x) -> x become invalid
    private List<Integer> invalidNodeId;
    private List<Integer> invalidRelationshipId;
    // private List<DataRead> readEntityIdList; --> readEntityIdList {id, if-modified}
    // here we need to store two things: the entity id and the if-modified label. when we upload a data, and this data is already in the list with a label, it means non-repeatable read happens. we should log it!
    public TransactionInformation(CustomTransaction Tx){
        startTime = LocalDateTime.now();
        commitTime = LocalDateTime.MAX;
        txId = Tx.Tx.hashCode();
        deletedEntityIdList = new ArrayList<>();
        createdEntityIdList = new ArrayList<>();
        modifiedNodeIdList = new ArrayList<>();
        modifiedRelationshipIdList = new ArrayList<>();
        readNodeIdList = new ArrayList<>();
        readRelationshipIdList = new ArrayList<>();
        invalidNodeId = new ArrayList<>();
        invalidRelationshipId = new ArrayList<>();
        System.out.print("Created one TransactionInformation Object with ID " + txId + " \n");
    }

    /**
     * to be deleted. because neo4j has already implemented read-commmitted isolation level, so we don't have to worry about dirty reads/writes
     * @param createdNodes nodes that created during the transaction. they are not visible to other transactions.
     */
    void uploadCreatedNodes(Iterable<Node> createdNodes){ // ??
        Iterator<Node> it = createdNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the id into List
            if (!createdEntityIdList.contains(nodeId)){
                createdEntityIdList.add(nodeId);
            }
            // test
            System.out.print("[In TransactionInformation] (" + txId + "): receive a created node with ID: " + nodeId + "\n");
        }
    }

    /**
     * to be deleted also
     * @param deletedNodes nodes that deleted during the transaction.
     */
    void uploadDeletedNodes(Iterable<Node> deletedNodes){

        Iterator<Node> it = deletedNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the id into List
            if (!deletedEntityIdList.contains(nodeId)) {
                deletedEntityIdList.add(nodeId);
            }
            // test
            System.out.print("[In TransactionInformation] (" + txId + "): receive a deleted node with ID: " + nodeId + "\n");
        }
    }

    // Iterable<PropertyEntry<Node>> assignedNodeProperties, Iterable<PropertyEntry<Node>> removedNodeProperties,
    // Iterable<PropertyEntry<Relationship>> assignedRelationshipProperties, Iterable<PropertyEntry<Relationship>> removedRelationshipProperties,
    void uploadWrittenNodes(Iterable<PropertyEntry<Node>> writtenNodes){
        Iterator<PropertyEntry<Node>> it = writtenNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().entity().hashCode();
            if (!modifiedNodeIdList.contains(nodeId)) {
                modifiedNodeIdList.add(nodeId);
            }
            System.out.print("[In TransactionInformation](" + txId + ") receive a written node with ID: " + nodeId + "\n");
        }
    }
    void uploadWrittenRelationships(Iterable<PropertyEntry<Relationship>> writtenRelatonships){
        Iterator<PropertyEntry<Relationship>> it = writtenRelatonships.iterator();
        while(it.hasNext()){
            Integer relationshipId = it.next().entity().hashCode();
            if (!modifiedRelationshipIdList.contains(relationshipId)) {
                modifiedRelationshipIdList.add(relationshipId);
            }
            System.out.print("[In TransactionInformation](" + txId + ") receive a written relationship with ID: " + relationshipId + "\n");
        }
    }

    /**
     * log the nodes accessed during the transaction. It may be called in findNodes, getRelationships, etc.
     * the node we store looks like (hashCode, timeStamp)
     * @param readNodes
     */
    void uploadNodesRead(Iterable<Node> readNodes){
        Iterator<Node> it = readNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the hashCode into List
            if (!readNodeIdList.contains(nodeId)) {
                readNodeIdList.add(nodeId);
            }
            // test
            System.out.print("[In TransactionInformation](" + txId + ") Receive a read node with ID: " + nodeId + "\n");
        }
    }
    void uploadRelationshipsRead(Iterable<Relationship> readRelationships){
        Iterator<Relationship> it = readRelationships.iterator();
        while(it.hasNext()){
            Integer relashiptoinId = it.next().hashCode();
            // upload the hashCode into List
            if (!readRelationshipIdList.contains(relashiptoinId)) {
                readRelationshipIdList.add(relashiptoinId);
            }
            // test
            System.out.print("[In TransactionInformation](" + txId + ")  Receive a read relationship with ID: " + relashiptoinId + "\n");
        }
    }

    void uploadRelationshipRead(Relationship relationship){
        if (!readRelationshipIdList.contains(relationship.hashCode())) {
            readRelationshipIdList.add(relationship.hashCode());
        }
        System.out.print("[In TransactionInformation](" + txId + ")  Receive a read relationship with ID: " + relationship.hashCode() + "\n");
    }
    void uploadNodeRead(Node node){
        if (!readNodeIdList.contains(node.hashCode())) {
            readNodeIdList.add(node.hashCode());
        }
        System.out.print("[In TransactionInformation](" + txId + ")  receive a read Node with ID: " + node.hashCode() + "\n");
    }

    /**
     * see is the data read is modified by other transactions.
     * @return
     */
    boolean ifReadValid(){
        boolean result;
        // value Read by this transaction
        List<Integer> commonNodeValues = new ArrayList<>(readNodeIdList); // R1(x)
        commonNodeValues.retainAll(invalidNodeId); // W2(x)     actually one sentence is enough here.
        // commonNodeValues.retainAll(modifiedNodeIdList); // W1(x)  !

        List<Integer> commonRelationShipValues = new ArrayList<>(readRelationshipIdList);
        commonRelationShipValues.retainAll(invalidRelationshipId);
        // commonRelationShipValues.retainAll(modifiedRelationshipIdList);

        // lost update may happen if it's not empty
        if(commonRelationShipValues.size() != 0 || commonNodeValues.size() != 0){
            return false;
        }
        else {
            return true;
        }
    }
    public void upadteInvalidNodeRead(List invalidNodeIdMadeByOtherTx){
        List<Integer> invalidNodeRead = new ArrayList<>(readNodeIdList);
        invalidNodeId.addAll(CollectionUtils.intersection(invalidNodeRead, invalidNodeIdMadeByOtherTx));// 怎么去掉重复的项 how to delete the repeat items?
    }
    public void upadteInvalidRelationshipRead(List invalidRelationshipIdMadeByOtherTx){
        List<Integer> invalidRelationshipRead = new ArrayList<>(readRelationshipIdList);
        invalidNodeId.addAll(CollectionUtils.intersection(invalidRelationshipRead, invalidRelationshipIdMadeByOtherTx));// 怎么去掉重复的项 how to delete the repeat items?
    }

    List getModifiedNodeId(){
        return modifiedNodeIdList;
    }
    List getModifiedRelationshipId(){
        return modifiedRelationshipIdList;
    }
    Integer getTxId() {
        return txId;
    }
    LocalDateTime getStartTime(){
        return startTime;
    }
    LocalDateTime getCommitTime(){
        return commitTime;
    }
    LocalDateTime getValidationTestTime(){
        return validationTestTime;
    }
    void setTxId(Integer _txId){
        txId = _txId;
    }
    void setStartTime(LocalDateTime _startTime){
        startTime = _startTime;
    }
    void setValidationTestTime(LocalDateTime _validationTestTime){
        validationTestTime = _validationTestTime;
    }
    void setCommitTime(LocalDateTime _commitTime){
        commitTime = _commitTime;
    }

}
