import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.graphdb.Label.label;

public class MonitorSpeedTester extends Thread{
    static Map<Integer, Long> serializedTxEndTime;
    static Map<Integer, Long> monitoredTxEndTime;
    static Map<Integer, Long> originalTxEndTime; // original neo4j transaction without monitor

    private static final Path databaseDirectory = Path.of("target/neo4j-test-db");

    GraphDatabaseService graphDb;
    DatabaseManagementService managementService;
    CustomGraphDatabaseService customGraphDb;
    CustomDatabaseManagementService customManagementService;
    static int testNumber;

    public MonitorSpeedTester(){
        serializedTxEndTime = new HashMap<Integer, Long>();
        monitoredTxEndTime = new HashMap<Integer, Long>();
        originalTxEndTime = new HashMap<Integer, Long>();
        testNumber = 100;
    }
    public static void main(final String[] args) throws IOException {
        MonitorSpeedTester neoDB = new MonitorSpeedTester();

        neoDB.createDb();

        neoDB.GetOriginalTxEndTime();
        neoDB.GetSerializedTxEndTime();
        neoDB.GetMonitoredTxEndTime();

        // output the result: commit time of each transaction:
        System.out.print("\n originalTxEndTime:\n");
        for (int i = 1; i <= testNumber; i++){
            System.out.print(originalTxEndTime.get(i) + ",");
        }

        System.out.print("\n serializedTxEndTime:\n");
        for (int i = 1; i <= testNumber; i++){
            System.out.print(serializedTxEndTime.get(i) + ",");
        }

        System.out.print("\n monitoredTxEndTime:\n");
        for (int i = 1; i <= testNumber; i++){
            System.out.print(monitoredTxEndTime.get(i) + ",");
        }
    }

    void createDb(){
        managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        try (Transaction Tx = graphDb.beginTx()){
            Tx.createNode(label("lockedNode"));
            Tx.commit();
        }

        managementService.shutdown();
    }
    void GetOriginalTxEndTime(){

        // connect to database
        managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        // open many transaction and log their end time
        // parameter: id: transaction sleeping time
        OriginalTransaction[] transaction = new OriginalTransaction[testNumber];
        for (int i = 0; i < testNumber; i++) {
            transaction[i] = new OriginalTransaction(i, originalTxEndTime);
        }
        Long startTime = System.nanoTime();
        for (int i = 0; i < testNumber; i++) {
            transaction[i].start();
        }

        // wait until all the transactions commit
        try{
            Thread.sleep(30000);
        }
        catch (InterruptedException e){
            e.printStackTrace();;
        }

        for (int i = 1; i <= testNumber; i++){
            if (originalTxEndTime.get(i) != null){
                originalTxEndTime.replace(i, originalTxEndTime.get(i) - startTime);
            }
            else {
                System.out.print("\na null value at id: " + i + "\n");
            }
        }

        // disconnect to the database
        managementService.shutdown();
    }

