/******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2017 DSX Technologies Limited.                               *
 * *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 * *
 * Removal or modification of this copyright notice is prohibited.            *
 * *
 ******************************************************************************/
package model;

public class TransactionInfo {

    private long time;
    private long transactionId;
    private int transactionSize;
    private int nodeId;
    private int responseCode;
    private String responseMessage;
    private int blockId;

    public TransactionInfo(long time, long transactionId, int transactionSize,
                           int nodeId, int responseCode, String responseMessage) {
        this.time = time;
        this.transactionId = transactionId;
        this.transactionSize = transactionSize;
        this.nodeId = nodeId;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public long getTime() {
        return time;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public int getTransactionSize() {
        return transactionSize;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
