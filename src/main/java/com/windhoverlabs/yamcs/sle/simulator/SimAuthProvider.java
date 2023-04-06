/****************************************************************************
 *
 *   Copyright (c) 2017 Windhover Labs, L.L.C. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 3. Neither the name Windhover Labs nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *****************************************************************************/
package com.windhoverlabs.yamcs.sle.simulator;

import io.netty.buffer.ByteBufUtil;
import java.util.Arrays;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.Isp1Authentication;
import org.yamcs.jsle.provider.AuthProvider;

public class SimAuthProvider implements AuthProvider {
  String myUsername;
  byte[] myPass;
  YConfiguration config;

  public SimAuthProvider(YConfiguration config) {
    this.config = config;
    this.myUsername = (String) config.get("sle.myUsername");
    this.myPass = ByteBufUtil.decodeHexDump((CharSequence) config.get(("sle.myPassword")));
  }

  @Override
  public Isp1Authentication getAuth(String initiatorId) {

    // gets the value of id from the yamcs.fsw.yaml file
    config.getKeys().toArray();
    Object[] num = config.getKeys().toArray();
    String id = "";
    for (int i = 0; i < num.length; i++) {

      if (((String) num[i]).startsWith("auth.") && ((String) num[i]).endsWith(".initiatorId")) {
        id = ((String) num[i]).substring(5, ((String) num[i]).length() - 12);
      }
    }
    String peerUsername = (String) config.get(("auth." + id + ".peerUsername"));
    String hashAlgorithm = config.getString("auth." + id + ".hashAlgorithm", "SHA-1");
    byte[] peerPass = ByteBufUtil.decodeHexDump(config.getString("auth." + id + ".peerPassword"));
    System.out.println(Arrays.toString(peerPass));
    System.out.println(peerPass + "****");
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
