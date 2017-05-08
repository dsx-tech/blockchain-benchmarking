package uk.dsxt.bb;

import org.hyperic.sigar.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NetworkData {

    static Map<String, Long> rxCurrentMap = new HashMap<>();
    static Map<String, List<Long>> rxChangeMap = new HashMap<>();
    static Map<String, Long> txCurrentMap = new HashMap<>();
    static Map<String, List<Long>> txChangeMap = new HashMap<>();
    private static Sigar sigar;

    /**
     * @throws InterruptedException
     * @throws SigarException
     *
     */
    public NetworkData(Sigar s) throws SigarException, InterruptedException {
        sigar = s;
        getMetric();
        System.out.println(networkInfo());
        Thread.sleep(1000);
    }

//    public static void main(String[] args) throws SigarException,
//            InterruptedException {
//        new NetworkData(new Sigar());
//        NetworkData.newMetrickThread();
//    }

    public static String networkInfo() throws SigarException {
        String info = sigar.getNetInfo().toString();
        info += "\n"+ sigar.getNetInterfaceConfig().toString();
        return info;
    }

    public static String getDefaultGateway() throws SigarException {
        return sigar.getNetInfo().getDefaultGateway();
    }

    public static void newMetricThread() throws SigarException, InterruptedException {
        while (true) {
            Long[] m = getMetric();
            long totalrx = m[0];
            long totaltx = m[1];
            System.out.print("totalrx(download): ");
            System.out.println("\t" + Sigar.formatSize(totalrx));
            System.out.print("totaltx(upload): ");
            System.out.println("\t" + Sigar.formatSize(totaltx));
            System.out.println("-----------------------------------");
            Thread.sleep(1000);
        }

    }

    public static Long[] getMetric() throws SigarException {
        for (String ni : sigar.getNetInterfaceList()) {
            // System.out.println(ni);
            NetInterfaceStat netStat = sigar.getNetInterfaceStat(ni);
            NetInterfaceConfig ifConfig = sigar.getNetInterfaceConfig(ni);
            String hwaddr = null;
            if (!NetFlags.NULL_HWADDR.equals(ifConfig.getHwaddr())) {
                hwaddr = ifConfig.getHwaddr();
            }
            if (hwaddr != null) {
                long rxCurrenttmp = netStat.getRxBytes();
                saveChange(rxCurrentMap, rxChangeMap, hwaddr, rxCurrenttmp, ni);
                long txCurrenttmp = netStat.getTxBytes();
                saveChange(txCurrentMap, txChangeMap, hwaddr, txCurrenttmp, ni);
            }
        }
        long totalrx = getMetricData(rxChangeMap);
        long totaltx = getMetricData(txChangeMap);
        for (List<Long> l : rxChangeMap.values())
            l.clear();
        for (List<Long> l : txChangeMap.values())
            l.clear();
        return new Long[] { totalrx, totaltx };
    }

    private static long getMetricData(Map<String, List<Long>> rxChangeMap) {
        long total = 0;
        for (Entry<String, List<Long>> entry : rxChangeMap.entrySet()) {
            int average = 0;
            for (Long l : entry.getValue()) {
                average += l;
            }
            total += average / entry.getValue().size();
        }
        return total;
    }

    private static void saveChange(Map<String, Long> currentMap,
                                   Map<String, List<Long>> changeMap, String hwaddr, long current,
                                   String ni) {
        Long oldCurrent = currentMap.get(ni);
        if (oldCurrent != null) {
            List<Long> list = changeMap.get(hwaddr);
            if (list == null) {
                list = new LinkedList<Long>();
                changeMap.put(hwaddr, list);
            }
            list.add((current - oldCurrent));
        }
        currentMap.put(ni, current);
    }

}