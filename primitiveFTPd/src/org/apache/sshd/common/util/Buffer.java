/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sshd.common.util;

import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.cipher.ECCurves;
import org.primftpd.pojo.KeyParser;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * TODO Add javadoc
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
@SuppressWarnings("ALL")
public final class Buffer implements Readable {

    public static final int DEFAULT_SIZE = 256;
    public static final int MAX_LEN = 65536;

    private byte[] data;
    private int rpos;
    private int wpos;

    public Buffer() {
        this(DEFAULT_SIZE);
    }

    public Buffer(int size) {
        this(new byte[getNextPowerOf2(size)], false);
    }

    public Buffer(byte[] data) {
        this(data, 0, data.length, true);
    }

    public Buffer(byte[] data, boolean read) {
        this(data, 0, data.length, read);
    }

    public Buffer(byte[] data, int off, int len) {
        this(data, off, len, true);
    }

    public Buffer(byte[] data, int off, int len, boolean read) {
        this.data = data;
        this.rpos = off;
        this.wpos = (read ? len : 0) + off;
    }

    @Override
    public String toString() {
        return "Buffer [rpos=" + rpos + ", wpos=" + wpos + ", size=" + data.length + "]";
    }

    /*======================
      Global methods
    ======================*/

    public int rpos() {
        return rpos;
    }

    public void rpos(int rpos) {
        this.rpos = rpos;
    }

    public int wpos() {
        return wpos;
    }

    public void wpos(int wpos) {
        ensureCapacity(wpos - this.wpos);
        this.wpos = wpos;
    }

    public int available() {
        return wpos - rpos;
    }

    public int capacity() {
        return data.length - wpos;
    }

    public byte[] array() {
        return data;
    }

    public void compact() {
        if (available() > 0) {
            System.arraycopy(data, rpos, data, 0, wpos - rpos);
        }
        wpos -= rpos;
        rpos = 0;
    }

    public byte[] getCompactData() {
        int l = available();
        if (l > 0) {
            byte[] b = new byte[l];
            System.arraycopy(data, rpos, b, 0, l);
            return b;
        } else {
            return new byte[0];
        }
    }

    public void clear() {
        rpos = 0;
        wpos = 0;
    }

    public String printHex() {
        return BufferUtils.printHex(array(), rpos(), available());
    }

    /*======================
       Read methods
     ======================*/

    public byte getByte() {
        ensureAvailable(1);
        return data[rpos++];
    }

    public int getInt() {
        return (int) getUInt();
    }

    public long getUInt()
    {
        ensureAvailable(4);
        long l = ((data[rpos++] << 24) & 0xff000000L)|
                ((data[rpos++] << 16) & 0x00ff0000L)|
                ((data[rpos++] <<  8) & 0x0000ff00L)|
                ((data[rpos++]      ) & 0x000000ffL);
        return l;
    }

    public long getLong()
    {
        ensureAvailable(8);
        long l = (((long) data[rpos++] << 56) & 0xff00000000000000L)|
                (((long) data[rpos++] << 48) & 0x00ff000000000000L)|
                (((long) data[rpos++] << 40) & 0x0000ff0000000000L)|
                (((long) data[rpos++] << 32) & 0x000000ff00000000L)|
                (((long) data[rpos++] << 24) & 0x00000000ff000000L)|
                (((long) data[rpos++] << 16) & 0x0000000000ff0000L)|
                (((long) data[rpos++] <<  8) & 0x000000000000ff00L)|
                (((long) data[rpos++]      ) & 0x00000000000000ffL);
        return l;
    }

    public boolean getBoolean() {
        return getByte() != 0;
    }

    public String getString() {
        int len = getInt();
        if (len < 0) {
            throw new IllegalStateException("Bad item length: " + len);
        }
        ensureAvailable(len);
        String s = new String(data, rpos, len);
        rpos += len;
        return s;
    }

    public byte[] getStringAsBytes() {
        return getBytes();
    }

    public BigInteger getMPInt() {
        return new BigInteger(getMPIntAsBytes());
    }

    public byte[] getMPIntAsBytes() {
        return getBytes();
    }

    public byte[] getBytes() {
        int len = getInt();
        if (len < 0) {
            throw new IllegalStateException("Bad item length: " + len);
        }
        ensureAvailable(len);
        byte[] b = new byte[len];
        getRawBytes(b);
        return b;
    }

