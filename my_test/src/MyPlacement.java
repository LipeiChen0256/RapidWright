import com.xilinx.rapidwright.design.*;
import com.xilinx.rapidwright.device.*;
import com.xilinx.rapidwright.edif.*;
import com.xilinx.rapidwright.util.Utils;
import java.util.*;

public class MyPlacement {

    Device device;
    Design design;
    private double temperature = 1000;
    private double coolingFactor = 0.995;
    ArrayList<SiteInst> siteInstsToBePlaced;

    //the range of InnerNum is [1, 10]
    private int innerNum = 1;
    private double rangeLimit;
    private Random rand = new Random();
    boolean calInitialTemp = false;
    HashMap<SiteInst, Site> bestConfiguration;

    SiteInst selectedSiteInst;
    Site selectedSite;
    public MyPlacement(Design design) {
        this.device = design.getDevice();
        this.design = design;
        SiteInst[] siteInsts = design.getSiteInsts().stream()
                .filter(siteInst -> Utils.isModuleSiteType(siteInst.getSiteTypeEnum()) && siteInst.getSiteTypeEnum()!=SiteTypeEnum.BUFGCE)
                .toArray(SiteInst[]::new);
        this.siteInstsToBePlaced = new ArrayList<>(Arrays.asList(siteInsts));
    }

