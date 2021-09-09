package com.xilinx.rapidwright.placer.saplacer;


import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.Net;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.device.PIP;
import com.xilinx.rapidwright.device.Site;
import com.xilinx.rapidwright.device.Tile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
    public static void getSites(HashSet<Tile> tiles){
        for(Tile t:tiles)
            sites= Arrays.asList(t.getSites());
        int t=1;//test
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
        HashSet<Tile> tiles = new HashSet<Tile>();
        getTileSize(tiles);
        getSites(tiles);
        //-----------------------get Sites and do initialization



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
        SA_algorithm();
        WriteDCP();     //write the output
    }
}