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

import org.hyperledger.api.APITransaction;
import org.hyperledger.api.BCSAPI;
import org.hyperledger.api.BCSAPIException;
import org.hyperledger.common.BID;
import org.hyperledger.common.HyperLedgerException;
import org.hyperledger.common.TID;
import org.hyperledger.common.TransactionOutput;
import org.hyperledger.common.color.ColoredTransactionOutput;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/explorer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExplorerResource {

    private final BCSAPI api;

    public ExplorerResource(BCSAPI api) {
        this.api = api;
    }

    @GET
    @Path("block/{blockId}")
    public BlockRepresentation latestBlocks(@PathParam("blockId") BID blockId) throws BCSAPIException {
        return BlockRepresentation.create(api.getBlock(blockId));
    }

    @GET
    @Path("tx/{txId}")
    public TransactionRepresentation getTx(@PathParam("txId") TID txId) throws BCSAPIException, HyperLedgerException {
        APITransaction tx = api.getTransaction(txId);

        List<APITransaction> inputTxs = api.getInputTransactions(txId);
        List<String> inputAddresses = new ArrayList<>(inputTxs == null ? 0 : inputTxs.size());
        List<String> color = new ArrayList<>(inputTxs == null ? 0 : inputTxs.size());
        List<Long> quantity = new ArrayList<>(inputTxs == null ? 0 : inputTxs.size());
        if (inputTxs != null) {
            int i = 0;
            for (APITransaction inputTx : inputTxs) {
                if (inputTx == null) {
                    inputAddresses.add("");
                    color.add("");
                    quantity.add(-1L);
                } else {
                    TransactionOutput output = inputTx.getOutput(tx.getSource(i).getOutputIndex());
                    if (output.getOutputAddress() != null) {
                        inputAddresses.add(output.getOutputAddress().toString());
                        if (output instanceof ColoredTransactionOutput) {
                            color.add(((ColoredTransactionOutput) output).getColor().toString());
                            quantity.add(((ColoredTransactionOutput) output).getQuantity());
                        } else {
                            color.add("");
                            quantity.add(-1L);
                        }
                    } else {
                        inputAddresses.add("");
                        color.add("");
                        quantity.add(-1L);
                    }
                }
                i++;
            }
        }
        return TransactionRepresentation.create(tx, inputAddresses, color, quantity);
    }

    @GET
    @Path("chain/height")
    public ChainHeightRepresentation getChainHeight() throws BCSAPIException {
        return ChainHeightRepresentation.create(api.getChainHeight());
    }

    @GET
    @Path("chain/blockids")
    public BlockIdsRepresentation getBlockIds(@DefaultValue("top") @QueryParam("blockId") String blockId, @DefaultValue("20") @QueryParam("count") int count) throws BCSAPIException {
        BID hash;
        if ("top".equals(blockId)) {
            hash = null;
        } else {
            try {
                hash = new BID(blockId);
            } catch (IllegalArgumentException e) {
                throw new BCSAPIException(e.getMessage());
            }
        }
        return BlockIdsRepresentation.create(api.getBlockIds(hash, count));
    }


}
