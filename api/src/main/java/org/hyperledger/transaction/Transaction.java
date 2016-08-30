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
package org.hyperledger.transaction;

import com.google.protobuf.ByteString;
import org.hyperledger.common.AvroSerializer;
import org.hyperledger.common.Hash;
import org.hyperledger.common.PublicKey;
import org.hyperledger.merkletree.MerkleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protos.Chaincode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;

public class Transaction implements MerkleTreeNode {
    public static final String chaincodeName = "noop";
    public static final String txCreatorChaincodeFunction = "execute";
    private static final Logger log = LoggerFactory.getLogger(Transaction.class);

    private final TID ID;
    private final List<TID> inputs;
    private final List<byte[]> outputs;
    private final List<Endorser> endorsers;

    public Transaction(List<TID> inputs, List<byte[]> outputs, List<Endorser> endorsers) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.endorsers = endorsers;

        this.ID = new TID(Hash.of(fabricInvocationForm()));
    }

    private byte[] fabricInvocationForm() {
        Chaincode.ChaincodeInput.Builder chaincodeInput = Chaincode.ChaincodeInput.newBuilder();
        chaincodeInput.addArgs(ByteString.copyFromUtf8(txCreatorChaincodeFunction));
        chaincodeInput.addArgs(ByteString.copyFrom(toByteArray()));
        return chaincodeInput.build().toByteArray();
    }

    protected Transaction(Transaction t) {
        inputs = t.inputs;
        outputs = t.outputs;
        endorsers = t.endorsers;
        ID = t.ID;
    }

    /**
     * @return 0 since Transaction is always the leaf of the Merkle Tree
     */
    @Override
    public int getMerkleHeight() {
        return 0;
    }


    public TID getID() {
        return ID;
    }

    public List<TID> getInputs() {
        return inputs;
    }

    public List<byte[]> getOutputs() {
        return outputs;
    }

    public List<Endorser> getEndorsers() {
        return endorsers;
    }

    /**
     * Verifies if the endorser signed the transaction with the private pair
     * of the provided public key.
     */
    public boolean verify(Endorser endorser, PublicKey key) {
        byte[] hash = Hash.of(outputs.get(0)).toByteArray();
        return endorser.verify(hash, key);
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transaction) {
            Transaction other = (Transaction) obj;
            return ID.equals(other.ID);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "TID=" + ID.toString();
    }

    public byte[] toByteArray() {
        try {
            return toByteArray(inputs, outputs, endorsers);
        } catch (IOException e) {
            log.error("Failed to serialize transaction {}: {}", ID, e.getMessage());
            return new byte[0];
        }
    }

    public static byte[] toByteArray(List<TID> inputs, List<byte[]> outputs, List<Endorser> endorsers) throws IOException {
        List<ByteBuffer> inputBytes = AvroSerializer.toByteBufferList(inputs, TID::toByteArray);
        List<ByteBuffer> outputBytes = AvroSerializer.toByteBufferList(outputs, Function.identity());
        List<ByteBuffer> endorserBytes = AvroSerializer.toByteBufferList(endorsers, Endorser::getSignature);

        SerializedTransaction t = SerializedTransaction.newBuilder()
                .setInputs(inputBytes)
                .setOutputs(outputBytes)
                .setEndorsers(endorserBytes)
                .build();

        return AvroSerializer.serialize(t);
    }

    public static Transaction fromByteArray(byte[] array) throws IOException {
        SerializedTransaction t = AvroSerializer.deserialize(array, SerializedTransaction.getClassSchema());

        List<TID> inputs = AvroSerializer.fromByteBufferList(t.getInputs(), TID::new);
        List<byte[]> outputs = AvroSerializer.fromByteBufferList(t.getOutputs(), Function.identity());
        List<Endorser> endorsers = AvroSerializer.fromByteBufferList(t.getEndorsers(), Endorser::new);

        return new TransactionBuilder()
                .inputs(inputs)
                .outputs(outputs)
                .endorsers(endorsers)
                .build();
    }

}
