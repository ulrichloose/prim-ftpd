/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.ftpserver.listener.nio;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.textline.TextLineDecoder;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Factory for creating decoders and encoders
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class FtpServerProtocolCodecFactory implements ProtocolCodecFactory {
    private final ProtocolDecoder decoder = new TextLineDecoder(StandardCharsets.UTF_8);

    private final ProtocolEncoder encoder = new FtpResponseEncoder();

    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }
}
