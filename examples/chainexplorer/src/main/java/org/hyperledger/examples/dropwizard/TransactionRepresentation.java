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
import org.hyperledger.api.APITransaction;
import org.hyperledger.common.BID;
import org.hyperledger.common.TID;
import org.hyperledger.common.TransactionInput;
import org.hyperledger.common.TransactionOutput;
import org.hyperledger.common.color.ColoredTransactionOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class TransactionRepresentation {
    @JsonProperty
    private final TID id;
    @JsonProperty
    private final BID blockID;
    @JsonProperty
    private final int version;
    @JsonProperty
    private final List<OutputRepresentation> outputs;
    @JsonProperty
    private final List<InputRepresentation> inputs;

    public TransactionRepresentation(TID id, BID blockID, int version, List<OutputRepresentation> outputs, List<InputRepresentation> inputs) {

        this.id = id;
        this.blockID = blockID;
        this.version = version;
        this.outputs = outputs;
        this.inputs = inputs;
    }

    public static TransactionRepresentation create(APITransaction tx, List<String> inputAddresses, List<String> color, List<Long> quantity) {
        List<InputRepresentation> inputRepresentations = new ArrayList<>(inputAddresses.size());
        int i = 0;
        for (TransactionInput input : tx.getInputs()) {
            inputRepresentations.add(InputRepresentation.create(input, inputAddresses.get(i), color.get(i), quantity.get(i)));
            i++;
        }

        return new TransactionRepresentation(
                tx.getID(),
                tx.getBlockID(),
                tx.getVersion(),
                tx.getOutputs().stream().map(OutputRepresentation::create).filter(o -> o.quantity != 0L).collect(Collectors.toList()),
                inputRepresentations
        );
    }

    public static class OutputRepresentation {
        @JsonProperty
        private final long value;
        @JsonProperty
        private final String outputAddress;
        @JsonProperty
        private final String color;
        @JsonProperty
        private final long quantity;

        public OutputRepresentation(long value, String outputAddress, String color, long quantity) {
            this.value = value;
            this.outputAddress = outputAddress;
            this.color = color;
            this.quantity = quantity;
        }

        public static OutputRepresentation create(TransactionOutput o) {
            String color = "";
            long quantity = -1L;
            String outputAddress = o.getOutputAddress() == null ? "" : o.getOutputAddress().toString();
            if (o instanceof ColoredTransactionOutput) {
                ColoredTransactionOutput co = (ColoredTransactionOutput) o;
                color = co.getColor().toString();
                quantity = co.getQuantity();
            }

            return new OutputRepresentation(o.getValue(), outputAddress, color, quantity);
        }
    }

    public static class InputRepresentation {
        @JsonProperty
        private final String sourceTransactionID;
        @JsonProperty
        private final int outputIndex;
        @JsonProperty
        private final String inputAddress;
        @JsonProperty
        private final String color;
        @JsonProperty
        private final long quantity;

        public InputRepresentation(TID sourceTransactionID, int outputIndex, String inputAddress, String color, long quantity) {
            if (TID.INVALID.equals(sourceTransactionID)) {
                this.sourceTransactionID = "";
                this.outputIndex = -1;
            } else {
                this.sourceTransactionID = sourceTransactionID.toString();
                this.outputIndex = outputIndex;
            }
            this.inputAddress = inputAddress;
            this.color = color;
            this.quantity = quantity;
        }

        public static InputRepresentation create(TransactionInput i, String inputAddress, String color, long quantity) {
            return new InputRepresentation(
                    i.getSourceTransactionID(),
                    i.getOutputIndex(),
                    inputAddress, color, quantity);
        }
    }

}
