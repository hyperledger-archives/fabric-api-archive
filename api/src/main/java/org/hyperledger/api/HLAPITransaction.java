/**
 * Copyright 2016 Digital Asset Holdings, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hyperledger.api;

import org.hyperledger.block.BID;
import org.hyperledger.transaction.Transaction;

public class HLAPITransaction extends Transaction {

    private final BID blockID;

    public HLAPITransaction(Transaction transaction, BID blockID) {
        super(transaction);
        this.blockID = blockID;
    }


    /**
     * get hash of the block this transaction is embedded into. Note that this is not part of the protocol, but is filled by the server while retrieving a
     * transaction in context of a block A transaction alone might not have this filled.
     */
    public BID getBlockID() {
        return blockID;
    }
}
