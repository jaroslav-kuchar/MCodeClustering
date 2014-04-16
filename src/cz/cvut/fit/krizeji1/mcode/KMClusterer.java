/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fit.krizeji1.mcode;

import cz.cvut.fit.krizeji1.multicolour.attribute.GraphColorizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.filters.plugin.graph.KCoreBuilder;
import org.gephi.filters.plugin.graph.KCoreBuilder.KCoreFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author JiriKrizek
 */
public class KMClusterer implements Clusterer, LongTask {

    static String PLUGIN_NAME = "KM Clustering";
    static String PLUGIN_DESCRIPTION = "KM Clustering";
    private KMParams params;
    private GraphModel graphModel;
    private boolean isCancelled;
    private ProgressTicket progress;
    private static final Logger logger = Logger.getLogger(KMClusterer.class.getName());
    private KMClusterImpl[] result;
    private int kCoreValue;
    private Map<Integer, Integer> nodeToId;
    private Map<Integer, Node> idToNode;
    private HashMap nodeInfoHashMap;

    public KMClusterer() {
        this.params = new KMParams();
    }

    private int getSeqId(Node n) {
        return nodeToId.get(n.getId());
    }

    @Override
    public void execute(org.gephi.graph.api.GraphModel gm) {
        long startAlg = System.currentTimeMillis();
        this.graphModel = gm;
        this.isCancelled = false;

        if (progress != null) {
            this.progress.start();
            this.progress.progress(NbBundle.getMessage(KMClusterer.class, "KMClusterer.setup"));
        }

        logger.log(Level.INFO, "Algorithm started\n Parameters: Degree cutoff: {0}; maxDepth: {1}; nodeScoreCutoff: {2}; kCore: {3}; isFluff: {4}; isHaircut: {5}; isIncludeLoops: {6}",
                new Object[]{getParams().getDegreeCutoff(), getParams().getMaxDepth(), getParams().getNodeScoreCutoff(), getParams().getkCore(), getParams().isFluff(), getParams().isHaircut(), getParams().isIncludeLoops()});

        nodeToId = new HashMap<Integer, Integer>();
        idToNode = new HashMap<Integer, Node>();
        nodeInfoHashMap = new HashMap();

        Graph g = graphModel.getGraph();

        Integer sequenceId = 0;
        for (Node n : g.getNodes()) {
            nodeToId.put(n.getId(), sequenceId);
            idToNode.put(sequenceId, n);
            sequenceId++;
        }
        //dumpMap(nodeToId);

        TreeMap nodeScoreSortedMap = new TreeMap(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                double d1 = ((Double) o1).doubleValue();
                double d2 = ((Double) o2).doubleValue();
                if (d1 == d2) {
                    return 0;
                } else if (d1 < d2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });


        Graph tempGraph = graphModel.getGraph();
        tempGraph.writeLock();

        int N = nodeToId.size();

        logger.log(Level.INFO, "Stage 1 - procedure MCODE-VERTEX-WEIGHTING");
        long stage1 = System.currentTimeMillis();
        ArrayList list;
        for (Node n : idToNode.values()) {
            

            NodeMetrics metric = calcNodeMetrics(tempGraph, n);
            nodeInfoHashMap.put(new Integer(getSeqId(n)), metric);
            double score = scoreNode(metric);
            if (nodeScoreSortedMap.containsKey(new Double(score))) {
                list = (ArrayList) nodeScoreSortedMap.get(new Double(score));
                list.add(getSeqId(n));
            } else {
                list = new ArrayList();
                list.add(new Integer(getSeqId(n)));
                nodeScoreSortedMap.put(new Double(score), list);
            }
        }
        System.out.println("end stage 1: "+(System.currentTimeMillis()-stage1));
        logger.log(Level.INFO, "Stage 1 ended");
        
        logger.log(Level.INFO, "Stage 2 & 3");
        long stage2 = System.currentTimeMillis();
        result = findClusters(nodeScoreSortedMap);

        AttributeTable nodeTable = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        GraphColorizer c = new GraphColorizer(nodeTable);
        c.colorizeGraph(result);

        tempGraph.writeUnlock();
        if (progress != null) {
            this.progress.finish(NbBundle.getMessage(KMClusterer.class, "KMClusterer.finished"));
        }
        long time = System.currentTimeMillis();
        
        System.out.println("endStage 2 & 3: "+(time-stage2));
        System.out.println("endAlg: "+(time-startAlg));
    }

