import com.xilinx.rapidwright.design.Design;

public class MyPlacement {

    public void SA_algorithm(){

    }

    public static void main(String[] args) {

        String filename = "1_spi.dcp";
        Design design = Design.readCheckpoint("my_test/dcpfile/input/" + filename);

        // or if the EDIF inside the DCP is encrypted because of source references,
        //   you can alternatively supply a separate EDIF
        //Design design = Design.readCheckpoint("my_test/dcpfile/input/1_spi.dcp", "my_test/dcpfile/input/1_spi.edf");



        // To write out a design
        design.writeCheckpoint("my_test/dcpfile/output/" + filename);
    }
}
