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
import org.hyperledger.api.APIBlockIdList;
import org.hyperledger.common.BID;

import java.util.List;

public class BlockIdsRepresentation {
    public static BlockIdsRepresentation create(APIBlockIdList blockIdList) {
        return new BlockIdsRepresentation(blockIdList.idList, blockIdList.height, blockIdList.previousBlockId);
    }

    @JsonProperty
    private final List<BID> blockIds;
    @JsonProperty
    private final int height;
    @JsonProperty
    private final BID previousBlockId;

    public BlockIdsRepresentation(List<BID> blockIds, int height, BID previousBlockId) {
        this.blockIds = blockIds;
        this.height = height;
        this.previousBlockId = previousBlockId;
    }
}