    public void getRawBytes(byte[] buf) {
        getRawBytes(buf, 0, buf.length);
    }

    public void getRawBytes(byte[] buf, int off, int len) {
        ensureAvailable(len);
        System.arraycopy(data, rpos, buf, off, len);
        rpos += len;
    }

    public PublicKey getPublicKey() throws SshException {
        int ow = wpos;
        int len = getInt();
        wpos = rpos + len;
        try {
            return getRawPublicKey();
        } finally {
            wpos = ow;
        }
    }

    public PublicKey getRawPublicKey() throws SshException {
        try {
            PublicKey key;
            String keyAlg = getString();
            if (KeyPairProvider.SSH_RSA.equals(keyAlg)) {
                BigInteger e = getMPInt();
                BigInteger n = getMPInt();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory("RSA");
                key = keyFactory.generatePublic(new RSAPublicKeySpec(n, e));
            } else if (KeyPairProvider.SSH_DSS.equals(keyAlg)) {
                BigInteger p = getMPInt();
                BigInteger q = getMPInt();
                BigInteger g = getMPInt();
                BigInteger y = getMPInt();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory("DSA");
                key = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP256.equals(keyAlg)) {
                key = getRawECKey("nistp256", ECCurves.EllipticCurves.nistp256);
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP384.equals(keyAlg)) {
                key = getRawECKey("nistp384", ECCurves.EllipticCurves.nistp384);
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP521.equals(keyAlg)) {
                key = getRawECKey("nistp521", ECCurves.EllipticCurves.nistp521);
            } else {
                if (KeyParser.NAME_ED25519.equals(keyAlg)) {
                    try {
                        byte[] keyBytes = getBytes();
                        key = KeyParser.parsePublicKeyEd25519(keyBytes);
                    } catch (IOException e) {
                        throw new SshException(e);
                    }
                } else {
                    throw new IllegalStateException("Unsupported algorithm: " + keyAlg);
                }
            }
            return key;
        } catch (InvalidKeySpecException e) {
            throw new SshException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new SshException(e);
        } catch (NoSuchProviderException e) {
            throw new SshException(e);
        }
    }

    private PublicKey getRawECKey(String expectedCurve, ECParameterSpec spec) throws InvalidKeySpecException,
            SshException, NoSuchAlgorithmException, NoSuchProviderException {
        String curveName = getString();
        if (!expectedCurve.equals(curveName)) {
            throw new InvalidKeySpecException("Curve name does not match expected: " + curveName + " vs "
                    + expectedCurve);
        }
        ECPoint w = ECCurves.decodeECPoint(getStringAsBytes(), spec.getCurve());
        KeyFactory keyFactory = SecurityUtils.getKeyFactory("EC");
        return keyFactory.generatePublic(new ECPublicKeySpec(w, spec));
    }

    public KeyPair getKeyPair() throws SshException {
        try {
            PublicKey pub;
            PrivateKey prv;
            String keyAlg = getString();
            if (KeyPairProvider.SSH_RSA.equals(keyAlg)) {
                BigInteger e = getMPInt();
                BigInteger n = getMPInt();
                BigInteger d = getMPInt();
                BigInteger qInv = getMPInt();
                BigInteger q = getMPInt();
                BigInteger p = getMPInt();
                BigInteger dP = d.remainder(p.subtract(BigInteger.valueOf(1)));
                BigInteger dQ = d.remainder(q.subtract(BigInteger.valueOf(1)));
                KeyFactory keyFactory = SecurityUtils.getKeyFactory("RSA");
                pub = keyFactory.generatePublic(new RSAPublicKeySpec(n, e));
                prv = keyFactory.generatePrivate(new RSAPrivateCrtKeySpec(n, e, d, p, q, dP, dQ, qInv));
            } else if (KeyPairProvider.SSH_DSS.equals(keyAlg)) {
                BigInteger p = getMPInt();
                BigInteger q = getMPInt();
                BigInteger g = getMPInt();
                BigInteger y = getMPInt();
                BigInteger x = getMPInt();
                KeyFactory keyFactory = SecurityUtils.getKeyFactory("DSA");
                pub = keyFactory.generatePublic(new DSAPublicKeySpec(y, p, q, g));
                prv = keyFactory.generatePrivate(new DSAPrivateKeySpec(x, p, q, g));
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP256.equals(keyAlg)) {
                return extractEC("nistp256", ECCurves.EllipticCurves.nistp256);
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP384.equals(keyAlg)) {
                return extractEC("nistp384", ECCurves.EllipticCurves.nistp384);
            } else if (KeyPairProvider.ECDSA_SHA2_NISTP521.equals(keyAlg)) {
                return extractEC("nistp521", ECCurves.EllipticCurves.nistp521);
            } else {
                throw new IllegalStateException("Unsupported algorithm: " + keyAlg);
            }
            return new KeyPair(pub, prv);
        } catch (InvalidKeySpecException e) {
            throw new SshException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new SshException(e);
        } catch (NoSuchProviderException e) {
            throw new SshException(e);
        }
    }

