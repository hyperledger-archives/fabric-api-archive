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

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class BouncyCastleCrypto implements Cryptography {

    static final X9ECParameters curve = SECNamedCurves.getByName("secp256k1");
    static final ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
    static final SecureRandom secureRandom = new SecureRandom();
    static final BigInteger HALF_CURVE_ORDER = curve.getN().shiftRight(1);

    @Override
    public byte[] createNewPrivateKey() {
        ECKeyPairGenerator generator = new ECKeyPairGenerator();
        ECKeyGenerationParameters keygenParams = new ECKeyGenerationParameters(domain, secureRandom);
        generator.init(keygenParams);
        AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        return privParams.getD().toByteArray();
    }

    @Override
    public byte[] getPublicFor(byte[] privateKey) {
        return curve.getG().multiply(new BigInteger(privateKey)).getEncoded(true);
    }

    @Override
    public byte[] getPrivateKeyAtOffset(byte[] privateKey, byte[] offset) {
        return new BigInteger(offset).add(new BigInteger(privateKey)).mod(curve.getN()).toByteArray();
    }

    @Override
    public byte[] uncompressPoint(byte[] compressed) {
        return curve.getCurve().decodePoint(compressed).getEncoded(false);
    }

    @Override
    public byte[] getPublicKeyAtOffset(byte[] publicKey, byte[] offset) {
        BigInteger offsetInt = new BigInteger(publicKey);
        boolean invert = false;

        if (offsetInt.compareTo(BigInteger.ZERO) < 0) {
            invert = true;
            offsetInt = offsetInt.abs();
        }

        ECPoint oG = curve.getG().multiply(offsetInt);

        if (invert) {
            oG = oG.negate();
        }

        return oG.add(curve.getCurve().decodePoint(publicKey)).getEncoded(true);
    }

    @Override
    public byte[] sign(byte[] hash, byte[] privateKey) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, new ECPrivateKeyParameters(new BigInteger(privateKey), domain));
        BigInteger[] signature = signer.generateSignature(hash);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DERSequenceGenerator seq = new DERSequenceGenerator(baos);
            seq.addObject(new ASN1Integer(signature[0]));
            seq.addObject(new ASN1Integer(toCanonicalS(signature[1])));
            seq.close();
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private BigInteger toCanonicalS(BigInteger s) {
        if (s.compareTo(HALF_CURVE_ORDER) <= 0) {
            return s;
        } else {
            return curve.getN().subtract(s);
        }
    }

    @Override
    public boolean verify(byte[] hash, byte[] signature, byte[] publicKey) {
        ASN1InputStream asn1 = new ASN1InputStream(signature);
        try {
            ECDSASigner signer = new ECDSASigner();
            signer.init(false, new ECPublicKeyParameters(curve.getCurve().decodePoint(publicKey), domain));

            DLSequence seq = (DLSequence) asn1.readObject();
            BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getPositiveValue();
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getPositiveValue();
            return signer.verifySignature(hash, r, s);
        } catch (Exception e) {
            return false;
        } finally {
            try {
                asn1.close();
            } catch (IOException ignored) {
            }
        }
    }

}
