
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.graphdb.Label.label;

import java.io.IOException;
import java.nio.file.Path;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;


public class Main extends Thread{
    private static final Path databaseDirectory = Path.of("target/neo4j-Validation");
    private CustomGraphDatabaseService customGraphDb;
    private CustomDatabaseManagementService customManagementService;

    private enum RelTypes implements RelationshipType {
        LIKE
    }
    private CustomNode A, B;
    private CustomRelationship R;
    public static void main(final String[] args) throws IOException { // 这些static 啥的都有啥用啊？？？
        Main neoDb = new Main();
        neoDb.createDb();
        neoDb.create_graph();

        try {
            Main.sleep(1000);
        }
        catch(InterruptedException e){
            System.out.print(e.toString());
        }
        neoDb.shutDown();
    }

    void createDb() throws IOException {
        System.out.println("connecting to database ...");
        // FileUtils.deleteDirectory(databaseDirectory);
        customManagementService = new CustomDatabaseManagementService(
                new DatabaseManagementServiceBuilder(databaseDirectory).build()); // 这个调用关系好乱啊？？
        customGraphDb = customManagementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook(customManagementService.ManagementService());
    }
    private static void registerShutdownHook(final DatabaseManagementService managementService) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                managementService.shutdown();
            }
        });
    }

    void create_graph() {

        // in case we need to rollback and retry the transaction, we need to write the corresponding code here. but it's not a good idea, how to hide this function in monitor?
        /* clear all the nodes first*/
        try (CustomTransaction Tx = customGraphDb.beginTx()){
            int cnt = 0;
            ResourceIterable<CustomNode> Nodes = Tx.getAllNodes();
            for (CustomNode n : Nodes) {
                cnt++;
                for(CustomRelationship r : n.getRelationships()){
                    r.relationship.delete();
                }
                n.delete();
            }
            try{
                System.out.print("*1\n");
                Tx.commit();
                System.out.print("*1.5\n"); // validation test passed, commit successfully
            }
            catch (TransactionFailureException e){
                System.out.print("*2\n"); // if it doesn't pass the validation test, program will come to here.
                // here the transaction has already been rolled back
                System.out.print(e.toString());
            }
            finally {
                System.out.print("*3\n");
                Tx.close();
            }
            System.out.println("cleared " + cnt + " Nodes. Now the graph has no node");
        }

        try (CustomTransaction Tx = customGraphDb.beginTx()){
            A = Tx.createNode(label("Person"));
            A.node.setProperty("name", "A");
            B = Tx.createNode(label("Person"));
            B.node.setProperty("name", "B");

            R = A.createRelationshipTo(B, RelTypes.LIKE);
            R.relationship.setProperty("date", 20230312);

            try{
                System.out.print("*1\n");
                Tx.commit();
                System.out.print("*1.5\n");
            }
            catch (TransactionFailureException e){
                System.out.print("*2\n");
                System.out.print(e.toString());
            }
            finally {
                System.out.print("*3\n");
                Tx.close();
            }
        }
    }
    void printDb() {
        try (CustomTransaction Tx = customGraphDb.beginTx()){

            ResourceIterable<CustomNode> Nodes = Tx.getAllNodes();
            for (CustomNode n : Nodes) {
                System.out.print(n.toString() + "\n" +
                        "Label: " + n.getLabels().toString() + "\n" +
                        "Properties: " + n.node.getAllProperties().toString() + "\n");
                for(CustomRelationship r : n.getRelationships()){
                    System.out.print("   Relationship: " + r.toString() + "\n" +
                            "   Properties: " + r.relationship.getAllProperties().toString() + "\n");
                }
            }
            Tx.commit();
        }
    }
    void shutDown() {
        System.out.println();
        System.out.println("Shutting down database ...");
        customManagementService.shutdown();
    }

}

