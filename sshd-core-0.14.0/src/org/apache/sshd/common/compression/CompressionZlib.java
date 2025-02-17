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
package org.apache.sshd.common.compression;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.sshd.common.Compression;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.util.Buffer;

/**
 * ZLib based Compression.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class CompressionZlib implements Compression {

    /**
     * Named factory for the ZLib Compression.
     */
    public static class Factory implements NamedFactory<Compression> {
        public String getName() {
            return "zlib";
        }

        public Compression create() {
            return new CompressionZlib();
        }
    }

    static private final int BUF_SIZE = 4096;

    private final byte[] tmpbuf = new byte[BUF_SIZE];
    private Deflater compresser;
    private Inflater decompresser;

    /**
     * Create a new instance of a ZLib base compression
     */
    public CompressionZlib() {
    }

    public boolean isDelayed() {
        return false;
    }

    public void init(Type type, int level) {
        compresser = new Deflater(level);
        decompresser = new Inflater();
    }

    public void compress(Buffer buffer) throws IOException {
        compresser.setInput(buffer.array(), buffer.rpos(), buffer.available());
        buffer.wpos(buffer.rpos());
        int len;
        while ((len = compresser.deflate(tmpbuf, 0, tmpbuf.length, Deflater.SYNC_FLUSH)) > 0) {
            buffer.putRawBytes(tmpbuf, 0, len);
        }
    }

    public void uncompress(Buffer from, Buffer to) throws IOException {
        decompresser.setInput(from.array(), from.rpos(), from.available());
        int len;
        try {
            while ((len = decompresser.inflate(tmpbuf)) > 0) {
                to.putRawBytes(tmpbuf, 0, len);
            }
        } catch (DataFormatException e) {
            throw new IOException("Error decompressing data", e);
        }
    }

}
