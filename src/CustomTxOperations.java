import org.neo4j.kernel.impl.newapi.AllStoreHolder;
import org.neo4j.kernel.impl.newapi.Operations;
import org.neo4j.storageengine.api.StorageReader;
import org.neo4j.storageengine.api.CommandCreationContext;
import org.neo4j.kernel.impl.newapi.IndexTxStateUpdater;
import org.neo4j.storageengine.api.StorageLocks;
import org.neo4j.kernel.impl.api.KernelTransactionImplementation;
import org.neo4j.kernel.impl.newapi.KernelToken;
import org.neo4j.kernel.impl.newapi.DefaultPooledCursors;
import org.neo4j.kernel.impl.api.state.ConstraintIndexCreator;
import org.neo4j.memory.MemoryTracker;
import org.neo4j.configuration.Config;
import org.neo4j.kernel.impl.constraints.ConstraintSemantics;
import org.neo4j.kernel.impl.api.index.IndexingProvidersService;
import org.neo4j.io.pagecache.context.CursorContext;

public class CustomTxOperations extends Operations { // 怎么样才能让这个方法被调用呢？
    // 这里主要是一些写的操作，delete呀什么的。感觉应该先去解决transaction的问题，再回来看这个operation怎么处理！
    public CustomTxOperations(
            AllStoreHolder allStoreHolder,
            StorageReader storageReader,
            IndexTxStateUpdater updater,
            CommandCreationContext commandCreationContext,
            StorageLocks storageLocks,
            KernelTransactionImplementation ktx,
            KernelToken token,
            DefaultPooledCursors cursors,
            ConstraintIndexCreator constraintIndexCreator,
            ConstraintSemantics constraintSemantics,
            IndexingProvidersService indexProviders,
            Config config,
            MemoryTracker memoryTracker) {
        super(allStoreHolder, storageReader, updater, commandCreationContext, storageLocks, ktx, token, cursors, constraintIndexCreator, constraintSemantics, indexProviders, config, memoryTracker);
    }
    public void initialize(CursorContext cursorContext) {
        super.initialize(cursorContext);
    }





}