    private KeyPair extractEC(String expectedCurveName, ECParameterSpec spec) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeySpecException, SshException {
        String curveName = getString();
        byte[] groupBytes = getStringAsBytes();
        BigInteger exponent = getMPInt();

        if (!expectedCurveName.equals(curveName)) {
            throw new SshException("Expected curve " + expectedCurveName + " but was " + curveName);
        }

        ECPoint group = ECCurves.decodeECPoint(groupBytes, spec.getCurve());
        if (group == null) {
            throw new InvalidKeySpecException("Couldn't decode EC group");
        }

        KeyFactory keyFactory = SecurityUtils.getKeyFactory("EC");
        PublicKey pubKey = keyFactory.generatePublic(new ECPublicKeySpec(group, spec));
        PrivateKey privKey = keyFactory.generatePrivate(new ECPrivateKeySpec(exponent, spec));
        return new KeyPair(pubKey, privKey);
    }

    private void ensureAvailable(int a) {
        if (available() < a) {
            throw new BufferException("Underflow");
        }
    }

    /*======================
       Write methods
     ======================*/

    public void putByte(byte b) {
        ensureCapacity(1);
        data[wpos++] = b;
    }

    public void putBuffer(Readable buffer) {
        putBuffer(buffer, true);
    }

    public int putBuffer(Readable buffer, boolean expand) {
        int r = expand ? buffer.available() : Math.min(buffer.available(), capacity());
        ensureCapacity(r);
        buffer.getRawBytes(data, wpos, r);
        wpos += r;
        return r;
    }

    /**
     * Writes 32 bits
     * @param i
     */
    public void putInt(long i) {
        ensureCapacity(4);
        data[wpos++] = (byte) (i >> 24);
        data[wpos++] = (byte) (i >> 16);
        data[wpos++] = (byte) (i >>  8);
        data[wpos++] = (byte) (i      );
    }

    /**
     * Writes 64 bits
     * @param i
     */
    public void putLong(long i) {
        ensureCapacity(8);
        data[wpos++] = (byte) (i >> 56);
        data[wpos++] = (byte) (i >> 48);
        data[wpos++] = (byte) (i >> 40);
        data[wpos++] = (byte) (i >> 32);
        data[wpos++] = (byte) (i >> 24);
        data[wpos++] = (byte) (i >> 16);
        data[wpos++] = (byte) (i >>  8);
        data[wpos++] = (byte) (i      );
    }

    public void putBoolean(boolean b) {
        putByte(b ? (byte) 1 : (byte) 0);
    }

    public void putBytes(byte[] b) {
        putBytes(b, 0, b.length);
    }

    public void putBytes(byte[] b, int off, int len) {
        putInt(len);
        ensureCapacity(len);
        System.arraycopy(b, off, data, wpos, len);
        wpos += len;
    }

    public void putString(String string) {
        putString(string.getBytes());
    }

    public void putString(byte[] str) {
        putInt(str.length);
        putRawBytes(str);
    }

    public void putMPInt(BigInteger bi) {
        putMPInt(bi.toByteArray());
    }

    public void putMPInt(byte[] foo) {
        int i = foo.length;
        if ((foo[0] & 0x80) != 0) {
            i++;
            putInt(i);
            putByte((byte)0);
        } else {
            putInt(i);
        }
        putRawBytes(foo);
    }

