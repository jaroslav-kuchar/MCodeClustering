/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fit.krizeji1.mcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.clustering.api.Cluster;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author JiriKrizek
 */
public class KMClusterImpl implements Cluster {
    private Integer seedNode;
    private ArrayList clusterList;
    private double scoreCluster;
    private HashMap seenNodes;
    private Graph clusterSubGraph;
    private Node metanode;
    private static int sequenceid = 1;
    private String clusterName;

    public KMClusterImpl() {
        super();
    }

    
    
    @Override
    public Node[] getNodes() {
        sequenceid=1;
        return clusterSubGraph.getNodes().toArray();
        
    }

    @Override
    public int getNodesCount() {
        return clusterList.size();
    }

    @Override
    public String getName() {
        return clusterName;
    }
    
    public void setName(String name) {
        this.clusterName = name;
    }

    @Override
    public Node getMetaNode() {
        return this.metanode;
    }

    @Override
    public void setMetaNode(Node node) {
        this.metanode = node;
    }

    void setSeedNode(Integer currentNode) {
        seedNode = currentNode;
    }

    void setList(ArrayList listCluster) {
        this.clusterList = listCluster;
    }

    void setClusterScore(double scoreCluster) {
        this.scoreCluster = scoreCluster;
    }

    void setNodeSeenMap(HashMap seenNodes) {
        this.seenNodes = seenNodes;
    }

    void setSubGraph(Graph clusterSubGraph) {
        this.clusterSubGraph = clusterSubGraph;
    }
    
    Graph getSubGraph() {
        return this.clusterSubGraph;
    }
    
}
