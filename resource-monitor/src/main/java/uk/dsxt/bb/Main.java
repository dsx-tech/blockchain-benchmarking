package uk.dsxt.bb;

import lombok.extern.log4j.Log4j2;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.StringJoiner;

/**
 * @author phd
 */
@Log4j2
public class Main {
    public static void main(String[] args) throws Exception {

        System.setProperty("java.library.path", System.getProperty("java.library.path")+File.pathSeparator+"lib");

        //set sys_paths to null
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);

        System.out.println(System.getProperty("java.library.path"));

        FileWriter fw = new FileWriter("resource_usage.csv", true);
        StringJoiner stringJoiner = new StringJoiner(",");
        stringJoiner.add("time");
        stringJoiner.add("cpu");
        stringJoiner.add("used mem");
        stringJoiner.add("used mem%");
        stringJoiner.add("downloaded");
        stringJoiner.add("uploaded");
        fw.write(stringJoiner.toString() + '\n');
        fw.flush();

        Sigar sigar = new Sigar();
        new NetworkData(sigar);

        NumberFormat formatter = new DecimalFormat("0.00", new MyDecimalFormatSymbols());


        while (true) {
            long start = System.currentTimeMillis();
            Mem mem = sigar.getMem();
            CpuPerc cpuPerc = sigar.getCpuPerc();
            System.out.print(mem.getUsed() + "\t");
            System.out.print(mem.getUsedPercent() + "\t");
            System.out.print(formatter.format(cpuPerc.getCombined() * 100) + "\t");

            Long[] metric = NetworkData.getMetric();


            long downloaded = metric[0];
            long uploaded = metric[1];
            System.out.println("in-bound " + Sigar.formatSize(downloaded)
                    + " out-bound " + Sigar.formatSize(uploaded));
            System.out.println();

            stringJoiner = new StringJoiner(",");
            stringJoiner.add(Long.toString(start));
            stringJoiner
                    .add(formatter.format(cpuPerc.getCombined() * 100))
                    .add(mem.getUsed() + "")
                    .add(mem.getUsedPercent() + "")
                    .add((downloaded) + "")
                    .add((uploaded) + "");

            fw.write(stringJoiner.toString() + "\n");
            fw.flush();

            long end = System.currentTimeMillis();
            Thread.sleep(Math.max(1000 - (end - start), 0));
        }
    }
}

class MyDecimalFormatSymbols extends DecimalFormatSymbols {
    @Override
    public char getDecimalSeparator() {
        return '.';
    }
}
