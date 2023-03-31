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

    //
    //	 Optional<String> x = (config.getKeys())
    //    			  .entrySet().stream()
    //          .filter(e-> initiatorId.equals(e.getValue())
    //                  && ((String)e.getKey()).startsWith("auth.")
    //                  && ((String)e.getKey()).endsWith(".initiatorId"))
    //          .map(e-> {
    //              String k = (String)e.getKey();
    //              return k.substring(5, k.length()-12);
    //          }).findFirst();
    //
    //          if(!x.isPresent()) {
    //              return null;
    //          }

    config.getKeys().toArray();
    // Set<String> num = config.getKeys();
    // System.out.println(num);

    Object[] num = config.getKeys().toArray();
    String id = "";
    for (int i = 0; i < num.length; i++) {
      //      System.out.println(num[5]);
      System.out.println(id);

      if (((String) num[i]).startsWith("auth.") && ((String) num[i]).endsWith(".initiatorId")) {
        System.out.println(num[i]);
        id = ((String) num[i]).substring(5, num.length - 12);
        System.out.println(id);

        // ((String) num[i]).substring(((String) num[i]).indexOf("h"), ((String)
        // num[i]).indexOf("i"));
        //      //  String id = ids[5];
        //      }
        //    	  String id = ids[5];
        System.out.println(((String) num[i]).substring(5, ((String) num[i]).length() - 12));
      }
    }

    // String id = num.get();
    String peerUsername = (String) config.get(("auth." + id + ".peerUsername" + initiatorId));
    // id is 1
    System.out.println(peerUsername);
    byte[] peerPass =
        ByteBufUtil.decodeHexDump((CharSequence) config.getString("auth." + id + ".peerPassword"));
    String hashAlgorithm = config.getString("auth." + id + ".hashAlgorithm", "SHA-1");
    return new Isp1Authentication(myUsername, myPass, peerUsername, peerPass, hashAlgorithm);
  }
}
