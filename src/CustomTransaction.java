import org.neo4j.graphdb.*;

import java.util.Iterator;

public class CustomTransaction implements AutoCloseable{
    Transaction Tx;
    public CustomTransaction (Transaction _Tx) {
        Tx = _Tx;
    }
    Node createNode(Label... labels){
        return Tx.createNode(labels);
    }
    ResourceIterable<Node> getAllNodes(CustomGraphDatabaseService customGraphDb) {
        ResourceIterable<Node> nodes= Tx.getAllNodes();
        // send a msg to monitor, how?
        customGraphDb.graphDbMonitor.UpdateTransactionDataMap(Tx, nodes);
        return nodes;
    }
    void commit() {
        // validation test is called here.
        // If it passes the test, transaction will commit. otherwise rollback
        Tx.commit();
    }
    void rollback(){
        Tx.rollback();
    }
    // 这里有问题！！返回的不是Tx的hashCode
    void terminate(){
        Tx.terminate();
    }
    int hashcode(){ // 注意我们不能返回customTx的hashcode!!
        return Tx.hashCode();
    }
    public void close(){
        Tx.close();
    }
}
