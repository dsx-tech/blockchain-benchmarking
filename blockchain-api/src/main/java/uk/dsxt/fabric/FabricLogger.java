/*
 * *****************************************************************************
 *  * Blockchain benchmarking framework                                          *
 *  * Copyright (C) 2016 DSX Technologies Limited.                               *
 *  * *
 *  * This program is free software: you can redistribute it and/or modify       *
 *  * it under the terms of the GNU General Public License as published by       *
 *  * the Free Software Foundation, either version 3 of the License, or          *
 *  * (at your option) any later version.                                        *
 *  * *
 *  * This program is distributed in the hope that it will be useful,            *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 *  * See the GNU General Public License for more details.                       *
 *  * *
 *  * You should have received a copy of the GNU General Public License          *
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *  * *
 *  * Removal or modification of this copyright notice is prohibited.            *
 *  * *
 *  *****************************************************************************
 */

package uk.dsxt.fabric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.dsxt.datamodel.fabric.FabricBlock;
import uk.dsxt.datamodel.fabric.FabricChain;
import uk.dsxt.datamodel.fabric.FabricPeer;
import uk.dsxt.datamodel.fabric.FabricTimestamp;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FabricLogger {

    private long counterForHeight = 0;

    private static final Logger log = LogManager.getLogger(FabricLogger.class.getName());

    private FabricTimestamp getTimestampDiff(FabricBlock fb1, FabricBlock fb2) {
        FabricTimestamp timestamp1 = fb1.getNonHashData().getLocalLedgerCommitTimestamp();
        FabricTimestamp timestamp2 = fb2.getNonHashData().getLocalLedgerCommitTimestamp();

        FabricTimestamp diffTimestamp = new FabricTimestamp();

        diffTimestamp.setNanos(Math.abs(timestamp2.getNanos() - timestamp1.getNanos()));
        diffTimestamp.setSeconds(Math.abs(timestamp2.getSeconds() - timestamp1.getSeconds()));

        return diffTimestamp;
    }

    public void log(FabricManager fabricManager, String mainPeer) throws IOException {
        FabricChain fabricChain = fabricManager.getChain(mainPeer);
        List<FabricTimestamp> timestamps = new LinkedList<>();

        if (fabricChain.getHeight() - 1 > counterForHeight) {

            FabricPeer[] fabricPeers = fabricManager.getPeers(mainPeer);
            counterForHeight++;
            long finalCounterForHeight = counterForHeight;

            Arrays.stream(fabricPeers).forEach(peer -> {
                try {
                    String peerStr = peer.getAddress();
                    peerStr = peerStr.replaceFirst(".$", "0");
                    String peerURL = String.join("", "http://", peerStr, "/");
                    FabricBlock fabricBlock = fabricManager.getBlock(peerURL, finalCounterForHeight);

                    if (fabricBlock != null) {
                        timestamps.add(getTimestampDiff(fabricBlock,
                                fabricManager.getBlock(mainPeer, finalCounterForHeight)));
                    } else {
                        log.info(String.format("block %d not added to %s yet", finalCounterForHeight, peer.getAddress()));
                    }

                } catch (IOException e) {
                    log.error("Cannot get block", e);
                }
            });
            log.info(String.join(" ", "Chain block:", Long.toString(finalCounterForHeight), timestamps.stream()
                    .map(timestamp ->  Long.toString(timestamp.getNanos())).collect(Collectors.joining("/"))));
        }
    }
}
