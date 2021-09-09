package com.xilinx.rapidwright.placer.saplacer;


import com.xilinx.rapidwright.design.Cell;
import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.Net;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.device.PIP;
import com.xilinx.rapidwright.device.Site;
import com.xilinx.rapidwright.device.Tile;

import java.util.*;

public class SAPlacer {
    private static Design design;
    private static String filename;
    private static Device device;
    private static List<Site> sites;
  //  private static HashSet<Tile> tiles;
    private static int tileSize;

    private static double alpha;
    private static double gamma;
    private static double Temperature;
    private static double StartTemp = 1e3;

    public SAPlacer(){

    }

    /**
     * load file
     */
    private static void InitPlacer(){
        filename = "2_spmc.dcp";

        design=Design.readCheckpoint("D:\\TUD\\task2\\RapidWright\\src\\com\\xilinx\\rapidwright\\placer\\saplacer\\input\\dcp\\"+filename);
        device = design.getDevice();
    }
    public static void SetTileSize(int tileSize){
        SAPlacer.tileSize =tileSize;
    }

    /**
     * get tiles from the design
     */
    public static void getTileSize(HashSet<Tile> tiles){

        for(SiteInst inst: design.getSiteInsts()){
            tiles.add(inst.getTile());
        }
        for(Net net: design.getNets()){
            for(PIP p: net.getPIPs()){
                tiles.add(p.getTile());
            }
        }
        SetTileSize(tiles.size());
    }
    /**
     * to get sites from tiles
     */
    public List<Site> getSites() {
        return sites;
    }
    /**
     * compute the probabilities
     * exp(-delta C/T)
     * delta C : S new - S
     */
    private static double probability(double Snew, double S, double Temperature){
        if(Snew>S) return 1;
        else return Math.exp((S-Snew)/Temperature);
    }

    /**
     * SA algorithm
     */
    private static void SA_algorithm(){
        alpha=0.44;
        gamma=1.0;
        Temperature=1e3;
        if(sites.isEmpty()) System.out.println("Error: no sites to be placed on");
        Path current = new Path(sites);
        Path best = current.duplicate();
        //-----------------------get Sites and do initialization

        for (double t=Temperature; t > 1; t *= gamma){
            Path neighbour = current.duplicate();

            int index1=(int)(neighbour.SitesNum()*Math.random());
            int index2 = (int)(neighbour.SitesNum()*Math.random());
            Collections.swap(sites, index1, index2);

            int currentlength =current.computeDistance();
            int neighbourlength =neighbour.computeDistance();

            if(Math.random() < probability(currentlength, neighbourlength, t))
                current=neighbour.duplicate();

            if(current.computeDistance()<best.computeDistance())
                best=current.duplicate();
        }

        System.out.println("Final Path length : "+best.computeDistance());
        System.out.println("Path : "+ best);


    }


    /**
     * write the .dcp file
     */
    public static void WriteDCP(){
        design.writeCheckpoint("D:\\TUD\\task2\\RapidWright\\src\\com\\xilinx\\rapidwright\\placer\\saplacer\\output\\"+filename);
    }
    public static void main(String[] arg){
        InitPlacer();   //Initialization, read from file
        //getTileSize();  //
        Collection<Cell> cells = design.getCells();
        Collection<SiteInst> SiteInsts = design.getSiteInsts();
        Collection<Tile> tiles = device.getAllTiles();
        HashSet<Site> unusedSites = new HashSet<>();



        SA_algorithm();
        WriteDCP();     //write the output
    }
}