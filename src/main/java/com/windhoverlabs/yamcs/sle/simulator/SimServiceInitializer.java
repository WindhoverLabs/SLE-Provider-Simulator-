package com.windhoverlabs.yamcs.sle.simulator;

import java.util.logging.Logger;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.Constants.ApplicationIdentifier;
import org.yamcs.jsle.provider.CltuServiceProvider;
import org.yamcs.jsle.provider.ServiceInitializer;
import org.yamcs.jsle.provider.SleService;

public class SimServiceInitializer implements ServiceInitializer {
  final YConfiguration config;
  static Logger logger = Logger.getLogger(SimServiceInitializer.class.getName());

  public SimServiceInitializer(YConfiguration config) {
    this.config = config;
  }

  @Override
  public ServiceInitResult getServiceInstance(
      String initiatorId, String responderPortId, ApplicationIdentifier appId, String sii) {
    config.getKeys().toArray();
    Object[] args = config.getKeys().toArray();
    String id = "";
    for (int i = 0; i < args.length; i++) {

      if (((String) args[i]).startsWith("service.") && ((String) args[i]).endsWith(".sii")) {
        id = ((String) args[i]).substring(8, ((String) args[i]).length() - 4);
      }
    }

    String servType = (String) config.get(("service." + id + ".type"));

    if ("raf".equals(servType)) {
      if (appId != ApplicationIdentifier.rtnAllFrames) {
        logger.info("Requested application " + appId + " does not match defined service type raf");
        return negativeResponse(6); // inconsistent service type
      }
      return null; // will be implemented properly later

    } else if ("rcf".equals(servType)) {
      if (appId != ApplicationIdentifier.rtnChFrames) {
        logger.info("Requested application " + appId + " does not match defined service type rcf");
        return negativeResponse(6); // inconsistent service type
      }
      return null; // will be implemented properly later

    } else if ("cltu".equals(servType)) {
      if (appId != ApplicationIdentifier.fwdCltu) {
        logger.info("Requested application " + appId + " does not match defined service type cltu");
        return negativeResponse(6); // inconsistent service type
      }
      return createCltuProvider(id);
    } else {
      logger.warning("Invalid value for service." + id + ".type: '" + servType + "'");
      return negativeResponse(1); // service type not supported
    }
  }
  ///////////////////////// this will be revisited later//////////////
  //    private ServiceInitResult createRafProvider(String id) {
  //        RafServiceProvider rsp = new RafServiceProvider(getFrameSource(id));
  //        return positiveResponse(id, rsp);
  //    }
  //
  //    private ServiceInitResult createRcfProvider(String id) {
  //        RcfServiceProvider rsp = new RcfServiceProvider(getFrameSource(id));
  //        return positiveResponse(id, rsp); x
  //    }
  ////////////////////////////////////////////////////////////////////////////

  private ServiceInitResult createCltuProvider(String id) {
    CltuServiceProvider csp = new CltuServiceProvider(new SimFrameSink(1000));
    return positiveResponse(id, csp);
  }
  ////////////////////// this will be revisited later///////////////////////////
  //    private FrameSource getFrameSource(String id) {
  //        String sid = SimUtil.getProperty(properties, "service." + id + ".fsource");
  //        FrameSource frameSource = FrameSources.getSource(sid);
  //        if (frameSource == null) {
  //            throw new SimConfigurationException("Unknown frame source '" + sid + "'");
  //        }
  //        return frameSource;
  //    }
  //////////////////////////////////////////////////////////////////////////////

  private ServiceInitResult positiveResponse(String id, SleService service) {
    ServiceInitResult r = new ServiceInitResult();
    r.success = true;
    r.sleService = service;
    r.name = id;
    return r;
  }

  ServiceInitResult negativeResponse(int diag) {
    ServiceInitResult r = new ServiceInitResult();
    r.success = false;
    r.diagnostic = diag;
    return r;
  }
}
