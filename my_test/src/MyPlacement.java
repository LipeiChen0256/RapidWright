package src;

import com.xilinx.rapidwright.design.*;
import com.xilinx.rapidwright.design.Module;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.device.Site;
import com.xilinx.rapidwright.device.Tile;
import com.xilinx.rapidwright.edif.EDIFCell;
import com.xilinx.rapidwright.edif.EDIFNetlist;
import com.xilinx.rapidwright.interchange.DeviceResources;
import com.xilinx.rapidwright.ipi.BlockCreator;
import com.xilinx.rapidwright.placer.blockplacer.BlockPlacer;
import com.xilinx.rapidwright.router.Router;
import com.xilinx.rapidwright.tests.CodePerfTracker;
import com.xilinx.rapidwright.util.FileTools;
import com.xilinx.rapidwright.util.StringTools;

import java.util.*;

public class MyPlacement {

    private static double temperature = 1000;
    private static double coolingFactor = 0.995;

    List<Site> sites = new ArrayList<Site>();

    private static double Probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }

    public List<Site> getSites() {
        return sites;
    }

    private void SA_algorithm(){

        if(sites.isEmpty()) System.out.println("error: no Sites to be placed on.\n");

        Trace current = new Trace(sites);
        Trace best = current.duplicate();

        for (double t = temperature; t > 1; t *= coolingFactor) {
            Trace neighbor = current.duplicate();

            int index1 = (int) (neighbor.noSites() * Math.random());
            int index2 = (int) (neighbor.noSites() * Math.random());

            Collections.swap(this.getSites(), index1, index2);

            int currentLength = current.getTraceLength();
            int neighborLength = neighbor.getTraceLength();

            if (Math.random() < Probability(currentLength, neighborLength, t)) {
                current = neighbor.duplicate();
            }

            if (current.getTraceLength() < best.getTraceLength()) {
                best = current.duplicate();
            }
        }

        System.out.println("Final trace length:" + best.getTraceLength());
        System.out.println("Trace:" + best);
    }

    public static void main(String[] args) {

        String filename = "1_spi.dcp";
        Design design = Design.readCheckpoint("my_test/dcpfile/input/" + filename);
        // or if the EDIF inside the DCP is encrypted because of source references,
        // you can alternatively supply a separate EDIF
//        Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");

        Device device = design.getDevice();
        Collection<Cell> cells = design.getCells();
        Collection<SiteInst> SiteInsts = design.getSiteInsts();
        Collection<Tile> tiles = device.getAllTiles();
        HashSet<Site> unusedSites = new HashSet<>();
//        for (Tile t : tiles) {
//            Site[] tempArr = t.getSites();
//            for (Site s : tempArr){
//                if() unusedSites.add(s);
//            }
//        }

        EDIFNetlist netlist = design.getNetlist();
        EDIFCell topCell = netlist.getTopCell();
        Collection<Net> nets = design.getNets();
        HashMap<String, ModuleInst> moduleInsts = design.getModuleInstMap();


        /*to find unplaced Cells*/
        HashSet<Cell> unplacedCells = new HashSet<>();
        for (Cell c : cells) {
            if (!c.isPlaced()) unplacedCells.add(c);
        }
        
        /*to find unplaced Sites*/
        HashSet<SiteInst> unplacedSiteInsts = new HashSet<>();
        for (SiteInst si : SiteInsts) {
            if(!si.isPlaced()) unplacedSiteInsts.add(si);
        }

//        HashSet<Site> unplacedSites = new HashSet<>();
//        for (Site si : Sites) {
//            if(!si.isPlaced()) unplacedSites.add(si);
//        }


        /*to find all unoccupied positions*/
        //HashSet<Cell> unoccupiedPositions = design.;

        //Collection<SiteInst> sites = design.getSiteInsts();

        // Step 2 - Load block(s)
//        ModuleImpls ilaModuleImpls = BlockCreator.readStoredModule(DEBUG_CORE_PATH + "/" + ilaCoreFileName, null);
//        ModuleImpls dhModuleImpls = BlockCreator.readStoredModule(DEBUG_CORE_PATH + "/" + debugHubCoreFileName, null);


        /* Place cells in empty locations within the user design*/
//        for (Cell c: unplacedCells) {
//            DesignTools.placeCell(c, design);
//        }

//        BlockPlacer placer = new BlockPlacer();
//        placer.placeDesign(design,true);
//        int unplacedInsts = 0;
//        for(SiteInst si : design.getSiteInsts()){
//            if(!si.isPlaced()){
//                unplacedInsts++;
//            }
//        }
//        if(unplacedInsts > 0){
//            throw new RuntimeException("ERROR: " + unplacedInsts + "Unplaced instances!");
//        }

        // Step 5 - Route updated nets

//        Router router = new Router(design);
//        router.routeStaticNets();
//        router.routePinsReEntrant(pinsToRoute, true);

        // To write out a design
        design.writeCheckpoint("my_test/dcpfile/output/" + filename);
//        design.writeCheckpoint("my_test/dcpfile/output/1_spi.dcp");
    }
}
