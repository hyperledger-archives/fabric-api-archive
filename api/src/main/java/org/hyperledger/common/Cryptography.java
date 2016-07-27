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

public interface Cryptography {

    byte[] createNewPrivateKey();

    byte[] getPublicFor(byte[] privateKey);

    byte[] getPrivateKeyAtOffset(byte[] privateKey, byte[] offset);

    byte[] uncompressPoint(byte[] compressed);

    byte[] getPublicKeyAtOffset(byte[] publicKey, byte[] offset);

    byte[] sign(byte[] hash, byte[] privateKey);

    boolean verify(byte[] hash, byte[] signature, byte[] publicKey);

}
