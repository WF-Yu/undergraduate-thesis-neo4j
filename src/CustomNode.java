import org.neo4j.graphdb.*;
import scala.Int;

import java.util.Iterator;

public class CustomNode {
    Node node;
    CustomGraphDatabaseService customGraphDb;
    Integer TxID;
    public CustomNode(Node _node, Integer _TxID, CustomGraphDatabaseService _customGraph){
        node = _node;
        TxID = _TxID;
        customGraphDb = _customGraph;

        //??????要是能这儿写，这不直接就行了吗？？？？
        customGraphDb.graphDbMonitor.uploadNodeRead(TxID, node);
    }
    void delete(){
        node.delete();
    }
    ResourceIterable<CustomRelationship> getRelationships(){
        ResourceIterable<Relationship> relationships = node.getRelationships();
        customGraphDb.graphDbMonitor.uploadRelationshipRead(TxID.hashCode(), relationships);
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
                        return new CustomRelationship(relationship, TxID, customGraphDb); // 根据Node对象创建customNode对象并返回
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

    boolean hasRelationship(){
        return node.hasRelationship();
    }
    ResourceIterable<CustomRelationship> getRelationships(RelationshipType... types){
        ResourceIterable<Relationship> relationships = node.getRelationships(types);
        customGraphDb.graphDbMonitor.uploadRelationshipRead(TxID.hashCode(), relationships);
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
                        return new CustomRelationship(relationship, TxID, customGraphDb); // 根据Node对象创建customNode对象并返回
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

    ResourceIterable<CustomRelationship> getRelationships(Direction direction, RelationshipType... types){
        ResourceIterable<Relationship> relationships = node.getRelationships(direction, types);
        customGraphDb.graphDbMonitor.uploadRelationshipRead(TxID.hashCode(), relationships);
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
                        return new CustomRelationship(relationship, TxID, customGraphDb); // 根据Node对象创建customNode对象并返回
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
    boolean hasRelationship(RelationshipType... types){
        return node.hasRelationship(types);
    }
    boolean hasRelationship(Direction direction, RelationshipType... types){
        return node.hasRelationship(direction, types);
    }
    ResourceIterable<CustomRelationship> getRelationships(Direction dir){
        ResourceIterable<Relationship> relationships = node.getRelationships(dir);
        customGraphDb.graphDbMonitor.uploadRelationshipRead(TxID.hashCode(), relationships);
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
                        return new CustomRelationship(relationship, TxID, customGraphDb); // 根据Node对象创建customNode对象并返回
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
    boolean hasRelationship(Direction dir){
        return node.hasRelationship(dir);
    }
    CustomRelationship getSingleRelationship(RelationshipType type, Direction dir){
        Relationship relationship = node.getSingleRelationship(type, dir);
        customGraphDb.graphDbMonitor.uploadRelationshipRead(TxID, relationship);
        return new CustomRelationship(relationship, TxID, customGraphDb);
    }

    CustomRelationship createRelationshipTo(CustomNode otherNode, RelationshipType type){
        return new CustomRelationship(node.createRelationshipTo(otherNode.node, type), TxID, customGraphDb);
    }
    Iterable<RelationshipType> getRelationshipTypes(){
        return node.getRelationshipTypes();
    }
    int getDegree(){
        return node.getDegree();
    }
    int getDegree(RelationshipType type){
        return node.getDegree(type);
    }

    int getDegree(Direction direction){
        return node.getDegree(direction);
    }

    int getDegree(RelationshipType type, Direction direction){
        return node.getDegree(type, direction);
    }

    void addLabel(Label label){
        node.addLabel(label);
    }

    void removeLabel(Label label){
        node.removeLabel(label);
    }

    boolean hasLabel(Label label){
        return node.hasLabel(label);
    }

    Iterable<Label> getLabels(){
        return node.getLabels();
    }
}
