
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashMap;
import java.util.Map;

public class GraphDatabaseMonitorImpl implements GraphDatabaseMonitor{

    // Map<TransactionId, TransactionInformation(R:from transaction actions, W:TransactionData)>
    final private Map<Integer, TransactionInformation> transactionDataMap; // why final?
    public GraphDatabaseMonitorImpl(){
        transactionDataMap = new HashMap<Integer, TransactionInformation>();
    }
    public void connect(String DatabaseName){
        System.out.print("In Monitor: Connect to database " + DatabaseName + "\n");

        // create area to store data for this Database
    }

    public void startTransaction(CustomTransaction customTx){
        System.out.print("In Monitor: Start one Transaction: " + customTx.Tx.hashCode() + "\n");
        // create area to store data for this Transaction
        // startTIme for this transactoin will be logged automatically when constructing new TransactionInformation object
        // 为啥这俩hashCode不一样啊？？？？？
        transactionDataMap.put(customTx.Tx.hashCode(),new TransactionInformation(customTx));
    }

    /**
     *
     * @return false is doesn't pass the test.
     */
    public Boolean ValidationTest(){
        // 根据validation test原则，似乎只对写进程进行了validation test啊？
        // 这个问题可以先放一放，先解决如何获得被读取的数据的信息
        return false;
    }

    /**
     *
     * @param data is the interface where we can get the data modified by @param transaction
     * @param transaction is the link to the transaction to be committed
     * @param databaseService link to the GraphDatabaseService where this transaction belongs to
     * @return ?don't know where it goes
     * @throws Exception (don't know how to use it?
     */
    public TransactionInformation beforeCommit(TransactionData data, Transaction transaction, GraphDatabaseService databaseService)
            throws Exception {
        System.out.print("In Monitor: Before Commit: " + transaction.hashCode() + "\n");

        // here we can get the data write by transaction;
        // add them to Map<Integer, TransactionInformation> transactionDataMap
        UpdateTransactionDataMap(transaction, data.createdNodes(), data.deletedNodes(),
                data.createdRelationships(), data.deletedRelationships(),
                data.assignedNodeProperties(), data.removedNodeProperties(),
                data.assignedRelationshipProperties(), data.removedRelationshipProperties(),
                data.assignedLabels(), data.removedLabels());

        // call the validation test function here
        Integer txId = transaction.hashCode();
        TransactionInformation txInfo = transactionDataMap.get(txId);
        if(!ValidationTest()) {
            // didn't pass the test, rollback the transaction.
            // should we restart it again at some point of time???

            // seems to have some problem here.
            // if it can't go to afterrollback function,
            // then we need to delete its transactionMapData here
            /*try{
                transaction.rollback();
            }
            catch (Exception e){
                System.out.print(e.toString() + "\n");
            }*/
            transaction.terminate();

        }
        // if pass the validation test, transaction will be committed automatically
        // and state information will be passed to afterCommit function
        return txInfo; // ?
    }

    public void afterCommit(TransactionData data, TransactionInformation state, GraphDatabaseService databaseService) {
        System.out.print("In Monitor: committed one transaction with ID: " + state.getTxId() +"\n");
        endTransaction(state.getTxId());
    }

    /**
     * Invoked after the transaction has been rolled back if committing the
     * transaction failed for some reason.
     * 如果没有被commit,而是直接被rollback了，那么这个功能就不会被唤醒。所以这个功能好像没啥用
     */
    public void afterRollback(TransactionData data, TransactionInformation state, GraphDatabaseService databaseService) {
        System.out.print("In Monitor: rolled back one transaction (" + state.getTxId() + ")\n");
        endTransaction(state.getTxId());
    }
    public void UpdateTransactionDataMap(Transaction Tx,
                                         Iterable<Node> createdNodes, Iterable<Node> deletedNodes,
                                         Iterable<Relationship> createdRelationships, Iterable<Relationship> deletedRelationships,
                                         Iterable<PropertyEntry<Node>> assignedNodeProperties, Iterable<PropertyEntry<Node>> removedNodeProperties,
                                         Iterable<PropertyEntry<Relationship>> assignedRelationshipProperties, Iterable<PropertyEntry<Relationship>> removedRelationshipProperties,
                                         Iterable<LabelEntry> assignedLabels, Iterable<LabelEntry> removedLabels) {

        // add the IDs of these data into transactionDataMap
        // neo4j does not expose transaction id,
        // so we use its hashcode instead
        // (don't know if it will cause any problems like hash collision?!!)
        Integer transactionId = Tx.hashCode();
        if(transactionDataMap.containsKey(transactionId)){ // 正常情况应该是有的。没有的话证明出了什么问题
            TransactionInformation txInfo = transactionDataMap.get(transactionId);
            // add the modified data into txInfo
            txInfo.uploadCreatedNodes(createdNodes); // ?不知道这里应该怎么写
            txInfo.uploadDeletedNodes(deletedNodes);
            // ......to be implemented

            // upload modified txInfo back to transactionDataMap
            transactionDataMap.replace(transactionId, txInfo);
        }
        else{
            // there is an error here. we need to terminate the transaction.
        }
    }
    public void UpdateTransactionDataMap(Transaction Tx, Iterable<Node> readNodes){
        Integer transactionId = Tx.hashCode();
        if(transactionDataMap.containsKey(transactionId)){
            TransactionInformation txInfo = transactionDataMap.get(transactionId);
            txInfo.uploadReadNodes(readNodes);
        }
        else{
            // there is an error here. we need to terminate the transaction.
        }
    }
    public void endTransaction(Integer transactionId) {
        transactionDataMap.remove(transactionId);
    }
}
