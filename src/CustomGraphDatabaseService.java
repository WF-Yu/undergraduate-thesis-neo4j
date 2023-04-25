import org.neo4j.graphdb.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.neo4j.annotations.api.PublicApi;
public class CustomGraphDatabaseService{
    GraphDatabaseService graphDb;
    GraphDatabaseMonitor graphDbMonitor;
    // some data
    public CustomGraphDatabaseService (GraphDatabaseService _graphDb) {
        graphDb = _graphDb;
        graphDbMonitor = new GraphDatabaseMonitorImpl();
        graphDbMonitor.connect(graphDb.databaseName());
        System.out.print("Initializing CustomGraphDatabaseService\n");
    }

    // rewrite the operaions of GraphDatabaseService:
    /**
     * Check if the database is currently in a usable state.
     * This method is equivalent to calling {@link #isAvailable(long)} with 0 as the requested timeout.
     * @return the state of the database: {@code true} if it is available, otherwise {@code false}
     * @see #isAvailable(long)
     */
    boolean isAvailable() {
        return graphDb.isAvailable();
    }

    /**
     * Check if the database is currently in a usable state.
     *
     * @param timeoutMillis timeoutMillis (in milliseconds) to wait for the database to become available.
     *   If the database has been shut down {@code false} is returned immediately.
     * @return the state of the database: {@code true} if it is available, otherwise {@code false}
     */
    boolean isAvailable(long timeoutMillis) {
        return graphDb.isAvailable(timeoutMillis);
    }

    /**
     * Starts a new {@link Transaction transaction} and associates it with the current thread.
     * <p>
     * <em>All database operations must be wrapped in a transaction.</em>
     * <p>
     * If you attempt to access the graph outside of a transaction, those operations will throw
     * {@link NotInTransactionException}.
     * <p>
     * Please ensure that any returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @return a new transaction instance
     */
    CustomTransaction beginTx() {
        CustomTransaction Tx = new CustomTransaction(graphDb.beginTx(), this);
        graphDbMonitor.startTransaction(Tx);

        // test
        System.out.print("Opened one Transaction in CustomGraphDatabaseService\n");

        return Tx;
    }

    /**
     * Starts a new {@link Transaction transaction} with custom timeout and associates it with the current thread.
     * Timeout will be taken into account <b>only</b> when execution guard is enabled.
     * <p>
     * <em>All database operations must be wrapped in a transaction.</em>
     * <p>
     * If you attempt to access the graph outside of a transaction, those operations will throw
     * {@link NotInTransactionException}.
     * <p>
     * Please ensure that any returned {@link ResourceIterable} is closed correctly and as soon as possible
     * inside your transaction to avoid potential blocking of write operations.
     *
     * @param timeout transaction timeout
     * @param unit time unit of timeout argument
     * @return a new transaction instance
     */
    CustomTransaction beginTx(long timeout, TimeUnit unit) {
        CustomTransaction Tx = new CustomTransaction(graphDb.beginTx(timeout, unit), this);

        // test
        System.out.print("Opened one Transaction in CustomGraphDatabaseService\n");

        graphDbMonitor.startTransaction(Tx);
        return Tx;
    }

    /**
     * Executes query in a separate transaction.
     * Capable to execute queries with inner transactions.
     *
     * @param query The query to execute
     * @throws QueryExecutionException If the Query contains errors
     */
    void executeTransactionally(String query) throws QueryExecutionException {
        try{
            graphDb.executeTransactionally(query);
        }
        catch (QueryExecutionException e){ // 瞎写的
            System.out.print(e.getStatusCode());
        }
    }

    /**
     * Executes query in a separate transaction.
     * Capable to execute queries with inner transactions.
     *
     * @param query The query to execute
     * @param parameters Parameters for the query
     * @throws QueryExecutionException If the Query contains errors
     */
    void executeTransactionally(String query, Map<String, Object> parameters) throws QueryExecutionException {
        try{
            graphDb.executeTransactionally(query, parameters);
        }
        catch (QueryExecutionException e){ // 瞎写的
            System.out.print(e.getStatusCode());
        }
    }

    /**
     * Executes query in a separate transaction and allow to query result to be consumed by provided {@link ResultTransformer}.
     * Capable to execute queries with inner transactions.
     *
     * @param query The query to execute
     * @param parameters Parameters for the query
     * @param resultTransformer Query results consumer
     * @throws QueryExecutionException If the query contains errors
     */
    <T> T executeTransactionally(String query, Map<String, Object> parameters, ResultTransformer<T> resultTransformer)
            throws QueryExecutionException {
        try{
            return graphDb.executeTransactionally(query, parameters, resultTransformer);
        }
        catch (QueryExecutionException e){ // 瞎写的
            System.out.print(e.getStatusCode());
            return null;
        }
    } // ???不知道怎么搞

    /**
     * Executes query in a separate transaction and allows query result to be consumed by provided {@link ResultTransformer}.
     * If query will not gonna be able to complete within provided timeout time interval it will be terminated.
     *
     * Capable to execute queries with inner transactions.
     *
     * @param query The query to execute
     * @param parameters Parameters for the query
     * @param resultTransformer Query results consumer
     * @param timeout Maximum duration of underlying transaction
     * @throws QueryExecutionException If the query contains errors
     */
    <T> T executeTransactionally(
            String query, Map<String, Object> parameters, ResultTransformer<T> resultTransformer, Duration timeout)
            throws QueryExecutionException {
        try{
            return graphDb.executeTransactionally(query, parameters, resultTransformer, timeout);
        }
        catch (QueryExecutionException e){ // 瞎写的
            System.out.print(e.getStatusCode());
            return null;
        }
    }

    /**
     * Return name of underlying database
     * @return database name
     */
    String databaseName() {
        return graphDb.databaseName();
    }

}
