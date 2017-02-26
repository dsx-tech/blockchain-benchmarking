/*
 ******************************************************************************
 * Blockchain benchmarking framework                                          *
 * Copyright (C) 2016 DSX Technologies Limited.                               *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 *                                                                            *
 * Removal or modification of this copyright notice is prohibited.            *
 *                                                                            *
 ******************************************************************************
 */

package uk.dsxt.bb.datamodel.bitcoin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BitcoinUnspentTransaction extends BitcoinTransaction {
    private int vout;
    private String scriptPubKey;
    private BigDecimal confirmations;
    private boolean solvable;

    public BitcoinUnspentTransaction(String txid, int vout, String scriptPubKey, BigDecimal confirmations, boolean solvable) {
        super(txid);
        this.vout = vout;
        this.scriptPubKey = scriptPubKey;
        this.confirmations = confirmations;
        this.solvable = solvable;
    }
}
