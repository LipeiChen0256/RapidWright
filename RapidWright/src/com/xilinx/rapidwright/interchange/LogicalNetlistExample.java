package com.xilinx.rapidwright.interchange;

import java.io.IOException;

import com.xilinx.rapidwright.edif.EDIFNetlist;
import com.xilinx.rapidwright.edif.EDIFTools;
import com.xilinx.rapidwright.tests.CodePerfTracker;


/**
 * Example reader/writer for Cap'n Proto serialization for a logical netlist.
 *
 */
public class LogicalNetlistExample {

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("USAGE: <input>.edf");
            System.out.println("   Example round trip test for a logical netlist to start from EDIF,"
                    + " get converted to a\n   Cap'n Proto serialized file and then read back into "
                    + "an EDIF file.  Creates two new files:\n\t1. <input>.netlist "
                    + "- Cap'n Proto serialized file"
                    + "\n\t2. <input>.roundtrip.edf - EDIF after being written/read from serialized format");
            return;
        }
        CodePerfTracker t = new CodePerfTracker("Interchange EDIF->Cap'n Proto->EDIF");
        
        t.start("Read EDIF");
        // Read EDIF into memory using RapidWright
        EDIFNetlist n = EDIFTools.readEdifFile(args[0]);

        t.stop().start("Write Cap'n Proto");
        // Write Netlist to Cap'n Proto Serialization file
        String capnProtoFileName = args[0].replace(".edf", ".netlist");
        n.collapseMacroUnisims(n.getDevice().getSeries());
        LogNetlistWriter.writeLogNetlist(n, capnProtoFileName);
        
        t.stop().start("Read Cap'n Proto");
        // Read Netlist into RapidWright netlist
        EDIFNetlist n2 = LogNetlistReader.readLogNetlist(capnProtoFileName);
        
        t.stop().start("Write EDIF");
        // Write RapidWright netlist back to edif
        n2.exportEDIF(args[0].replace(".edf", ".roundtrip.edf"));
        
        t.stop().printSummary();
    }
}
