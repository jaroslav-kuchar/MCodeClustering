# Molecular Complex Detection (MCODE) Clustering
Author: Jiri Krizek

Supervisor: Jaroslav Kuchar

The Molecular Complex Detection clustering plugin for <a href="http://www.gephi.org">Gephi</a>. 
This plugin finds clusters in graph, which can be used in Social Network Analysis. 

Clustering on Graphs: 
* details are available at http://www.biomedcentral.com/1471-2105/4/2


## Tutorial
You can start cluster finding using "Clustering" panel. This panel is usually on the left part of Gephi window. 
If you don't see this panel, enable it using "Window/Clustering" from the main menu.

From dropdown menu, select **KM Clustering**.

Then you can edit algorithm parameters using button **Settings**:
![mclparams](https://raw.github.com/jaroslav-kuchar/MCodeClustering/master/images/mcode.png)

### Parameters
* **Use haircut** - enables/disables haircut usage
* **Use fluff** - enables/disables fluff usage
* **Include loops** - enables/disables loops inclusion, when this option is disabled, node loops will be ignored

* **Degree cutoff** - Minimal degree of Node needed to include node in the computation
* **Fluff node density cutoff** - Higher value of this parameter will make clusters more "fluffed", computed clusters will be bigger.
* **Node score cutoff** - Parameter affects size of the resulting clusters.  Lower values will result in smaller clusters.
* **K-Core value** - Clusters smaller than K-Core value will be filtered out.
* **Max recursion depth** - Maximal number of recursion calls. This parameter can affect size of the clusters. Default value is very high, which means it will not have any effect.

After setting the parameters, you can start computation using **Run** button in
the Clustering panel.

## License
The GPL version 3, http://www.gnu.org/licenses/gpl-3.0.txt
