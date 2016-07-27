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

package org.hyperledger.api.connector;

import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.hyperledger.api.*;
import org.hyperledger.block.Header;
import org.hyperledger.block.HyperledgerHeader;
import org.hyperledger.merkletree.MerkleRoot;
import org.hyperledger.merkletree.MerkleTree;
import org.hyperledger.transaction.TID;
import org.hyperledger.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protos.Chaincode.ChaincodeInvocationSpec;
import protos.EventsGrpc;
import protos.EventsOuterClass.Event;
import protos.EventsOuterClass.EventType;
import protos.EventsOuterClass.Interest;
import protos.EventsOuterClass.Register;
import protos.Fabric;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class GRPCObserver {
    private static final Logger log = LoggerFactory.getLogger(GRPCObserver.class);

    private EventsGrpc.EventsStub es;
    private Set<TransactionListener> txListeners = new HashSet<>();
    private Set<TrunkListener> trunkListeners = new HashSet<>();
    private Set<RejectListener> rejectionListeners = new HashSet<>();

    public GRPCObserver(Channel eventsChannel) {
        es = EventsGrpc.newStub(eventsChannel);
    }

    public void connect() {
        StreamObserver<Event> receiver = new StreamObserver<Event>() {
            @Override
            public void onNext(Event event) {
                try {
                    switch (event.getEventCase()) {
                        case BLOCK:
                            handleBlockEvent(event);
                            break;
                        case REJECTION:
                            handleRejectionEvent(event);
                            break;
                        default:
                            log.info("Unhandled event {}", event);
                    }
                } catch (HLAPIException e) {
                    log.error("Error handling event {}, {}", event, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in stream: ", t.getMessage());
            }

            @Override
            public void onCompleted() {
                log.info("Stream completed");
            }
        };

        StreamObserver<Event> sender = es.chat(receiver);
        sender.onNext(createRegisterMessage());
    }

    private Event createRegisterMessage() {
        Interest.Builder blockInterest = Interest.newBuilder().setEventType(EventType.BLOCK);
        Interest.Builder rejectionInterest = Interest.newBuilder().setEventType(EventType.BLOCK);

        Register.Builder register = Register.newBuilder()
                .addEvents(blockInterest)
                .addEvents(rejectionInterest);

        return Event.newBuilder().setRegister(register).build();
    }

    private void handleBlockEvent(Event event) throws HLAPIException {
        HLAPIBlock block = createBlock(event.getBlock().getTransactionsList());
        log.info("Handling new block event of {}", block.getID());
        serveTransactionListeners(block.getTransactions());
        serveTrunkListeners(block);
    }

    private HLAPIBlock createBlock(List<Fabric.Transaction> txs) {
        List<Transaction> txList = txs.stream()
                .map(GRPCObserver::toHLTransaction)
                .collect(toList());

        MerkleRoot merkleRoot = MerkleTree.computeMerkleRoot(txList);

        Header header = HyperledgerHeader.create().
                merkleRoot(merkleRoot)
                .build(); // TODO set previous hash and time

        List<HLAPITransaction> hlapiTxs = txList.stream()
                .map(tx -> new HLAPITransaction(tx, header.getID()))
                .collect(toList());

        return new HLAPIBlock.Builder()
                .header(header)
                .transactions(hlapiTxs)
                .build();
    }

    private static Transaction toHLTransaction(Fabric.Transaction tx) {
        ByteString invocationSpecBytes = tx.getPayload();
        try {
            ChaincodeInvocationSpec invocationSpec = ChaincodeInvocationSpec.parseFrom(invocationSpecBytes);
            String transactionString = invocationSpec.getChaincodeSpec().getCtorMsg().getArgs(0);
            byte[] transactionBytes = DatatypeConverter.parseBase64Binary(transactionString);
            return Transaction.fromByteArray(transactionBytes);
        } catch (IOException e) {
            log.error("Error when processing transaction {}, {}", invocationSpecBytes, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void serveTransactionListeners(List<HLAPITransaction> transactionsList) throws HLAPIException {
        for (HLAPITransaction tx : transactionsList) {
            for (TransactionListener listener : txListeners) {
                listener.process(tx);
            }
        }
    }

    private void serveTrunkListeners(HLAPIBlock block) {
        for (TrunkListener listener : trunkListeners) {
            listener.trunkUpdate(Collections.singletonList(block));
        }
    }

    private void handleRejectionEvent(Event event) {
        String reason = event.getRejection().getErrorMsg();
        TID txId = toHLTransaction(event.getRejection().getTx()).getID();
        log.info("Handle rejection of txid={} uuid={} because {}", txId, txId.toUuidString(), reason);
        for (RejectListener listener : rejectionListeners) {
            listener.rejected("invoke", txId, reason, 0);
        }
    }

    public void subscribeToTransactions(TransactionListener l) {
        txListeners.add(l);
    }

    public void unsubscribeFromTransactions(TransactionListener l) {
        txListeners.remove(l);
    }

    public void subscribeToBlocks(TrunkListener l) {
        trunkListeners.add(l);
    }

    public void unsubscribeFromBlocks(TrunkListener l) {
        trunkListeners.remove(l);
    }

    public void subscribeToRejections(RejectListener l) {
        rejectionListeners.add(l);
    }

    public void unsubscribeFromRejections(RejectListener l) {
        rejectionListeners.remove(l);
    }
}
