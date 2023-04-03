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

    config.getKeys().toArray();
    Object[] num = config.getKeys().toArray();
    String id = "";
    for (int i = 0; i < num.length; i++) {

      if (((String) num[i]).startsWith("auth.") && ((String) num[i]).endsWith(".initiatorId")) {
        System.out.println(num[i] + "*");
        id = ((String) num[i]).substring(5, ((String) num[i]).length() - 12);
        System.out.println("This is the ID");
        System.out.println(id);
        System.out.println("This is the number");
        System.out.println(
            ((String) num[i]).substring(5, ((String) num[i]).length() - 12) + "           **");
      }
    }

    System.out.println(id + "   ***");

    String peerUsername = (String) config.get(("auth." + id + ".peerUsername"));
    System.out.println(id + "             ****");
    System.out.println(peerUsername + "*****");
    String hashAlgorithm = config.getString("auth." + id + ".hashAlgorithm", "SHA-1");
    System.out.println(hashAlgorithm);
    byte[] peerPass = ByteBufUtil.decodeHexDump(config.getString("auth." + id + ".peerPassword"));
    System.out.println(peerPass + "4444455555544441111111111");
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
