import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;

import java.util.Iterator;
import java.util.Map;

public class CustomTransaction implements AutoCloseable{
    Transaction Tx;
    CustomGraphDatabaseService customGraphDb;
    public CustomTransaction (Transaction _Tx, CustomGraphDatabaseService _graphDb) {
        Tx = _Tx;
        customGraphDb = _graphDb;
    }
    CustomNode createNode(Label... labels){
        return new CustomNode(Tx.createNode(labels), Tx.hashCode(), customGraphDb);
    }
    ResourceIterable<CustomNode> getAllNodes() {
        ResourceIterable<Node> nodes= Tx.getAllNodes();
        customGraphDb.graphDbMonitor.uploadNodeRead(Tx.hashCode(), nodes);
        return new ResourceIterable<>() {
            @Override
            public ResourceIterator<CustomNode> iterator() {
                final Iterator<Node> nodeIterator = nodes.iterator(); // 获取Node对象的迭代器
                return new ResourceIterator<>() {
                    @Override
                    public boolean hasNext() {
                        return nodeIterator.hasNext(); // 判断是否还有下一个Node对象
                    }
                    @Override
                    public CustomNode next() {
                        Node node = nodeIterator.next(); // 获取下一个Node对象
                        return new CustomNode(node, Tx.hashCode(), customGraphDb); // 根据Node对象创建customNode对象并返回
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException(); // 不支持删除操作
                    }
                    @Override
                    public void close(){
                        // 关闭资源
                        try {
                            if (nodes instanceof AutoCloseable) {
                                ((AutoCloseable) nodes).close();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                };
            }
            @Override
            public void close(){
                // 关闭资源
                try {
                    if (nodes instanceof AutoCloseable) {
                        ((AutoCloseable) nodes).close();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    public void close(){
        Tx.close();
    }

    CustomNode getNodeByElementId(String elementId){
        Node node = Tx.getNodeByElementId(elementId);
        customGraphDb.graphDbMonitor.uploadNodeRead(Tx.hashCode(), node);
        return new CustomNode(node, Tx.hashCode(), customGraphDb);
    }

    CustomRelationship getRelationshipByElementId(String elementId){
        Relationship relationship = Tx.getRelationshipByElementId(elementId);
        // upload to monitor
        customGraphDb.graphDbMonitor.uploadRelationshipRead(Tx.hashCode(),relationship);
        return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
    }

    BidirectionalTraversalDescription bidirectionalTraversalDescription(){
        return Tx.bidirectionalTraversalDescription();
    }

    TraversalDescription traversalDescription(){
        return Tx.traversalDescription();
    }

    Result execute(String query) throws QueryExecutionException{
        return Tx.execute(query);
    }

    Result execute(String query, Map<String, Object> parameters) throws QueryExecutionException{
        return Tx.execute(query, parameters);
    }

    Iterable<Label> getAllLabelsInUse(){
        return Tx.getAllLabelsInUse();
    }

    Iterable<RelationshipType> getAllRelationshipTypesInUse(){
        return Tx.getAllRelationshipTypesInUse();
    }

    Iterable<Label> getAllLabels(){
        return Tx.getAllLabels();
    }

    Iterable<RelationshipType> getAllRelationshipTypes(){
        return Tx.getAllRelationshipTypes();
    }

    Iterable<String> getAllPropertyKeys(){
        return Tx.getAllPropertyKeys();
    }


    ResourceIterator<CustomNode> findNodes(Label label, String key, String template, StringSearchMode searchMode){
        ResourceIterator<Node> nodes = Tx.findNodes(label, key, template, searchMode);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }

    ResourceIterator<CustomNode> findNodes(Label label, Map<String, Object> propertyValues){
        ResourceIterator<Node> nodes = Tx.findNodes(label, propertyValues);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }

    ResourceIterator<CustomNode> findNodes(
            Label label, String key1, Object value1, String key2, Object value2, String key3, Object value3){
        ResourceIterator<Node> nodes = Tx.findNodes(label, key1, value1, key2, value2, key3, value3);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }

    ResourceIterator<CustomNode> findNodes(Label label, String key1, Object value1, String key2, Object value2){
        ResourceIterator<Node> nodes = Tx.findNodes(label, key1, value1, key2, value2);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }


    CustomNode findNode(Label label, String key, Object value){
        CustomNode customNode = new CustomNode(Tx.findNode(label, key, value), Tx.hashCode(), customGraphDb);
        /*
        import java.util.concurrent.CompletableFuture;

        // ...

        public void uploadNodeReadAsync(GraphDbMonitor graphDbMonitor, Tx tx, Node node) {
            CompletableFuture.runAsync(() -> {
                graphDbMonitor.uploadNodeRead(tx, node);
            });
        }

        // In  transaction class:
        uploadNodeReadAsync(customGraphDb.graphDbMonitor, Tx, customNode.node);
         */
        customGraphDb.graphDbMonitor.uploadNodeRead(Tx.hashCode(), customNode.node);
        return customNode;
    }

    ResourceIterator<CustomNode> findNodes(Label label, String key, Object value){
        ResourceIterator<Node> nodes = Tx.findNodes(label, key, value);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }

    ResourceIterator<CustomNode> findNodes(Label label){
        ResourceIterator<Node> nodes = Tx.findNodes(label);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return nodes.hasNext();
            }

            @Override
            public CustomNode next() {
                Node node = nodes.next();
                return new CustomNode(node, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                nodes.remove();
            }

            @Override
            public void close() {
                nodes.close();
            }
        };
    }

    ResourceIterator<CustomRelationship> findRelationships(
            RelationshipType relationshipType, String key, String template, StringSearchMode searchMode){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType, key, template, searchMode);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }
    ResourceIterator<CustomRelationship> findRelationships(
            RelationshipType relationshipType, Map<String, Object> propertyValues){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType, propertyValues);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }

    ResourceIterator<CustomRelationship> findRelationships(
            RelationshipType relationshipType,
            String key1,
            Object value1,
            String key2,
            Object value2,
            String key3,
            Object value3){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType, key1, value1, key1, value2, key3, value3);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }

    ResourceIterator<CustomRelationship> findRelationships(
            RelationshipType relationshipType, String key1, Object value1, String key2, Object value2){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType, key1, value1, key2, value2);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }

    CustomRelationship findRelationship(RelationshipType relationshipType, String key, Object value){
        Relationship relationship = Tx.findRelationship(relationshipType, key, value);
        customGraphDb.graphDbMonitor.uploadRelationshipRead(Tx.hashCode(), relationship);
        return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
    }

    ResourceIterator<CustomRelationship> findRelationships(RelationshipType relationshipType, String key, Object value){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType, key, value);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }

    ResourceIterator<CustomRelationship> findRelationships(RelationshipType relationshipType){
        ResourceIterator<Relationship> relationships = Tx.findRelationships(relationshipType);
        return new ResourceIterator<>() {
            @Override
            public boolean hasNext() {
                return relationships.hasNext();
            }

            @Override
            public CustomRelationship next() {
                Relationship relationship = relationships.next();
                return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb);
            }

            @Override
            public void remove() {
                relationships.remove();
            }

            @Override
            public void close() {
                relationships.close();
            }
        };
    }
    ResourceIterable<CustomRelationship> getAllRelationships(){
        ResourceIterable<Relationship> relationships = Tx.getAllRelationships();
        customGraphDb.graphDbMonitor.uploadRelationshipRead(Tx.hashCode(), relationships);
        return new ResourceIterable<>() {
            @Override
            public ResourceIterator<CustomRelationship> iterator() {
                final Iterator<Relationship> relationshipIterator = relationships.iterator(); // 获取Node对象的迭代器
                return new ResourceIterator<>() {
                    @Override
                    public boolean hasNext() {
                        return relationshipIterator.hasNext(); // 判断是否还有下一个Node对象
                    }

                    @Override
                    public CustomRelationship next() {
                        Relationship relationship = relationshipIterator.next(); // 获取下一个Node对象
                        return new CustomRelationship(relationship, Tx.hashCode(), customGraphDb); // 根据Node对象创建customNode对象并返回
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException(); // 不支持删除操作
                    }

                    @Override
                    public void close() {
                        // 关闭资源
                        try {
                            if (relationships instanceof AutoCloseable) {
                                ((AutoCloseable) relationships).close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
            }

            @Override
            public void close() {
                // 关闭资源
                try {
                    if (relationships instanceof AutoCloseable) {
                        ((AutoCloseable) relationships).close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    Lock acquireWriteLock(Entity entity){
        return Tx.acquireWriteLock(entity);
    }

    Lock acquireReadLock(Entity entity){
        return Tx.acquireReadLock(entity);
    }

    Schema schema(){
        return Tx.schema();
    }
    void commit(){

        Integer txId = Tx.hashCode();
        Tx.commit();
        // call concurrent commit: delete the corresponding DataMap for this transaction
        customGraphDb.graphDbMonitor.commitInBackground(txId);
    }

}
