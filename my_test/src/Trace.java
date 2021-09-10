package src;

import com.xilinx.rapidwright.device.Site;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Trace {
    private List<Site> sites;
    private int distance;

    public Trace(List<Site> sites) {
        this.sites = new ArrayList<>(sites);
        Collections.shuffle(this.sites);
    }
    public Site getSite(int index) {
        return sites.get(index);
    }

    public int getTraceLength() {
        if (distance != 0) return distance;

        int totalDistance = 0;

        for (int i = 0; i < noSites(); i++) {
            Site start = getSite(i);
            Site end = getSite(i + 1 < noSites() ? i + 1 : 0);
            totalDistance += start.getTile().getManhattanDistance(end.getTile());
        }

        distance = totalDistance;
        return totalDistance;
    }

    public Trace duplicate() {
        return new Trace(new ArrayList<>(sites));
    }

    public int noSites() {
        return sites.size();
    }

    // Getters and toString()
}