    private static double Probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }

    private void SAPlacer(){
        //ratio of accepted changes
        double alpha = 0.0;
        //number of elements to be configured
        double N = siteInstsToBePlaced.size();
        boolean exit = false;
        temperature = InitalTemperature();

        System.out.println("start annealing ...");
        rangeLimit = Math.max(device.getColumns(), device.getRows());
        designTracker current= new designTracker(siteInstsToBePlaced, design.getNets());
        double currLength = 0.0;
        double prevLength = current.calSysCost();
        double bestLength = prevLength;
//        designTracker best = current.duplicate();
        int movesPerTemp = (int) (innerNum * Math.pow(N, 1.333));
        while(!exit){
            int numAcceptedChange = 0;
            int numChange = 0;
            int numRefusedChange = 0;
            int badAcceptedMoveCount = 0;
            double totalMovesCost = 0.0;
            for(int i=0; i<movesPerTemp; i++){

                if(applyRandChange(current)) {
                    numChange++;
                    currLength = current.calSysCost();
                    if (Math.random() < Probability(prevLength, currLength, temperature)) {
                        numAcceptedChange++;

                        prevLength = currLength;
                    } else {
                        numRefusedChange++;
                        undoChange();
                    }
                    if (currLength < bestLength) {
                        bestLength = currLength;
//                        bestConfiguration = current.generateConfig();
                    }
                }
            }//inner loop

            if(numChange > 0) {
                alpha = ((double) numAcceptedChange) / numChange;
            }else{
                alpha = 0;
            }

            rangeLimit = rangeLimit * (1.0 + alpha - 0.44);
            double upperLimit = Math.max(device.getColumns(), device.getRows());
            rangeLimit = Math.min(rangeLimit, upperLimit);
            rangeLimit = Math.max(rangeLimit, 1.0);

            temperature = updateTemperature(temperature, alpha);
            System.out.printf("temperature: %f, range limit: %f, best system cost: %f%n", temperature,rangeLimit,bestLength);
            //TODO: the exit condition of the outer loop
            double exitCriterion = 0.005 * (prevLength/current.getNets().size());
//            double exitCriterion = (prevLength - currLength)/prevLength;
            if(temperature < exitCriterion)
                exit = true;
        }// outer loop
        System.out.println("Annealing completed!");
//        design.clearUsedSites();
//        for(Map.Entry<SiteInst, Site> e : bestConfiguration.entrySet()){
//            e.getKey().place(e.getValue());
//        }
        siteInstVerticalStack();
        System.out.println("SAPlacer completed.");
    }


    private void undoChange() {
        selectedSiteInst.place(selectedSite);
    }

    private double updateTemperature(double temperature, double alpha) {
        double gamma;
        if(alpha >= 0.96) gamma = 0.5;
        else if(alpha >= 0.8 && alpha < 0.96) gamma = 0.9;
        else if(alpha >= 0.15 && alpha < 0.8) gamma = 0.95;//0.99 better but more time
        else gamma = 0.8;
        return  gamma * temperature;
    }

    private double InitalTemperature() {
        calInitialTemp = true;
        int numSiteInst = siteInstsToBePlaced.size();
        double stdDev = 0.0;
        double avgCost = 0.0;
        double curCost = 0.0;
        double tmp = 0.0;
        int numAcceptedChange = 0;
        designTracker current = new designTracker(siteInstsToBePlaced, design.getNets());
        ArrayList<Double> arrayCosts = new ArrayList<>();
        for(int i=0; i<numSiteInst; i++){
            //TODO: is the random Change the same as generateNew

            if(applyRandChange(current)) {
                curCost = current.calSysCost();
                numAcceptedChange++;
                arrayCosts.add(curCost);
                avgCost = avgCost + curCost;
            }
        }
        System.out.println("N random Changes compeleted.");
        avgCost = avgCost / numAcceptedChange;
        for(double c : arrayCosts){
            tmp = c - avgCost;
            stdDev = stdDev + (tmp*tmp);
        }
        stdDev = Math.sqrt(stdDev / (numAcceptedChange-1));//sample standard deviation
        System.out.println("initial temperature is " + 20*stdDev);
        calInitialTemp = false;
        return 20 * stdDev;
    }

    //TODO
    public boolean applyRandChange(designTracker current) {

        int size = current.getSiteInstsPlaced().size();
        selectedSiteInst = current.getSiteInstsPlaced().get(rand.nextInt(size));
        selectedSite = selectedSiteInst.getSite();
//        System.out.println(selectedSite.getName()+" is selected." + selectedSiteInst.getName() + " is moved.");
        ArrayList<Site> randSiteRange = new ArrayList<>();
        int min_X;
        int min_Y;
        int max_Y;
        int max_X;
        if(calInitialTemp) {
            min_Y = 1;
            max_Y = device.getRows();
            min_X = 1;
            max_X = device.getColumns();
        }else{
            min_Y = Math.max(1, selectedSite.getTile().getRow() - (int) rangeLimit);
            max_Y = Math.min(device.getRows(), selectedSite.getTile().getRow() + (int) rangeLimit);
            min_X = Math.max(1, selectedSite.getTile().getColumn() - (int) rangeLimit);
            max_X = Math.min(device.getColumns(), selectedSite.getTile().getColumn() + (int) rangeLimit);
        }
        for(Site s : getValidSites(selectedSite.getSiteTypeEnum())){
            if (s.getTile().getColumn()>=min_X && s.getTile().getColumn()<=max_X &&
                    s.getTile().getRow()>=min_Y && s.getTile().getRow()<=max_Y){
                randSiteRange.add(s);
            }
        }

        if(randSiteRange.size()>0){
            Site randSite = randSiteRange.get(rand.nextInt(randSiteRange.size()));
            selectedSiteInst.place(randSite);
            //        if(!design.isSiteUsed(selectedSite)) System.out.println(selectedSite.getName()+" is not used now. Moved to "+randSite.getName());
            return true;
        }else{
            return false;
        }
    }

    //TODO
