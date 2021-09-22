import com.xilinx.rapidwright.design.*;
import com.xilinx.rapidwright.design.Module;
import com.xilinx.rapidwright.device.*;
import com.xilinx.rapidwright.edif.*;
import com.xilinx.rapidwright.interchange.DeviceResources;
import com.xilinx.rapidwright.interchange.SiteSitePIP;
import com.xilinx.rapidwright.ipi.BlockCreator;
import com.xilinx.rapidwright.placer.blockplacer.BlockPlacer;
import com.xilinx.rapidwright.placer.blockplacer.BlockPlacer2;
import com.xilinx.rapidwright.placer.blockplacer.Path;
import com.xilinx.rapidwright.placer.blockplacer.PathPort;
import com.xilinx.rapidwright.router.Router;
import com.xilinx.rapidwright.tests.CodePerfTracker;
import com.xilinx.rapidwright.util.FileTools;
import com.xilinx.rapidwright.util.StringTools;
import com.xilinx.rapidwright.util.Utils;
import org.python.modules._hashlib;

import java.io.FileNotFoundException;
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

        Design design = Design.readCheckpoint("my_test/rapidwright_benchmarks_unrouted_v2/benchmarks/spmc/mjpeg_5cores/configuration_placed.dcp");
        // or if the EDIF inside the DCP is encrypted because of source references,
        // you can alternatively supply a separate EDIF
        //Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");
        /* fill all data structures by itself from the EDIF netlist*/
        DesignTools.createMissingSitePinInsts(design);
        design.unrouteDesign();
        design.getSiteInsts().stream()
                .filter(siteInst -> Utils.isModuleSiteType(siteInst.getSiteTypeEnum()))
                .forEach(SiteInst::unPlace);

        Device device = design.getDevice();
        //to find all Sites on this device
        HashMap<String, Site> allSitesOnDevice = new HashMap<>();
        for (Tile t : device.getAllTiles()) {
            Site[] tempArr = t.getSites();
            for (Site s : tempArr) {
                allSitesOnDevice.put(s.getName(), s);
            }
        }

        Collection<Net> nets = design.getNets();

        /*****************to find all SiteInsts to be placed*******************/
        Collection<SiteInst> siteInstsToBePlaced = design.getSiteInsts();

        /******************************************************************/

        /**********find all unused Sites that can be populated with needed Type**********/
        HashMap<SiteTypeEnum, ArrayList<Site>> unusedSitesMapOfNeededType = new HashMap<>();

        for (SiteInst siteInst : siteInstsToBePlaced) {
            SiteTypeEnum t = siteInst.getSiteTypeEnum();
            if (!unusedSitesMapOfNeededType.containsKey(t)) {
                ArrayList<Site> siteList = new ArrayList<>();
                unusedSitesMapOfNeededType.put(t, siteList);
                Site[] sites = device.getAllCompatibleSites(t);
                for (Site s : sites) {
                    if(!design.isSiteUsed(s)) {
                        unusedSitesMapOfNeededType.get(t).add(s);
                    }
                }
            }
        }

        /**************************************************************************/

        /*******************make a random placement of the design******************/
        for (SiteInst si: siteInstsToBePlaced){
            SiteTypeEnum type = si.getSiteTypeEnum();
            Site site;
            do{
                site = unusedSitesMapOfNeededType.get(type).remove(0);
            }while(design.isSiteUsed(site));
            si.place(site);
        }
        /**************************************************************************/


        Collection<Net> nets2 = design.getNets();

        HashMap<Net, HashMap<SiteInst, Map<String, SitePinInst>>> siteInstsOfNet = new HashMap<>();
        for (Net n: nets){
            Set<SiteInst> tempSet = n.getSiteInsts();
            HashMap<SiteInst, Map<String, SitePinInst>> tempMap = new HashMap<>();
            for (SiteInst si: tempSet) {
                Map<String,SitePinInst> tempMap2 = si.getSitePinInstMap();
                tempMap.put(si, tempMap2);
            }
            siteInstsOfNet.put(n, tempMap);
        }

        Collection<ModuleInst> moduleInsts = design.getModuleInsts();
        Collection<ModuleImpls> modules = design.getModules();
        HashMap<String, ModuleInst> moduleInstsMap = design.getModuleInstMap();



//        /*to find unplaced Cells*/
//        HashSet<Site> unplacedSites = new HashSet<>();
//        for (Map.Entry<String, Site> entry : allSitesOnDevice.entrySet()) {
//            if (!entry.getValue().is) unplacedSites.add(entry.getValue());
//        }


        //TODO:
        /*to find all unused and type matching sites*/
//        HashMap<String, Site> unusedSites = new HashMap<>();
//        Collection<ArrayList<Site>> tempCol = unusedSitesMapOfNeededType.values();
//        HashMap<Site, SitePIP[]> Sites = new HashMap<>();
//        for (ArrayList<Site> arrayList: tempCol) {
//            for(Site s: arrayList){
//                SitePIP[] sitePIPs = s.getSitePIPs();
//                boolean isUnused = true;
//                Sites.put(s, sitePIPs);
//                for (SitePIP sp: sitePIPs){
//                    if(!sp.getInputPin().getSiteConns().isEmpty()){
//                        isUnused = false;
//                        break;
//                    }
//                }
//                //why always null
//                if (isUnused) unusedSites.put(s.getName(), s);
//            }
//        }





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
        design.writeCheckpoint("my_test/rapidwright_benchmarks_unrouted_v2/benchmarks/spmc/mjpeg_5cores/out_configuration_placed.dcp");
//        design.writeCheckpoint("my_test/dcpfile/output/1_spi.dcp");
    }
}
