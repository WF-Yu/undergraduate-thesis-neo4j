import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;

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
    private LocalDateTime commitTime;

    // now it's unsure how should we store the data.
    // Just test if the monitor can receive the data successfully -> it can!
    // the way we store data should rely on how we do the validation test
    private List<Integer> deletedEntityIdList;
    private List<Integer> createdEntityIdList;
    private List<Integer> modifiedEntityIdList; // Entities which properties or labels are modified.
    private List<Integer> readEntityIdList; // Entities accessed by functions like: findNodes, findRelationships

    public TransactionInformation(CustomTransaction Tx){
        startTime = LocalDateTime.now();
        commitTime = LocalDateTime.MAX;
        txId = Tx.hashcode();
        deletedEntityIdList = new ArrayList<Integer>();
        createdEntityIdList = new ArrayList<Integer>();
        modifiedEntityIdList = new ArrayList<Integer>();
        readEntityIdList = new ArrayList<Integer>();
        System.out.print("Created one TransactionInformation Object with ID " + txId + " \n");
    }

    void uploadCreatedNodes(Iterable<Node> modifiedNodes){

        Iterator<Node> it = modifiedNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the id into List
            createdEntityIdList.add(nodeId);
            // test
            System.out.print("In TransactionInformation (" + txId + "): receive a created node with ID: " + nodeId + "\n");
        }

    }
    void uploadDeletedNodes(Iterable<Node> modifiedNodes){

        Iterator<Node> it = modifiedNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the id into List
            deletedEntityIdList.add(nodeId);
            // test
            System.out.print("In TransactionInformation (" + txId + "): receive a deleted node with ID: " + nodeId + "\n");
        }
    }



    // other implementation for uploading data
    // ......
    void uploadReadNodes(Iterable<Node> readNodes){
        Iterator<Node> it = readNodes.iterator();
        while(it.hasNext()){
            Integer nodeId = it.next().hashCode();
            // upload the id into List
            readEntityIdList.add(nodeId);
            // test
            System.out.print("In TransactionInformation: receive a read node with ID: " + nodeId + "\n");
        }
    }

    // some functions for calculate: input may be another TransactionInformation
    // ......


    Integer getTxId() {
        return txId;
    }
    LocalDateTime getStartTime(){
        return startTime;
    }
    LocalDateTime getCommitTime(){
        return commitTime;
    }
    void setTxId(Integer _txId){
        txId = _txId;
    }
    void setStartTime(LocalDateTime _startTime){
        startTime = _startTime;
    }
    void setCommitTime(LocalDateTime _commitTime){
        commitTime = _commitTime;
    }

}
