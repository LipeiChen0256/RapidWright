import com.xilinx.rapidwright.design.*;
import com.xilinx.rapidwright.design.Module;
import com.xilinx.rapidwright.device.*;
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

    List<Site> sites = new ArrayList<>();

    private static double Probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }

    public List<Site> getSites() {
        return sites;
    }

    private List<Site> SA_algorithm(){

        if(sites.isEmpty()) System.out.println("error: no Sites to be placed on.\n");

        Trace current = new Trace(sites);
        Trace best = current.duplicate();

        for (double t = temperature; t > 1; t *= coolingFactor) {
            Trace neighbor = current.duplicate();

            int index1 = (int) (neighbor.noSites() * Math.random());
            int index2 = (int) (neighbor.noSites() * Math.random());

            Collections.swap(neighbor.getSites(), index1, index2);

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
        return best.getSites();
    }

    public static void main(String[] args) {

        MyPlacement mp = new MyPlacement();

        String filename = "1_spi.dcp";
        Design design = Design.readCheckpoint("my_test/dcpfile/input/" + filename);
        // or if the EDIF inside the DCP is encrypted because of source references,
        // you can alternatively supply a separate EDIF
//        Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");

        Device device = design.getDevice();
        Collection<Cell> cells = design.getCells();
        Collection<SiteInst> SiteInsts = design.getSiteInsts();
        Collection<Tile> tiles = device.getAllTiles();

        /*all Sites on this device*/
        HashMap<String, Site> AllSitesOnDevice = new HashMap<>();
        for (Tile t : tiles) {
            Site[] tempArr = t.getSites();
            for (Site s : tempArr) {
                AllSitesOnDevice.put(s.getName(), s);
            }
        }

        EDIFNetlist netlist = design.getNetlist();
        EDIFCell topCell = netlist.getTopCell();
        Collection<Net> nets = design.getNets();
        HashMap<String, ModuleInst> moduleInsts = design.getModuleInstMap();


        /*to find unplaced Cells*/
        HashSet<Cell> unplacedCells = new HashSet<>();
        for (Cell c : cells) {
            if (!c.isPlaced()) unplacedCells.add(c);
        }
        
        /*to find all Sites that can be populated with a specific SiteInst*/
        HashMap<SiteTypeEnum, ArrayList<Site>> sitesOfAllTypeOfSI = new HashMap<>();
        for (SiteInst si : SiteInsts) {
            SiteTypeEnum t = si.getSiteTypeEnum();
            if (!sitesOfAllTypeOfSI.containsKey(t)) {
                sitesOfAllTypeOfSI.put(t, new ArrayList<>());
                Site[] sites = device.getAllCompatibleSites(t);
                for (Site s : sites) {
                    sitesOfAllTypeOfSI.get(t).add(s);
                }
            }
        }

        //TODO:
        /*to find all unused and type matching sites*/
        HashMap<String, Site> unusedSites = new HashMap<>();
        Collection<ArrayList<Site>> allSitesOfSpecificType = sitesOfAllTypeOfSI.values();

        for (ArrayList<Site> list: allSitesOfSpecificType) {
            for(Site s: list){
                SitePIP[] sitePIPs = s.getSitePIPs();
                boolean isUnused = true;
                for (SitePIP sp: sitePIPs){
                    if(!sp.getInputPin().getSiteConns().isEmpty()){
                        isUnused = false;
                        break;
                    }

                }
                //why always null
                if (isUnused) unusedSites.put(s.getName(), s);
            }
        }





        //Collection<SiteInst> sites = design.getSiteInsts();

        // Step 2 - Load block(s)
//        ModuleImpls ilaModuleImpls = BlockCreator.readStoredModule(DEBUG_CORE_PATH + "/" + ilaCoreFileName, null);
//        ModuleImpls dhModuleImpls = BlockCreator.readStoredModule(DEBUG_CORE_PATH + "/" + debugHubCoreFileName, null);


        /* Place cells in empty locations within the user design*/
//      for reference: DesignTools.placeCell(c, design);

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