//    private double curSystemCost(Collection<SiteInst> siteInsts){
//        double totalWireLength = 0;
//        double tmp = 0.0;
//        designTracker dt = new designTracker(siteInsts);
//        totalWireLength = dt.calSysCost();
//        return totalWireLength;
//    }

    private void randomPlacement() {
        //to find all SiteInsts to be placed
        HashMap<SiteTypeEnum, ArrayList<SiteInst>> siteInstsToBePlacedMap = new HashMap<>();
        for (SiteInst si: siteInstsToBePlaced){
            SiteTypeEnum type = si.getSiteTypeEnum();
            if(!siteInstsToBePlacedMap.containsKey(type)) {
                ArrayList<SiteInst> tmpArr = new ArrayList<>();
                tmpArr.add(si);
                siteInstsToBePlacedMap.put(type, tmpArr);
            }else{
                siteInstsToBePlacedMap.get(type).add(si);
            }
        }

        //get the middle of th plane
        Tile center = device.getTile(device.getRows()/2, device.getColumns()/2);
        PriorityQueue<Site> sites = new PriorityQueue<>(1024, new Comparator<Site>() {
            public int compare(Site i, Site j) {
                return i.getTile().getManhattanDistance(center) - j.getTile().getManhattanDistance(center);
            }
        });

        for(Map.Entry<SiteTypeEnum, ArrayList<SiteInst>> entry : siteInstsToBePlacedMap.entrySet()){
            SiteTypeEnum t = entry.getKey();
            ArrayList<SiteInst> tmpArr = entry.getValue();
            sites.clear();
            sites.addAll(getValidSites(t));
            while(!tmpArr.isEmpty()){
                Site site = sites.remove();
                SiteInst siteInst = tmpArr.remove(0);
                siteInst.place(site);
            }
        }
    }

    private ArrayList<Site> getValidSites(SiteTypeEnum type) {
        //find all unused Sites that can be populated with needed Type

        ArrayList<Site> siteList = new ArrayList<>();
        Site[] sites = MyPlacement.getAllCompatibleSitesFixed(device, type);
        for (Site s : sites) {
            if (!design.isSiteUsed(s)) {
                siteList.add(s);
            }
        }
        return siteList;
    }

    private static final Map<Device, Map<SiteTypeEnum, Site[]>> compatibleSitesCache = new HashMap<>();
    public static Site[] getAllCompatibleSitesFixed(Device dev, SiteTypeEnum ste) {
        return compatibleSitesCache.computeIfAbsent(dev, x -> new HashMap<>())
                .computeIfAbsent(ste, s -> Arrays.stream(dev.getAllCompatibleSites(s))
                        .filter(site -> {
                            SiteTypeEnum[] compat = site.getAlternateSiteTypeEnums();
                            return site.getSiteTypeEnum()==s || (compat != null && Arrays.asList(compat).contains(s));
                        })
                        .toArray(Site[]::new)
                );
    }

    private void siteInstVerticalStack(){
        //find out carry chain
        Net[] carrNets = design.getNets().stream()
                .filter(net -> net.getLogicalNet()!=null)
                .filter(net -> {
                    ArrayList<EDIFPortInst> epis = new ArrayList<>(net.getLogicalNet().getPortInsts());
                    String name1 = epis.get(0).getName();
                    String name2 = epis.get(1).getName();
                    return epis.size() == 2 && (name1.contains("CI") && name2.contains("CO") || name2.contains("CI") && name1.contains("CO"));
                })
                .sorted(Comparator.comparing(Net::getName))
                .toArray(Net[]::new);

        //sort these carry Chains
        //find out all related SiteInst
        ArrayList<Set<SiteInst>> siteInstList = new ArrayList<>();
        for(int i=0; i< carrNets.length; i++) {
            Net net = carrNets[i];
            Set<SiteInst> tmpSet = new HashSet<>(net.getSiteInsts());
            siteInstList.add(tmpSet);
        }
        
        boolean isSorted = false;
        while(!isSorted) {
            isSorted = true;
            for (int i = 0; i < siteInstList.size(); i++) {
                Set<SiteInst> set1 = siteInstList.get(i);
                for (int j = 0; j < siteInstList.size(); j++) {
                    if (i == j) continue;
                    Set<SiteInst> set2 = siteInstList.get(j);
                    if (!Collections.disjoint(set1, set2)) {
                        isSorted = false;
                        Set<SiteInst> set3 = getUnion(set1,set2);
                        siteInstList.add(set3);
                        siteInstList.remove(set1);
                        siteInstList.remove(set2);
                    }
                }
            }
        }

        //place
        Set<SiteInst> modifiedSet = new HashSet<>();
        for(Set set : siteInstList){
            ArrayList<SiteInst> siList = new ArrayList<>(set);
            for (int i=1; i<siList.size(); i++){
                Site site = siList.get(i-1).getSite();
                Site site1 = siList.get(i).getSite(); //siteInst that has carryChain
//                int X = site.getInstanceX();
//                int Y = site.getInstanceY();
                int dy = 0;
                int count = 0;
                Site site2;
                do {
                    dy++;
                    site2 = site.getNeighborSite(0, dy);//siteInst that above the site
                }while(!site2.isCompatibleSiteType(site1));
                SiteInst siteInst1 = siList.get(i);
                SiteInst siteInst2 = design.getSiteInstFromSite(site2);
                if(siteInst2==null){
                    siteInst1.place(site2);
                    modifiedSet.add(siteInst1);
                }else{
                    siteInst1.place(site2);
                    modifiedSet.add(siteInst1);
                    siteInst2.place(site1);
                }
            }
            modifiedSet.add(siList.get(0));
        }

        //get the site to be used for swaping
//        public Site getCorrespondingSite(SiteTypeEnum type, Tile newSiteTile);
    }

    private boolean isModified(Site site2, Set<SiteInst> modifiedSet) {
        return modifiedSet.contains(design.getSiteInstFromSite(site2));
    }

    // get union
    private Set<SiteInst> getUnion(Set<SiteInst> set1, Set<SiteInst> set2) {
        Set<SiteInst> result = new HashSet<>();
        result.addAll(set1);
        result.removeAll(set2);
        result.addAll(set2);
        return result;
    }

    public static void main(String[] args) {


        Design design = Design.readCheckpoint("my_test/rapidwright_benchmarks_unrouted_v2/benchmarks/yosys/cordic/cordic_10_12_placed.dcp");
        // or if the EDIF inside the DCP is encrypted because of source references,
        // you can alternatively supply a separate EDIF
        //Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");
        /* fill all data structures by itself from the EDIF netlist*/

        DesignTools.createMissingSitePinInsts(design);
        design.unrouteDesign();
        design.getSiteInsts().stream()
                .filter(siteInst -> Utils.isModuleSiteType(siteInst.getSiteTypeEnum()) && siteInst.getSiteTypeEnum()!=SiteTypeEnum.BUFGCE)
                .forEach(SiteInst::unPlace);
//        design.getSiteInsts().stream()
//                .filter(siteInst -> Utils.isModuleSiteType(siteInst.getSiteTypeEnum()))
//                .forEach(SiteInst::unPlace);


        MyPlacement mp = new MyPlacement(design);
        //to find all Sites on this device
        HashMap<String, Site> allSitesOnDevice = new HashMap<>();
        for (Tile t : mp.device.getAllTiles()) {
            Site[] tempArr = t.getSites();
            for (Site s : tempArr) {
                allSitesOnDevice.put(s.getName(), s);
            }
        }

        Collection<Net> nets = mp.design.getNets();

        System.out.println("==============================================================================");
        System.out.println("==                 conducting initial random placement                      ==");
        System.out.println("==============================================================================");
        long startTime=System.currentTimeMillis();
        //make a random placement of the design
        mp.randomPlacement();
        long endTime=System.currentTimeMillis();
        System.out.println("\t*Total*\t"+(endTime-startTime)/1000.0 + "s");
        System.out.println("------------------------------------------------------------------------------");


        System.out.println("==============================================================================");
        System.out.println("==                           conducting SA Placer                           ==");
        System.out.println("==============================================================================");
        long startTime1 = System.currentTimeMillis();
        //make a SA placement of the design
        mp.SAPlacer();
        long endTime1 = System.currentTimeMillis();
        System.out.println("\t*Total*\t"+(endTime1-startTime1)/1000.0 + "s");
        System.out.println("------------------------------------------------------------------------------");

        // To write out a design
        mp.design.writeCheckpoint("my_test/rapidwright_benchmarks_unrouted_v2/benchmarks/yosys/cordic/out_cordic_10_12_placed.dcp");
//        design.writeCheckpoint("my_test/dcpfile/output/1_spi.dcp");
    }


}
