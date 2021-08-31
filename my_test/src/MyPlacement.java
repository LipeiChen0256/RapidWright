import com.xilinx.rapidwright.design.Cell;
import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.SiteInst;
import com.xilinx.rapidwright.device.Device;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class MyPlacement {

    public void SA_algorithm(){

    }

    public static void main(String[] args) {

        String filename = "1_spi.dcp";
        Design design = Design.readCheckpoint("my_test/dcpfile/input/" + filename);
        // or if the EDIF inside the DCP is encrypted because of source references,
        //   you can alternatively supply a separate EDIF
        //Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");

        Device device = design.getDevice();
        Collection<Cell> cells = design.getCells();

        /*to find unplaced Cells*/
        HashSet<Cell> unplacedCells = new HashSet<>();
        for (Cell c : cells) {
            if (!c.isPlaced()) unplacedCells.add(c);
        }

        /*to find all unoccupied positions*/
        //HashSet<Cell> unoccupiedPositions = design.;

        //Collection<SiteInst> sites = design.getSiteInsts();

        //BlockPlacer2 bp2 = new BlockPlacer2();
        //bp2.placeDesign(design,);



        // To write out a design
        design.writeCheckpoint("my_test/dcpfile/output/" + filename);
    }
}