    @Override
    public Cluster[] getClusters() {
        if (result == null || result.length == 0) {
            return null;
        } else {
            return result;
        }
    }

    @Override
    public boolean cancel() {
        return isCancelled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progress = pt;
    }

    private NodeMetrics calcNodeMetrics(Graph graph, Node sourceNode) {
        Node[] neighborhood;

        Node[] neighbors = graph.getNeighbors(sourceNode).toArray();
        // If node has only one or no neigbors, calculation is easy
        if (neighbors.length < 2) {
            NodeMetrics metric = new NodeMetrics(sourceNode);
            if (neighbors.length == 1) {
                metric.coreLevel = 1;
                metric.coreDensity = 1.0;
                metric.density = 1.0;
            }
            return metric;
        }

        // add original node to array - array neighborhood will contain node neighbors and itself
        neighborhood = new Node[neighbors.length + 1];
        System.arraycopy(neighbors, 0, neighborhood, 1, neighbors.length);
        neighborhood[0] = sourceNode;

        // Get neigh subgraph
        Graph neighGraph = createNeighSubgraph(neighborhood);
        NodeMetrics metric = new NodeMetrics(sourceNode);
        metric.density = calcDensity(neighGraph, getParams().isIncludeLoops());
        metric.setNeighBors(translateToIDs(neighborhood));

        // calculate the highest k-core
        KCoreResult res = getHighestKCore(neighGraph);
        Graph graphCore = res.getGraph();
        metric.coreLevel = res.getK();

        if (graphCore != null) {
            metric.coreDensity = calcDensity(graphCore, getParams().isIncludeLoops());
        } 

        return metric;
    }

    private double calcDensity(Graph graph, boolean includeLoops) {
        int possibleEdgeNum = 0, actualEdgeNum = 0, loopCount = 0;
        double density = 0;

        if (includeLoops) {
            for (Edge e : graph.getEdges()) {
                if (e.isSelfLoop()) {
                    loopCount++;
                }
            }
        }

        possibleEdgeNum = graph.getNodeCount() * graph.getNodeCount();
        actualEdgeNum = graph.getEdgeCount() - loopCount;

        density = (double) actualEdgeNum / (double) possibleEdgeNum;
        return density;
    }

    private KCoreResult getHighestKCore(Graph graph) {
        int i = 1;
        int nodeCount = 1;

        Graph filteredGraph = graph;
        Graph prevGraph = null;

        KCoreFilter kCoreFilter = new KCoreBuilder.KCoreFilter();
        GraphView view = graph.getView();
        
        GraphView oldView = graph.getGraphModel().copyView(view);

        while (nodeCount != 0) {
            kCoreFilter.setK(i++);

            oldView = view;
            view = filteredGraph.getGraphModel().copyView(oldView);
            
            prevGraph = filteredGraph;
            filteredGraph = graphModel.getGraph(view);
            filteredGraph = kCoreFilter.filter(filteredGraph);

            nodeCount = filteredGraph.getNodeCount();
            
            if (oldView != null && nodeCount!=0) {
                // Destroy view from previous cycle
                graphModel.destroyView(oldView);
            }
        }
        /*if (view != null) {
            graphModel.destroyView(view);
        }*/
        Integer k = new Integer(i - 1);
        return new KCoreResult(k, prevGraph);
    }

    private Graph createNeighSubGraph(ArrayList list) {
        Set<Integer> set = new HashSet(list);
        GraphView neighView = graphModel.newView();
        Graph neighGraph = graphModel.getGraph(neighView);

        Node[] nodes = neighGraph.getNodes().toArray();

        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];