    void GetSerializedTxEndTime(){
        managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        // open many transaction and log their end time
        // parameter: id: transaction sleeping time
        SerializedTransaction[] transaction = new SerializedTransaction[testNumber];
        for (int i = 0; i < testNumber; i++) {
            transaction[i] = new SerializedTransaction(i, serializedTxEndTime);
        }
        Long startTime = System.nanoTime();
        for (int i = 0; i < testNumber; i++) {
            transaction[i].start();
        }

        // wait until all the transactions commit
        try{
            Thread.sleep(30000);
        }
        catch (InterruptedException e){
            e.printStackTrace();;
        }

        for (int i = 1; i <= testNumber; i++){
            if (serializedTxEndTime.get(i) != null){
                serializedTxEndTime.replace(i, serializedTxEndTime.get(i) - startTime);
            }
            else {
                System.out.print("\na null value at id: " + i + "\n");
            }
        }

        // disconnect to the database
        managementService.shutdown();
    }
    void GetSerializedTxEndTime3(){
        managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        Long startTime = System.nanoTime();

        for(int i = 1; i <= testNumber; i++){
            try (Transaction Tx = graphDb.beginTx()) {
                // some read operations
                Thread.sleep(i);
                // some write operations: create node!
                Tx.createNode();

                Tx.commit();
                serializedTxEndTime.put(i, System.nanoTime() - startTime);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

        }
    }
    void GetSerializedTxEndTime2(){
        // connect to database
        managementService = new DatabaseManagementServiceBuilder(databaseDirectory).build();
        graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(managementService);

        // add a node which all transaction will write to. so that the transactions will become serializable
        try (Transaction Tx = graphDb.beginTx()) {
            Tx.createNode(label("lockedNode"));
            // Tx.acquireWriteLock(n);
            Tx.commit();
        }

        Long startTime = System.nanoTime();
        for (int i = 1; i <= testNumber; i++) {
            // SerializedTransaction transaction = new SerializedTransaction(i, serializedTxEndTime, startTime);
            SerializedTransaction transaction = new SerializedTransaction(i, serializedTxEndTime);
            transaction.start();
        }

        // wait until all the transactions commit
        try{
            Thread.sleep(30000);
        }
        catch (InterruptedException e){
            e.printStackTrace();;
        }
        // disconnect to the database
        managementService.shutdown();
    }


    // test the monitor's speed between original neo4j and serialized execution
    // should be the same as original one, just we use custom classes
    void GetMonitoredTxEndTime(){
        customManagementService = new CustomDatabaseManagementService(
                new DatabaseManagementServiceBuilder(databaseDirectory).build());
        customGraphDb = customManagementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(customManagementService.ManagementService());

        // open many monitoredTransactions and log their end time
        // parameter: id: transaction sleeping time
        MonitoredTransaction[] transaction = new MonitoredTransaction[testNumber];
        for (int i = 0; i < testNumber; i++) {
            transaction[i] = new MonitoredTransaction(i, monitoredTxEndTime);
        }
        Long startTime = System.nanoTime();
        for (int i = 0; i < testNumber; i++) {
            transaction[i].start();
        }

        // wait until all the transactions commit
        try{
            Thread.sleep(30000);
        }
        catch (InterruptedException e){
            e.printStackTrace();;
        }

        for (int i = 1; i <= testNumber; i++){
            if (monitoredTxEndTime.get(i) != null){
                monitoredTxEndTime.replace(i, monitoredTxEndTime.get(i) - startTime);
            }
            else {
                System.out.print("\na null value at id: " + i + "\n");
            }
        }

        // disconnect to the database
        customManagementService.shutdown();
    }



    // tag::shutdownHook[]
    private static void registerShutdownHook(final DatabaseManagementService managementService) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                managementService.shutdown();
            }
        });
    }

    class OriginalTransaction implements Runnable{
        private int id;
        private Map<Integer, Long> endTime;
        // Long startTime;
        Thread t;
        public OriginalTransaction(Integer _id, Map<Integer, Long> _endTime){
            this.endTime = _endTime;
            this.id = _id;
            // this.startTime = _startTime;
        }

        @Override
        public void run(){
            System.out.print("Get id: " + id + "\n");
            try (Transaction Tx = graphDb.beginTx()) {
                // some read operations
                Thread.sleep(id);
                // some write operations: create node!
                Tx.createNode();

                Tx.commit();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            // endTime.put(id, System.nanoTime() - startTime);

            endTime.put(id, System.nanoTime());
        }
        public void start() {
            if (t == null) {
                t = new Thread (this);
                t.start ();
            }
        }
    }

    class SerializedTransaction implements Runnable{

        private int id;
        private Map<Integer, Long> endTime;
        // Long startTime;
        Thread t;
        public SerializedTransaction(Integer _id, Map<Integer, Long> _endTime){
            this.endTime = _endTime;
            this.id = _id;
            // this.startTime = _startTime;
        }

        @Override
        public void run(){
            try (Transaction Tx = graphDb.beginTx()) {

                // get exclusive lock
                ResourceIterator<Node> lockNode = Tx.findNodes(label("lockedNode"));
                while (lockNode.hasNext()) {
                    Node n = lockNode.next();
                    Tx.acquireWriteLock(n);
                }

                // some read operations
                Thread.sleep(id);

                // some write operations: create node!
                Tx.createNode();

                Tx.commit();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            endTime.put(id, System.nanoTime());
        }
        public void start() {
            if (t == null) {
                t = new Thread (this);
                t.start ();
            }
        }
    }
    public class MonitoredTransaction implements Runnable {
        private int id;
        private Map<Integer, Long> endTime;
        Thread t;
        public MonitoredTransaction(Integer _id, Map<Integer, Long> _endTime){
            this.endTime = _endTime;
            this.id = _id;
        }

        @Override
        public void run(){
            System.out.print("Get id: " + id + "\n");
            try (CustomTransaction Tx = customGraphDb.beginTx()) {
                // some read operations
                Thread.sleep(id);
                // some write operations: create node!
                Tx.createNode();

                Tx.commit();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            catch (TransactionFailureException e){
                // roll back
            }
            endTime.put(id, System.nanoTime());
        }
        public void start() {
            if (t == null) {
                t = new Thread (this);
                t.start ();
            }
        }
    }
}

