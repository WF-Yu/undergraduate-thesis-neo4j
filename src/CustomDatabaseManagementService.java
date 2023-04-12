import org.neo4j.dbms.database.DatabaseManagementServiceImpl.*;
import org.neo4j.dbms.api.*;
import java.util.List;
import org.neo4j.annotations.api.PublicApi;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Configuration;
import org.neo4j.graphdb.event.DatabaseEventListener;
import org.neo4j.graphdb.event.TransactionEventListener;
public class CustomDatabaseManagementService {
    DatabaseManagementService managementService;
    public CustomDatabaseManagementService (DatabaseManagementService _managementService) {
        managementService = _managementService; //???

        System.out.print("Initializing CustomDatabaseManagementService\n");
    }

    /**
     * Retrieve a database service by name.
     * @param databaseName name of the database.
     * @return the database service with the provided name
     * @throws DatabaseNotFoundException if no database service with the given name is found.
     */
    CustomGraphDatabaseService database(String databaseName) throws DatabaseNotFoundException {
        try {
            // 这里是连接/新建了一个数据库，可以将这里作为激活monitor的开关
            CustomGraphDatabaseService graphDbService = new CustomGraphDatabaseService(managementService.database(databaseName));
            managementService.registerTransactionEventListener(databaseName, graphDbService.graphDbMonitor);
            return graphDbService;
        }
        catch (DatabaseNotFoundException e) {
            System.out.print(e.getMessage());
            return null;
        }
    }

    /**
     * Create a new database.
     * @param databaseName name of the database.
     * @throws DatabaseExistsException if a database with the provided name already exists
     */
    void createDatabase(String databaseName) throws DatabaseExistsException {
        managementService.createDatabase(databaseName, Configuration.EMPTY);
    }

    /**
     * Create a new database.
     * @param databaseName name of the database.
     * @param databaseSpecificSettings settings that are specific to this database. Only a sub-set of settings are supported TODO.
     * @throws DatabaseExistsException if a database with the provided name already exists
     */
    void createDatabase(String databaseName, Configuration databaseSpecificSettings) throws DatabaseExistsException {
        managementService.createDatabase(databaseName,databaseSpecificSettings);
    }

    /**
     * Drop a database by name. All data stored in the database will be deleted as well.
     * @param databaseName name of the database to drop.
     * @throws DatabaseNotFoundException if no database with the given name is found.
     * @throws DatabaseAliasExistsException if the database exists but has an alias.
     */
    void dropDatabase(String databaseName) throws DatabaseNotFoundException, DatabaseAliasExistsException {
        managementService.dropDatabase(databaseName);
    }

    /**
     * Starts a already existing database.
     * @param databaseName name of the database to start.
     * @throws DatabaseNotFoundException if no database with the given name is found.
     */
    void startDatabase(String databaseName) throws DatabaseNotFoundException {
        managementService.startDatabase(databaseName);
    }

    /**
     * Shutdown database with provided name.
     * @param databaseName name of the database.
     * @throws DatabaseNotFoundException if no database with the given name is found.
     */
    void shutdownDatabase(String databaseName) throws DatabaseNotFoundException {
        managementService.shutdownDatabase(databaseName);
    }

    /**
     * @return an alphabetically sorted list of all database names this database server manages.
     */
    List<String> listDatabases() {
        return managementService.listDatabases();
    }

    /**
     * Registers {@code listener} as a listener for database events.
     * If the specified listener instance has already been registered this method will do nothing.
     *
     * @param listener the listener to receive events about different states
     *                in the database lifecycle.
     */
    void registerDatabaseEventListener(DatabaseEventListener listener) {
        managementService.registerDatabaseEventListener(listener);
    }

    /**
     * Unregisters {@code listener} from the list of database event handlers.
     * If {@code listener} hasn't been registered with
     * {@link #registerDatabaseEventListener(DatabaseEventListener)} prior to calling
     * this method an {@link IllegalStateException} will be thrown.
     * After a successful call to this method the {@code listener} will no
     * longer receive any database events.
     *
     * @param listener the listener to receive events about database lifecycle.
     * @throws IllegalStateException if {@code listener} wasn't registered prior
     *                               to calling this method.
     */
    void unregisterDatabaseEventListener(DatabaseEventListener listener) {
        managementService.unregisterDatabaseEventListener(listener);
    }

    /**
     * Registers {@code listener} as a listener for transaction events which
     * are generated from different places in the lifecycle of each
     * transaction in particular database. To guarantee that the handler gets all events properly
     * it shouldn't be registered when the application is running (i.e. in the
     * middle of one or more transactions). If the specified handler instance
     * has already been registered this method will do nothing.
     *
     * @param databaseName name of the database to listener transactions
     * @param listener the listener to receive events about different states
     *                in transaction lifecycle.
     */
    void registerTransactionEventListener(String databaseName, TransactionEventListener<?> listener) {
        managementService.registerTransactionEventListener(databaseName, listener);
    }

    /**
     * Unregisters {@code listener} from the list of transaction event listeners.
     * If {@code handler} hasn't been registered with
     * {@link #registerTransactionEventListener(String, TransactionEventListener)} prior
     * to calling this method an {@link IllegalStateException} will be thrown.
     * After a successful call to this method the {@code listener} will no
     * longer receive any transaction events.
     *
     * @param databaseName name of the database to listener transactions
     * @param listener the listener to receive events about different states
     *                in transaction lifecycles.
     * @throws IllegalStateException if {@code listener} wasn't registered prior
     *                               to calling this method.
     */
    void unregisterTransactionEventListener(String databaseName, TransactionEventListener<?> listener) {
        managementService.unregisterTransactionEventListener(databaseName, listener);
    }

    /**
     * Shutdown database server.
     */
    void shutdown() {
        // send msg to monitor, close the monitor
        // unregister listener
        managementService.shutdown();
    }


    // new methods:

    public DatabaseManagementService ManagementService(){
        return managementService;
    }
}
