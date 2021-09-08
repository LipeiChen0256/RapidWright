package com.xilinx.rapidwright.placer.saplacer;


import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.Net;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.device.PIP;
import com.xilinx.rapidwright.device.Tile;

import java.util.Collection;
import java.util.HashSet;

public class SAPlacer {
    private static Design design;
    private static String filename;
    private static Device device;

    private static int tileSize;


    public SAPlacer(){
        double alpha = 0.44;
        double gamma = 1.0;
        double initTemperature = 1e3;
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
     *
     */
    public static void getTileSize(){
        HashSet<Tile> tileSet=new HashSet<Tile>();
        for(SiteInst inst: design.getSiteInsts()){
            tileSet.add(inst.getTile());
        }
        for(Net net: design.getNets()){
            for(PIP p: net.getPIPs()){
                tileSet.add(p.getTile());
            }
        }
        SetTileSize(tileSet.size());
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
     * write the .dcp file
     */
    public static void WriteDCP(){
        design.writeCheckpoint("D:\\TUD\\task2\\RapidWright\\src\\com\\xilinx\\rapidwright\\placer\\saplacer\\output\\"+filename);
    }
    public static void main(String[] arg){
        InitPlacer();   //Initialization, read from file
        getTileSize();  //

        WriteDCP();     //write the output
    }
}