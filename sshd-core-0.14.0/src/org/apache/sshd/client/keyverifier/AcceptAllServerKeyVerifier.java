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
package org.apache.sshd.client.keyverifier;

import java.net.SocketAddress;
import java.security.PublicKey;

import org.apache.sshd.ClientSession;
import org.apache.sshd.client.ServerKeyVerifier;
import org.apache.sshd.common.util.KeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ServerKeyVerifier that accepts all server keys.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA SSHD Project</a>
 */
public class AcceptAllServerKeyVerifier implements ServerKeyVerifier {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final ServerKeyVerifier INSTANCE = new AcceptAllServerKeyVerifier();

	private AcceptAllServerKeyVerifier() {
	}

	public boolean verifyServerKey(ClientSession sshClientSession, SocketAddress remoteAddress, PublicKey serverKey) {
        log.warn("Server at {} presented unverified {} key: {}",
                remoteAddress, serverKey.getAlgorithm(), KeyUtils.getFingerPrint(serverKey));
		return true;
	}

}
