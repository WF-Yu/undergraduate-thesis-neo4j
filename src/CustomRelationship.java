import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class CustomRelationship {
    Relationship relationship;
    Integer TxID;
    CustomGraphDatabaseService customGraphDb;
    public CustomRelationship (Relationship _relationship, Integer _TxID, CustomGraphDatabaseService _customGraphDb){
        relationship = _relationship;
        TxID = _TxID;
        customGraphDb = _customGraphDb;
    }
    CustomNode getStartNode() {
        return new CustomNode(relationship.getStartNode(), TxID, customGraphDb);
    }
    CustomNode getEndNode() {
        return new CustomNode(relationship.getEndNode(), TxID, customGraphDb);
    }
    CustomNode getOtherNode(Node CustomNode) {
        return new CustomNode(relationship.getOtherNode(CustomNode), TxID, customGraphDb);
    }
    CustomNode[] getNodes() {
        Node[] nodes = relationship.getNodes();
        CustomNode[] customNodes = new CustomNode[nodes.length];
        int i = 0;
        for (Node n : nodes){
            customNodes[i] = new CustomNode(n, TxID, customGraphDb);
        }
        return customNodes;
    }
    RelationshipType getType(){
        return relationship.getType();
    }
    boolean isType(RelationshipType type){
        return relationship.isType(type);
    }
}
