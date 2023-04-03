package com.windhoverlabs.yamcs.sle.simulator;

import io.netty.buffer.ByteBufUtil;
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
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
