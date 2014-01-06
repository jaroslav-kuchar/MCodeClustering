package cz.cvut.fit.krizeji1.mcode;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Jiri Krizek <krizeji1 at fit.cvut.cz>
 */
class NodeMetrics {
    private final Node node;
    double score;
    
    public NodeMetrics(Node node) {
        this.node = node;
        this.numNeigh = 0;
        this.density = 0;
        this.coreDensity = 0;
        this.coreLevel = 0;
        this.score = 0;
    }
    int coreLevel; // 2 = 2-core
    double coreDensity; // density of core neighborhood
    double density; // neigh density
    private int numNeigh; // number of neighbors
    private int[] neighBors;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Node ");
        sb.append(node.getId()).append("; \n  numNeigh = ").append(getNumNeigh());
        sb.append("; \n  density = ").append(density).append("; \n  coreDensity: ");
        sb.append(coreDensity).append("; \n  coreLevel = ").append(coreLevel).append("\n  ")
                .append("score = ").append(score).append("\n");
        return sb.toString();
    }    

    /**
     * @return the neighBors
     */
    public int[] getNeighBors() {
        return neighBors;
    }

    /**
     * @param neighBors the neighBors to set
     */
    public void setNeighBors(int[] neighBors) {
        this.neighBors = neighBors;
        this.numNeigh = neighBors.length;
    }

    /**
     * @return the numNeigh
     */
    public int getNumNeigh() {
        return numNeigh;
    }
    
    
    
    
}
