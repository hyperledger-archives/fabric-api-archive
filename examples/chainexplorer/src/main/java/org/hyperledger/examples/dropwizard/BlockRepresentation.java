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
package org.hyperledger.examples.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.api.APIBlock;
import org.hyperledger.common.BID;
import org.hyperledger.common.Hash;
import org.hyperledger.common.TID;
import org.hyperledger.common.Transaction;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class BlockRepresentation {
    public static BlockRepresentation create(APIBlock block) {
        return new BlockRepresentation(
                block.getID(),
                block.getPreviousID(),
                block.getHeight(),
                block.getLocalCreateTime(),
                block.getNonce(),
                block.getVersion(),
                block.getMerkleRoot(),
                block.getDifficultyTarget(),
                block.getTransactions().stream().map(Transaction::getID).collect(Collectors.toList())
        );
    }

    @JsonProperty
    private final BID id;
    @JsonProperty
    private final BID previousID;
    @JsonProperty
    private final LocalTime localCreateTime;
    @JsonProperty
    private final int nonce;
    @JsonProperty
    private final int height;
    @JsonProperty
    private final int version;
    @JsonProperty
    private final Hash merkleRoot;
    @JsonProperty
    private final int difficultyTarget;
    @JsonProperty
    private final List<TID> transactions;

    public BlockRepresentation(BID id,
                               BID previousID,
                               int height,
                               LocalTime localCreateTime,
                               int nonce,
                               int version,
                               Hash merkleRoot,
                               int difficultyTarget,
                               List<TID> transactions) {
        this.id = id;
        this.previousID = previousID;
        this.height = height;
        this.localCreateTime = localCreateTime;
        this.nonce = nonce;
        this.version = version;
        this.merkleRoot = merkleRoot;
        this.difficultyTarget = difficultyTarget;
        this.transactions = transactions;
    }
}
