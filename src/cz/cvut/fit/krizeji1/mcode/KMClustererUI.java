/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.fit.krizeji1.mcode;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererUI;
import org.openide.util.NbBundle;

/**
 *
 * @author JiriKrizek
 */
public class KMClustererUI implements ClustererUI {

    private JPanel panel;
    private JCheckBox haircutCheckbox;
    private JCheckBox fluffCheckbox;
    private JCheckBox includeLoopsCheckbox;
    private JTextField kCoreField;
    private JTextField nodeScoreCutoffField;
    private JTextField degreeCutoffField;
    private JTextField maxDepthField;
    private KMClusterer clusterer;
    private JTextField fluffCutoffField;
    

    public KMClustererUI() {
        panel = new JPanel();
        initComponents();
    }

    
    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Clusterer clstr) {
        this.clusterer = (KMClusterer) clstr;
        KMParams params = this.clusterer.getParams();
        
        kCoreField.setText(Integer.toString(params.getkCore()));
        nodeScoreCutoffField.setText(Double.toString(params.getNodeScoreCutoff()));
        degreeCutoffField.setText(Integer.toString(params.getDegreeCutoff()));
        maxDepthField.setText(Integer.toString(params.getMaxDepth()));
        fluffCutoffField.setText(Double.toString(params.getFluffNodeDensityCutoff()));
        
        haircutCheckbox.setSelected(params.isHaircut());
        fluffCheckbox.setSelected(params.isFluff());
        includeLoopsCheckbox.setSelected(params.isIncludeLoops());
        
        fluffCutoffField.setEnabled(params.isFluff());
    }

    @Override
    public void unsetup() {
        KMParams params = clusterer.getParams();
        try {
            int kCoreValue = Integer.parseInt(kCoreField.getText());
            params.setkCore(kCoreValue);
        } catch (NumberFormatException ex) {
            //ignore invalid value
        }
        
        try {
            double nodeScoreCutoff = Double.parseDouble(nodeScoreCutoffField.getText());
            params.setNodeScoreCutoff(nodeScoreCutoff);
        } catch (NumberFormatException ex) {
            //ignore invalid value
        }
        
        try {
            int degreeCutoff = Integer.parseInt(degreeCutoffField.getText());
            params.setDegreeCutoff(degreeCutoff);
        } catch (NumberFormatException ex) {
            //ignore invalid value
        }
        
        try {
            int maxDepth = Integer.parseInt(maxDepthField.getText());
            params.setDegreeCutoff(maxDepth);
        } catch (NumberFormatException ex) {
            //ignore invalid value
        }
        
        try {
            double fluffCutoff = Double.parseDouble(fluffCutoffField.getText());
            params.setFluffNodeDensityCutoff(fluffCutoff);
        } catch (NumberFormatException ex) {
            // ignore invalid value
        }
        
        params.setHaircut(haircutCheckbox.isSelected());
        params.setFluff(fluffCheckbox.isSelected());
        params.setIncludeLoops(includeLoopsCheckbox.isSelected());
        
        this.clusterer.setParams(params);
    }

    private void initComponents() {
        ParameterContainer container = new ParameterContainer();
             
        String haircutCbLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.haircutCbLabel");
        this.haircutCheckbox = container.addCheckbox(haircutCbLabel, true);
        
        String fluffCbLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.fluffCbLabel");
        this.fluffCheckbox = container.addCheckbox(fluffCbLabel, false);
        
        String includeLoopsLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.includeLoopsLabel");
        this.includeLoopsCheckbox = container.addCheckbox(includeLoopsLabel, true);
        
        String degreeCutoffLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.degreeCutoffLabel");
        this.degreeCutoffField = container.addInput(degreeCutoffLabel);
        
        String fluffCutoffLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.fluffCutoffLabel");
        this.fluffCutoffField = container.addInput(fluffCutoffLabel);
        
        fluffCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                KMClustererUI.this.fluffCutoffField.setEnabled(KMClustererUI.this.fluffCheckbox.isSelected());
            }
        });
        
        String nodeScoreCutoffLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.nodeScoreCutoffLabel");
        this.nodeScoreCutoffField = container.addInput(nodeScoreCutoffLabel);
        
        String kCoreLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.KCoreValueLabel");
        this.kCoreField = container.addInput(kCoreLabel);
        
        String maxDepthLabel = NbBundle.getMessage(KMClustererUI.class, "KMClustererUI.maxDepthLabel");
        this.maxDepthField = container.addInput(maxDepthLabel);
        
        Logger.getLogger(KMClustererUI.class.getName()).log(Level.INFO, "{0}{1}{2}", new Object[]{degreeCutoffLabel, nodeScoreCutoffLabel, kCoreLabel});
        
        Logger.getLogger(KMClustererUI.class.getName()).log(Level.INFO, this.degreeCutoffField.toString());
        Logger.getLogger(KMClustererUI.class.getName()).log(Level.INFO, this.nodeScoreCutoffField.toString());
        Logger.getLogger(KMClustererUI.class.getName()).log(Level.INFO, this.kCoreField.toString());
        
        
        panel.add(container);
    }
}
