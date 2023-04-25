import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


// the functions in monitor should be Async!!!!!!!!!!!!!!
// example:
/*
public class Monitor {
    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void commitInBackground(String txId) {
        executor.submit(() -> {
            // 这里是需要在后台运行的代码
            if (hasTransaction(txId)) {
                endTransaction(txId);
            }
        });
    }
    // ....
}
in class CustomTransaction:
public class Transaction {
    public void commit() {
        monitor.commitInBackground(txId);
    }
}
 */
public class GraphDatabaseMonitorImpl implements GraphDatabaseMonitor{
    private ExecutorService executor = Executors.newFixedThreadPool(15); //CPU core count: 8

    // Map<TransactionId, TransactionInformation(R:from transaction actions, W:TransactionData)>
    final private Map<Integer, TransactionInformation> transactionDataMap; // why final?
    public GraphDatabaseMonitorImpl(){
        transactionDataMap = new HashMap<Integer, TransactionInformation>();
    }
    public void connect(String DatabaseName){
        System.out.print("In Monitor: Connect to database " + DatabaseName + "\n");
    }

    public void startTransaction(CustomTransaction customTx){
        System.out.print("In Monitor: Start one Transaction: " + customTx.Tx.hashCode() + "\n");
        // create area to store data for this Transaction
        // startTIme for this transaction will be logged automatically when constructing new TransactionInformation object
        // 为啥这俩hashCode不一样啊？？？？？
        transactionDataMap.put(customTx.Tx.hashCode(),new TransactionInformation(customTx));
    }

    /**
     *
     * @return false is doesn't pass the test.
     */
    public Boolean ValidationTest(Integer transactionId){
        Boolean result = false;

        // lost update: R1(x) R2(x) W1(x) W2(x) (assume that T1 is going to be committed)
        // we should first detect if there is another transaction reads the data x
        // if X has been modified by another transaction Ti, then T1 should be rollback
        // otherwise, T1 will commit and send a signal to T2 (who has read x) that x has been modified
        // it means R(x)W(x) should be serializable
        // 所以说，对于我们读到的数据，需要存两个: 一个是数据的hashcode, 另一个是数据是否被改了。进行validationTest的时候，我们首先对于所有W的数据，检查其是否存在于R集合里。 如果存在又被改了，那么肯定不行。如果不存在，就可以提交，但是得检查一下其他进行的R和本进程的W是否有重合。如果有的话，需要把那个进程对应的数据打上标记。
        TransactionInformation txInfo = transactionDataMap.get(transactionId);
        if(!txInfo.ifReadValid()){
            //
            System.out.print("[FAIL] Didn't pass the test (" + txInfo.getTxId() + ")\n");
            result = false;
        }
        else{
            result = true;
            System.out.print("[In Monitor] Validation test passed\n");
            // 将这个进程的写集合标记到其他所有进程中
            for(Map.Entry<Integer, TransactionInformation> data : transactionDataMap.entrySet()){
                // for other transactions, there data read may have been modified thus become invalid
                if (data.getValue().getTxId() != transactionId){
                    data.getValue().upadteInvalidNodeRead(txInfo.getModifiedNodeId());
                    data.getValue().upadteInvalidRelationshipRead(txInfo.getModifiedRelationshipId());
                }
            }// 这里和上面的区别是，最后写的元素不一定是X。并且在读的值失效之后，又进行了读，和写。我们怎么才能记录读失效读这个操作？只有满足这个条件，进程才会被停止。
        }
        return result;
    }

    /**
     *
     * @param data is the interface where we can get the data modified by @param transaction
     * @param transaction is the link to the transaction to be committed
     * @param databaseService link to the GraphDatabaseService where this transaction belongs to
     * @return ?don't know where it goes
     * @throws Exception
     */

