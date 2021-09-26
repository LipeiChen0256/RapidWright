import com.xilinx.rapidwright.design.Net;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.device.Site;
import com.xilinx.rapidwright.device.Tile;

import java.sql.Array;
import java.util.*;

public class Trace {
    private ArrayList<SiteInst> siteInsts; //siteInsts to be placed/moved
    private int distance;

    public Trace(Collection<SiteInst> siteInsts) {
        this.siteInsts = new ArrayList<>(siteInsts);
    }

    //TODO
//    public int calculatedLength() {
//        distance = 0;
//        int tmpLen = 0;
//        int size = siteInsts.size();
//        Tile tmp = siteInsts.get(0).getTile();
//        for (int i = 1; i < size; i++) {
//            Tile next = siteInsts.get(i).getTile();
//            //TODO ManhattenDistance or BoundingBox
//            distance += tmp.getTileManhattanDistance(next);// * (get(i).getPin().getNet().getFanOut())/2;
//            tmp = next;
//        }
//        return distance;
//    }

    //TODO
    public int calculateHPWL(){
        int hpwl = 0;
        int corrFactor = 1;


//        ArrayList<SiteInst> siteInsts = new ArrayList<>(n.getSiteInsts());
        Tile tmp = siteInsts.get(0).getTile();
        int min_X = tmp.getColumn();
        int max_X = tmp.getColumn();
        int min_Y = tmp.getRow();
        int max_Y = tmp.getRow();
        int size = siteInsts.size();
        for (int i = 1; i < size; i++) {
            Tile next = siteInsts.get(i).getTile();
            int tmpX = next.getColumn();
            int tmpY = next.getRow();
            if (tmpX < min_X) {
                min_X = tmpX;
            } else if (tmpX > max_X) {
                max_X = tmpX;
            }
            if (tmpY < min_Y) {
                min_Y = tmpY;
            } else if (tmpY > max_Y) {
                max_Y = tmpY;
            }
        }
        hpwl += (Math.abs(min_X - max_X) + Math.abs(min_Y - max_Y)) * corrFactor;

        distance = hpwl;
        return hpwl;
    }

    public int getDistance() {
        return distance;
    }
}
