/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fit.krizeji1.mcode;

import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author JiriKrizek
 */
@ServiceProvider(service = ClustererBuilder.class)
public class KMClustererBuilder<T> implements ClustererBuilder {

    @Override
    public Clusterer getClusterer() {
        return new KMClusterer();
    }

    @Override
    public String getName() {
        return KMClusterer.PLUGIN_NAME;
    }

    @Override
    public String getDescription() {
        return KMClusterer.PLUGIN_DESCRIPTION;
    }

    @Override
    public Class getClustererClass() {
        return KMClusterer.class;
    }

    @Override
    public ClustererUI getUI() {
        return new KMClustererUI();
    }
    
}
