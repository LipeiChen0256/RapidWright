import com.xilinx.rapidwright.design.Net;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.placer.blockplacer.Path;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * keep track of the changes in design
 * a new configuration corresponds to a new designTracker
 */
public class designTracker {
    private ArrayList<SiteInst> siteInstsPlaced;
    private ArrayList<Net> Nets;
    private double totalSysCost;

    public designTracker() {

    }

    public double getTotalSysCost() {
        return totalSysCost;
    }

    public ArrayList<SiteInst> getSiteInstsPlaced() {
        return siteInstsPlaced;
    }

    public ArrayList<Net> getNets() {
        return Nets;
    }

    public designTracker(Collection<SiteInst> siteInstsPlaced, Collection<Net> nets) {
        this.siteInstsPlaced = new ArrayList<>(siteInstsPlaced);
        this.Nets = new ArrayList<>(nets);
    }

    public double calSysCost(){
        double totalLength = 0.0;
        for(Net n : Nets) {
            Trace trace = new Trace(n.getSiteInsts());
            trace.calculateHPWL();
            totalLength = totalLength + trace.getDistance();
        }
        totalSysCost = totalLength;
        return totalLength;
    }

    public designTracker duplicate() {
        return new designTracker(this.getSiteInstsPlaced(), this.getNets());
    }
}