    public void putRawBytes(byte[] d) {
        putRawBytes(d, 0, d.length);
    }

    public void putRawBytes(byte[] d, int off, int len) {
        ensureCapacity(len);
        System.arraycopy(d, off, data, wpos, len);
        wpos += len;
    }

    public void putPublicKey(PublicKey key) {
        int ow = wpos;
        putInt(0);
        int ow1 = wpos;
        putRawPublicKey(key);
        int ow2 = wpos;
        wpos = ow;
        putInt(ow2 - ow1);
        wpos = ow2;
    }

    public void putRawPublicKey(PublicKey key) {
        if (key instanceof RSAPublicKey) {
            putString(KeyPairProvider.SSH_RSA);
            putMPInt(((RSAPublicKey) key).getPublicExponent());
            putMPInt(((RSAPublicKey) key).getModulus());
        } else if (key instanceof DSAPublicKey) {
            putString(KeyPairProvider.SSH_DSS);
            putMPInt(((DSAPublicKey) key).getParams().getP());
            putMPInt(((DSAPublicKey) key).getParams().getQ());
            putMPInt(((DSAPublicKey) key).getParams().getG());
            putMPInt(((DSAPublicKey) key).getY());
        } else if (key instanceof ECPublicKey) {
            ECPublicKey ecKey = (ECPublicKey) key;
            ECParameterSpec ecParams = ecKey.getParams();
            String curveName = ECCurves.getCurveName(ecParams);
            putString(ECCurves.ECDSA_SHA2_PREFIX + curveName);
            putString(curveName);
            putBytes(ECCurves.encodeECPoint(ecKey.getW(), ecParams.getCurve()));
        } else {
            throw new IllegalStateException("Unsupported algorithm: " + key.getAlgorithm());
        }
    }

    public void putKeyPair(KeyPair key) {
        if (key.getPrivate() instanceof RSAPrivateCrtKey) {
            putString(KeyPairProvider.SSH_RSA);
            putMPInt(((RSAPublicKey) key.getPublic()).getPublicExponent());
            putMPInt(((RSAPublicKey) key.getPublic()).getModulus());
            putMPInt(((RSAPrivateCrtKey) key.getPrivate()).getPrivateExponent());
            putMPInt(((RSAPrivateCrtKey) key.getPrivate()).getCrtCoefficient());
            putMPInt(((RSAPrivateCrtKey) key.getPrivate()).getPrimeQ());
            putMPInt(((RSAPrivateCrtKey) key.getPrivate()).getPrimeP());
        } else if (key.getPublic() instanceof DSAPublicKey) {
            putString(KeyPairProvider.SSH_DSS);
            putMPInt(((DSAPublicKey) key.getPublic()).getParams().getP());
            putMPInt(((DSAPublicKey) key.getPublic()).getParams().getQ());
            putMPInt(((DSAPublicKey) key.getPublic()).getParams().getG());
            putMPInt(((DSAPublicKey) key.getPublic()).getY());
            putMPInt(((DSAPrivateKey) key.getPrivate()).getX());
        } else if (key.getPublic() instanceof ECPublicKey) {
            ECPublicKey ecPub = (ECPublicKey) key.getPublic();
            ECPrivateKey ecPriv = (ECPrivateKey) key.getPrivate();
            ECParameterSpec ecParams = ecPub.getParams();
            String curveName = ECCurves.getCurveName(ecParams);

            putString(ECCurves.ECDSA_SHA2_PREFIX + curveName);
            putString(curveName);
            putString(ECCurves.encodeECPoint(ecPub.getW(), ecParams.getCurve()));
            putMPInt(ecPriv.getS());
        } else {
            throw new IllegalStateException("Unsupported algorithm: " + key.getPublic().getAlgorithm());
        }
    }

    private void ensureCapacity(int capacity) {
        if (data.length - wpos < capacity) {
            int cw = wpos + capacity;
            byte[] tmp = new byte[getNextPowerOf2(cw)];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
    }

    public static class BufferException extends RuntimeException {
        public BufferException(String message) {
            super(message);
        }
    }

    private static int getNextPowerOf2(int i) {
        int j = 1;
        while (j < i) {
            j <<= 1;
        }
        return j;
    }

}
