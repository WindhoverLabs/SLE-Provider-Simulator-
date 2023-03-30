package com.windhoverlabs.yamcs.sle.simulator;

import io.netty.buffer.ByteBufUtil;
import java.util.Map;
import java.util.Optional;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.Isp1Authentication;
import org.yamcs.jsle.provider.AuthProvider;

public class SimAuthProvider implements AuthProvider {
  String myUsername;
  byte[] myPass;
  YConfiguration config;

  public SimAuthProvider(YConfiguration config) {
    this.config = config;
    this.myUsername = (String) config.get("sle_myUsername");
    this.myPass = ByteBufUtil.decodeHexDump((CharSequence) config.get(("sle_myPassword")));
  }

  @Override
  public Isp1Authentication getAuth(String initiatorId) {
    // look for an entry where auth.x.initiatorId = initiatorId and return x
    Optional<String> x =
        ((Map<Object, Object>) config)
            .entrySet().stream()
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
    String peerUsername = (String) config.get(("auth_" + id + "_peerUsername" + initiatorId));
    byte[] peerPass =
        ByteBufUtil.decodeHexDump((CharSequence) config.getString("auth_" + id + "_peerPassword"));
    String hashAlgorithm = config.getString("auth_" + id + "_hashAlgorithm", "SHA-1");
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
