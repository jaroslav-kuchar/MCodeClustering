package cz.cvut.fit.krizeji1.mcode;

/**
 *
 * @author Jiri Krizek <krizeji1 at fit.cvut.cz>
 */
public class KMParams {
    
    // scoring stage
    private boolean isIncludeLoops;
    private int degreeCutoff;
    private int kCore;
    
    // cluster finding stage
    private int maxDepth;
    private double nodeScoreCutoff;
    private boolean isHaircut;
    private boolean isFluff;
    private double fluffNodeDensityCutoff;

    

    public KMParams() {
        this.isHaircut = true;
        this.isFluff = false;
        this.isIncludeLoops = false;

        this.degreeCutoff = 2;
        this.nodeScoreCutoff = 0.2;
        this.kCore = 2;
        this.maxDepth = 100;
        this.fluffNodeDensityCutoff = 0.1;
    }

    /**
     * @return the isHaircut
     */
    public boolean isHaircut() {
        return isHaircut;
    }

    /**
     * @param isHaircut the isHaircut to set
     */
    public void setHaircut(boolean isHaircut) {
        this.isHaircut = isHaircut;
    }

    /**
     * @return the isFluff
     */
    public boolean isFluff() {
        return isFluff;
    }

    /**
     * @param isFluff the isFluff to set
     */
    public void setFluff(boolean isFluff) {
        this.isFluff = isFluff;
    }

    /**
     * @return the isIncludeLoops
     */
    public boolean isIncludeLoops() {
        return isIncludeLoops;
    }

    /**
     * @param isIncludeLoops the isIncludeLoops to set
     */
    public void setIncludeLoops(boolean isIncludeLoops) {
        this.isIncludeLoops = isIncludeLoops;
    }

    /**
     * @return the degreeCutoff
     */
    public int getDegreeCutoff() {
        return degreeCutoff;
    }

    /**
     * @param degreeCutoff the degreeCutoff to set
     */
    public void setDegreeCutoff(int degreeCutoff) {
        this.degreeCutoff = degreeCutoff;
    }

    /**
     * @return the nodeScoreCutoff
     */
    public double getNodeScoreCutoff() {
        return nodeScoreCutoff;
    }

    /**
     * @param nodeScoreCutoff the nodeScoreCutoff to set
     */
    public void setNodeScoreCutoff(double nodeScoreCutoff) {
        this.nodeScoreCutoff = nodeScoreCutoff;
    }

    /**
     * @return the kCore
     */
    public int getkCore() {
        return kCore;
    }

    /**
     * @param kCore the kCore to set
     */
    public void setkCore(int kCore) {
        this.kCore = kCore;
    }

    /**
     * @return the maxDepth
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * @param maxDepth the maxDepth to set
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * @return the fluffNodeDensityCutoff
     */
    public double getFluffNodeDensityCutoff() {
        return fluffNodeDensityCutoff;
    }

    /**
     * @param fluffNodeDensityCutoff the fluffNodeDensityCutoff to set
     */
    public void setFluffNodeDensityCutoff(double fluffNodeDensityCutoff) {
        this.fluffNodeDensityCutoff = fluffNodeDensityCutoff;
    }

}