    //
    public TransactionInformation beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService)
            throws Exception {
        // Validation test is not concurrent
        System.out.print("[In Monitor] Before Commit: " + transaction.hashCode() + "\n");

        // here we can get the data write by transaction;
        // add them to Map<Integer, TransactionInformation> transactionDataMap
        UpdateTransactionDataMap(transaction.hashCode(), data.createdNodes(), data.deletedNodes(),
                data.createdRelationships(), data.deletedRelationships(),
                data.assignedNodeProperties(), data.removedNodeProperties(),
                data.assignedRelationshipProperties(), data.removedRelationshipProperties(),
                data.assignedLabels(), data.removedLabels());

        // call the validation test function here
        Integer txId = transaction.hashCode();
        TransactionInformation txInfo = transactionDataMap.get(txId);

        // log the time of Validation test
        txInfo.setValidationTestTime(LocalDateTime.now());

        if(!ValidationTest(txId)) {
            // didn't pass the test, rollback the transaction.
            // should we restart it again at some point of time??? -> this can be done in user code. but not good.
            transaction.terminate();

        }
        // if it passes the validation test, transaction will be committed automatically
        // and state information will be passed to afterCommit function
        return txInfo;
    }

    public void afterCommit(TransactionData data, TransactionInformation state, GraphDatabaseService databaseService) {
        System.out.print("[In Monitor] Committed one transaction with ID: " + state.getTxId() +"\n");
        commitInBackground(state.getTxId());
        // endTransaction(state.getTxId());
    }

    /**
     * Invoked after the transaction has been rolled back if committing the
     * transaction failed for some reason.
     * 如果没有被commit,而是直接被rollback了，那么这个功能就不会被唤醒。所以这个功能好像没啥用
     */
    public void afterRollback(TransactionData data, TransactionInformation state, GraphDatabaseService databaseService) {
        // System.out.print("[In Monitor] Rolled back one transaction (" + state.getTxId() + ")\n");
        commitInBackground(state.getTxId());
        // endTransaction(state.getTxId());
    }
    public void UpdateTransactionDataMap(Integer transactionId,
                                         Iterable<Node> createdNodes, Iterable<Node> deletedNodes,
                                         Iterable<Relationship> createdRelationships, Iterable<Relationship> deletedRelationships,
                                         Iterable<PropertyEntry<Node>> assignedNodeProperties, Iterable<PropertyEntry<Node>> removedNodeProperties,
                                         Iterable<PropertyEntry<Relationship>> assignedRelationshipProperties, Iterable<PropertyEntry<Relationship>> removedRelationshipProperties,
                                         Iterable<LabelEntry> assignedLabels, Iterable<LabelEntry> removedLabels) {

        // add the IDs of these data into transactionDataMap
        // neo4j does not expose transaction id, so we use its hashcode instead
        executor.submit(() -> {
            if (transactionDataMap.containsKey(transactionId)) {
                TransactionInformation txInfo = transactionDataMap.get(transactionId);

                // add the modified data into txInfo
                // txInfo.uploadCreatedNodes(createdNodes);
                // txInfo.uploadDeletedNodes(deletedNodes);
                // txInfo.uploadCreatedRelationships
                // txInfo.uploadDeletedRelationships
                txInfo.uploadWrittenNodes(assignedNodeProperties);
                txInfo.uploadWrittenNodes(removedNodeProperties);
                txInfo.uploadWrittenRelationships(assignedRelationshipProperties);
                txInfo.uploadWrittenRelationships(removedRelationshipProperties);

                // upload modified txInfo back to transactionDataMap
                transactionDataMap.replace(transactionId, txInfo);
            } else {
                // error: we get the wrong transaction ID. it happens only when there is logical error in source code
            }
        });
    }
   public void uploadRelationshipRead(Integer transactionId, ResourceIterable<Relationship> relationships){
       executor.submit(() -> {
           if (transactionDataMap.containsKey(transactionId)) {
               TransactionInformation txInfo = transactionDataMap.get(transactionId);
               txInfo.uploadRelationshipsRead(relationships);
           } else {
               // error: we get the wrong transaction ID. it happens only when there is logical error in source code
           }
       });
    }
    // 这些方法应该做成可以并行执行的！
    public void uploadRelationshipRead(Integer transactionId, Relationship relationship){
        executor.submit(() -> {
            if (transactionDataMap.containsKey(transactionId)) {
                TransactionInformation txInfo = transactionDataMap.get(transactionId);
                txInfo.uploadRelationshipRead(relationship);
            } else {
                // error: we get the wrong transaction ID. it happens only when there is logical error in source code
            }
        });
    }
    public void uploadNodeRead(Integer transactionId, Node node){
        executor.submit(() -> {
            if (transactionDataMap.containsKey(transactionId)) {
                TransactionInformation txInfo = transactionDataMap.get(transactionId);
                txInfo.uploadNodeRead(node);
            } else {
                // error: we get the wrong transaction ID. it happens only when there is logical error in source code
            }
        });
    }
    public void uploadNodeRead(Integer transactionId, ResourceIterable<Node> node){
        executor.submit(() -> {
            if (transactionDataMap.containsKey(transactionId)) {
                TransactionInformation txInfo = transactionDataMap.get(transactionId);
                txInfo.uploadNodesRead(node);
            } else {
                // error: we get the wrong transaction ID. it happens only when there is logical error in source code
            }
        });
    }

    public void endTransaction(Integer transactionId) {
        if (transactionDataMap.containsKey(transactionId)){
            transactionDataMap.remove(transactionId);
        }
    }
    public boolean hasTransaction(Integer transactionId){
        return transactionDataMap.containsKey(transactionId);
    }
    public void commitInBackground(Integer transactionId) {
        executor.submit(() -> {
            // 这里是需要在后台运行的代码
            if (hasTransaction(transactionId)) {
                endTransaction(transactionId);
            }
        });
    }
}
