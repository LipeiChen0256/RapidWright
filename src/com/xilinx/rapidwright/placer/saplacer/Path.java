package com.xilinx.rapidwright.placer.saplacer;

import com.xilinx.rapidwright.device.Site;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Path {
    //***********************used to compute the distance
    //*********************** to record every step of changes
    private List<Site> sites;
    private static int distance;

    public Path(List<Site> sites){
        this.sites=sites;
        Collections.shuffle(this.sites);
    }

    public Site getSite(int index){
        return sites.get(index);
    }

    /**
     * compute the ManhattanDistance
     */
    //TODO: to compute manhattan distance
    public int computeDistance(){
        if(distance!=0) return distance;

        int totalDistance = 0;
        for (int i=0;i<SitesNum();i++){
            Site start=getSite(i);
            Site end = getSite(i+1<SitesNum()? i+1:0);
            totalDistance+=start.getTile().getManhattanDistance(end.getTile());
        }
        distance=totalDistance;
        return totalDistance;
    }

    public Path duplicate(){
        return new Path(new ArrayList<>(sites));
    }

    public int SitesNum(){
        return sites.size();
    }
}
