package com.windhoverlabs.yamcs.sle.simulator;

import static org.yamcs.jsle.udpslebridge.Util.getProperty;

import io.netty.buffer.ByteBufUtil;
import java.util.Optional;
import java.util.Properties;
import org.yamcs.jsle.Isp1Authentication;
import org.yamcs.jsle.provider.AuthProvider;
import org.yamcs.jsle.udpslebridge.Util;

public class SimAuthProvider implements AuthProvider {
  //	This auth scheme is only for SIMULATION purposes!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
  Properties properties;
  String myUsername;
  byte[] myPass;

  public SimAuthProvider(Properties properties) {
    this.properties = properties;
    this.myUsername = Util.getProperty(properties, "sle.myUsername");
    this.myPass = ByteBufUtil.decodeHexDump(Util.getProperty(properties, "sle.myPassword"));
  }

  @Override
  public Isp1Authentication getAuth(String initiatorId) {
    // look for an entry where auth.x.initiatorId = initiatorId and return x
    Optional<String> x =
        properties.entrySet().stream()
            .filter(
                e ->
                    initiatorId.equals(e.getValue())
                        && ((String) e.getKey()).startsWith("auth.")
                        && ((String) e.getKey()).endsWith(".initiatorId"))
            .map(
                e -> {
                  String k = (String) e.getKey();
                  return k.substring(5, k.length() - 12);
                })
            .findFirst();

    if (!x.isPresent()) {
      return null;
    }
    String id = x.get();
    String peerUsername = properties.getProperty("auth." + id + ".peerUsername", initiatorId);
    byte[] peerPass =
        ByteBufUtil.decodeHexDump(getProperty(properties, "auth." + id + ".peerPassword"));
    String hashAlgorithm = properties.getProperty("auth." + id + ".hashAlgorithm", "SHA-1");
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
