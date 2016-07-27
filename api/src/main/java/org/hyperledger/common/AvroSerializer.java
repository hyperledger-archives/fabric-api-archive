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

package org.hyperledger.common;

import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class AvroSerializer {

    public static <T extends SpecificRecord> byte[] serialize(T data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        DatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
        writer.write(data, encoder);
        encoder.flush();
        out.close();
        return out.toByteArray();
    }

    public static <T extends SpecificRecord> T deserialize(byte[] data, Schema schema) throws IOException {
        SpecificDatumReader<T> reader = new SpecificDatumReader<>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }

    public static <T> List<ByteBuffer> toByteBufferList(List<T> list, Function<T, byte[]> encoder) {
        return list.stream()
                .map(item -> ByteBuffer.wrap(encoder.apply(item)))
                .collect(toList());
    }

    public static <T> List<T> fromByteBufferList(List<ByteBuffer> list, Function<byte[], T> decoder) {
        return list.stream()
                .map(item -> decoder.apply(item.array()))
                .collect(toList());
    }
}