            int nodeSeqId = getSeqId(n);
            if (!set.contains(nodeSeqId)) {
                neighGraph.removeNode(n);
            }
        }
        return neighGraph;
    }

    private Graph createNeighSubgraph(Node[] neighborhood) {
        GraphView neighView = graphModel.newView();
        Graph neighGraph = graphModel.getGraph(neighView);

        Set<Integer> set = new HashSet<Integer>();
        for(Node n : neighborhood) {
            set.add(n.getId());
        }

        for (Node n : neighGraph.getNodes().toArray()) {
            if (!set.contains(n.getId())) {
                neighGraph.removeNode(n);
            }
        }
        return neighGraph;
    }

    private double scoreNode(NodeMetrics metric) {
        if (metric.getNumNeigh() > getParams().getDegreeCutoff()) {
            metric.score = metric.coreDensity * (double) metric.coreLevel;
        } else {
            metric.score = 0.0;
        }
        return metric.score;
    }

    private KMClusterImpl[] findClusters(TreeMap nodeScoreSortedMap) {
        int findingTotal = 0;
        HashMap<Integer, Boolean> seenNodes = new HashMap<Integer, Boolean>();
        Integer currentNode;
        KMClusterImpl currentCluster;
        Collection values = nodeScoreSortedMap.values();
        for (Iterator i1 = values.iterator(); i1.hasNext();) {
            ArrayList value = (ArrayList) i1.next();

            for (Iterator i2 = value.iterator(); i2.hasNext();) {
                i2.next();
                findingTotal++;
            }
        }

        ArrayList<KMClusterImpl> listClusters = new ArrayList<KMClusterImpl>();
        ArrayList listNodesWithSameScore;
        int clusterId = 1;
        for (Iterator it = values.iterator(); it.hasNext();) {
            listNodesWithSameScore = (ArrayList) it.next();
            for (int j = 0; j < listNodesWithSameScore.size(); j++) {
                currentNode = (Integer) listNodesWithSameScore.get(j);

                if (!seenNodes.containsKey(currentNode)) {
                    currentCluster = new KMClusterImpl();
                    currentCluster.setSeedNode(currentNode);

                    ArrayList listClusterNodes = getClusterCore(currentNode, seenNodes, getParams().getMaxDepth());

                    if (listClusterNodes.size() > 0) {
                        if (!listClusterNodes.contains(currentNode)) {
                            listClusterNodes.add(currentNode);
                        }

                        Graph clusterSubGraph = createNeighSubGraph(listClusterNodes);
                        if (!filterCluster(clusterSubGraph)) {
                            if (getParams().isHaircut()) {
                                haircutCluster(clusterSubGraph, listClusterNodes);
                            }

                            if (getParams().isFluff()) { 
                                fluffCluster(listClusterNodes, seenNodes, nodeInfoHashMap);
                            }

                            currentCluster.setSubGraph(clusterSubGraph);
                            currentCluster.setList(listClusterNodes);
                            currentCluster.setClusterScore(scoreCluster(currentCluster));
                            currentCluster.setNodeSeenMap(seenNodes);
                            currentCluster.setName("Cluster " + clusterId++);
                            listClusters.add(currentCluster);
                        }
                    } else {
                        //Listcluster size <= 0
                    }
                } else {
                    //seenNodes contain node currentNode
                    //dumpIBMap(seenNodes);
                }

            }
        }
        return listClusters.toArray(new KMClusterImpl[0]);
    }

    public double scoreCluster(KMClusterImpl cluster) {
        int numNodes = 0;
        double density = 0.0, score = 0.0;

        numNodes = cluster.getNodesCount();
        density = calcDensity(cluster.getSubGraph(), true);
        score = density * numNodes;
        return score;
    }

    private ArrayList getClusterCore(Integer startNode, HashMap<Integer, Boolean> seenNodes, int maxDepth) {
        ArrayList cluster = new ArrayList();
        getClusterCoreInternal(startNode, seenNodes, ((NodeMetrics) nodeInfoHashMap.get(startNode)).score, 1, cluster, maxDepth);
        return cluster;
    }

    private boolean getClusterCoreInternal(Integer startNode, HashMap<Integer, Boolean> seenNodes, double startNodeScore, int currentDepth, ArrayList cluster, int maxDepth) {
        if (seenNodes.containsKey(startNode)) {
            return true;
        }
        seenNodes.put(startNode, true);

        if (currentDepth > maxDepth) {
            return true;
        }

        Integer currentNeigh;
        int i;
        for (i = 0; i < (((NodeMetrics) nodeInfoHashMap.get((startNode))).getNumNeigh()); i++) {
            currentNeigh = ((NodeMetrics) nodeInfoHashMap.get(startNode)).getNeighBors()[i];
            //Node neighGraph.getNode(nodeInnerId);
            //Node nObj = ((NodeMetrics) nodeInfoHashMap.get(startNode)).nodeNeighbors[i];
            if (!seenNodes.containsKey(currentNeigh)
                    && ((((NodeMetrics) nodeInfoHashMap.get(currentNeigh)).score) >= (startNodeScore - startNodeScore * getParams().getNodeScoreCutoff()))) {
                if (!cluster.contains(currentNeigh)) {
                    cluster.add(currentNeigh);
                }
                getClusterCoreInternal(currentNeigh, seenNodes, startNodeScore, currentDepth + 1, cluster, maxDepth);
            } /*else {
                logger.log(Level.INFO, "  seenNodes contains key {0}", currentNeigh);
            }*/
        }

        return true;
    }

    private void tmpdumpNodeScoreMap(TreeMap nodeScoreSortedMap) {
        Iterator it = nodeScoreSortedMap.values().iterator();
        logger.log(Level.INFO, "size: {0}", nodeScoreSortedMap.values().size());
        while (it.hasNext()) {
            logger.log(Level.INFO, "  {0}", it.next());
        }
    }

    private int[] translateToIDs(Node[] neighborhood) {
        int[] res = new int[neighborhood.length];

        for (int i = 0; i < neighborhood.length; i++) {
            res[i] = nodeToId.get(neighborhood[i].getId());
        }
        return res;
    }

    private void dumpMap(Map map, String str) {
        StringBuilder s = new StringBuilder("Map ");
        s.append(str);
        s.append(": [");
        Iterator<Integer> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            int num = iterator.next();
            Node n = idToNode.get(num);
            int id = n.getId();
            s.append(n);
            s.append("(");
            s.append(id);
            s.append(")");
            s.append(" -> ");
            s.append(num);
            s.append("; ");
        }
        s.append("]");
        logger.log(Level.INFO, s.toString());
    }

    
    public KMParams getParams() {
        return this.params;
    }

    public void setParams(KMParams params) {
        this.params = params;
    }

    private void haircutCluster(Graph clusterSubGraph, ArrayList cluster) {
        Graph kCore = getKCore(clusterSubGraph, 2);
        if (kCore != null) {
            // remove all nodes from the given graph
            cluster.clear();
            // add only 2-core graph part
            for (Node n : kCore.getNodes()) {
                cluster.add(getSeqId(n));
            }
        }
    }

    private void fluffCluster(ArrayList cluster, HashMap<Integer, Boolean> seenNodes, HashMap nodeInfoHashMap) {
        int currentNode = 0;
        int currentNeigh = 0;
        List nodesToAdd = new ArrayList();
        HashMap seenNodesInternal = new HashMap();

        for (int i = 0; i < cluster.size(); i++) {
            currentNode = (Integer) cluster.get(i);

            for (int j = 0; j < ((NodeMetrics) nodeInfoHashMap.get(currentNode)).getNumNeigh(); j++) {
                currentNeigh = ((NodeMetrics) nodeInfoHashMap.get(currentNode)).getNeighBors()[j];
                if ((!seenNodes.containsKey(currentNeigh)) && (!seenNodesInternal.containsKey(currentNeigh))
                        && ((NodeMetrics) nodeInfoHashMap.get(currentNeigh)).density > getParams().getFluffNodeDensityCutoff()) {
                    nodesToAdd.add(currentNeigh);
                    seenNodesInternal.put(currentNeigh, true);
                }
            }
        }

        if (nodesToAdd.size() > 0) {
            cluster.add(nodesToAdd);
        }
    }

    private boolean filterCluster(Graph clusterSubGraph) {
        if (clusterSubGraph == null) {
            return true;
        }
        // filter if cluster does not satisfy user specified k-core
        Graph filtered = getKCore(clusterSubGraph, getParams().getkCore());
        if (filtered == null) {
            return true;
        }
        return false;
    }

    private Graph getKCore(Graph clusterSubGraph, int kCore) {
        KCoreFilter kCoreFilter = new KCoreBuilder.KCoreFilter();
        kCoreFilter.setK(kCore);
        Graph filteredGraph = kCoreFilter.filter(clusterSubGraph);
        if (filteredGraph.getNodeCount() == 0) {
            return null;
        }
        return filteredGraph;
    }

    class KCoreResult {

        private final int k;
        private final Graph graph;

        private KCoreResult(Integer k, Graph graph) {
            this.k = k.intValue();
            this.graph = graph;
        }

        int getK() {
            return this.k;
        }

        Graph getGraph() {
            return this.graph;
        }
    }
}
