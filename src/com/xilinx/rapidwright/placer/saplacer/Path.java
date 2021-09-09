package com.xilinx.rapidwright.placer.saplacer;

import com.xilinx.rapidwright.device.Site;

import java.util.List;

public class Path {
    //***********************used to compute the distance
    //*********************** to record every step of changes
    private List<Site> sites;
    private static int distance;

    public Path(List<Site> sites){
        this.sites=sites;
    }

    /**
     * compute the ManhattanDistance
     */
    //TODO: to compute manhattan distance
    public int computeDistance(){
        int totalDistance=0;
        for(Site s: this.sites){}
    }

}
