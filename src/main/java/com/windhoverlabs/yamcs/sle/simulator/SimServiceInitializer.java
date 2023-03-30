package com.windhoverlabs.yamcs.sle.simulator;

import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import org.yamcs.jsle.Constants.ApplicationIdentifier;
import org.yamcs.jsle.provider.CltuServiceProvider;
import org.yamcs.jsle.provider.ServiceInitializer;
import org.yamcs.jsle.provider.SleService;

public class SimServiceInitializer implements ServiceInitializer {
  final Properties properties;
  static Logger logger = Logger.getLogger(SimServiceInitializer.class.getName());

  public SimServiceInitializer(Properties properties) {
    this.properties = properties;
  }

  @Override
  public ServiceInitResult getServiceInstance(
      String initiatorId, String responderPortId, ApplicationIdentifier appId, String sii) {
    // look for an entry where auth.x.initiatorId = initiatorId and return x
    Optional<String> x =
        properties.entrySet().stream()
            .filter(
                e ->
                    sii.equals(e.getValue())
                        && ((String) e.getKey()).startsWith("service.")
                        && ((String) e.getKey()).endsWith(".sii"))
            .map(
                e -> {
                  String k = (String) e.getKey();
                  return k.substring(8, k.length() - 4);
                })
            .findFirst();

    if (!x.isPresent()) {
      logger.info("Cannot find a service entry for sii='" + sii + "'");
      return negativeResponse(3); // no such service instance
    }

    String id = x.get();
    String servType = properties.getProperty("service." + id + ".type");

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

  //    private ServiceInitResult createRafProvider(String id) {
  //        RafServiceProvider rsp = new RafServiceProvider(getFrameSource(id));
  //        return positiveResponse(id, rsp); // this will be revisited later
  //    }
  //
  //    private ServiceInitResult createRcfProvider(String id) {
  //        RcfServiceProvider rsp = new RcfServiceProvider(getFrameSource(id));
  //        return positiveResponse(id, rsp); // this will be revisited later
  //    }

  private ServiceInitResult createCltuProvider(String id) {
    CltuServiceProvider csp = new CltuServiceProvider(new SimFrameSink(1000));
    return positiveResponse(id, csp);
  }

  //    private FrameSource getFrameSource(String id) {
  //        String sid = SimUtil.getProperty(properties, "service." + id + ".fsource");
  //        FrameSource frameSource = FrameSources.getSource(sid);
  //        if (frameSource == null) {
  //            throw new SimConfigurationException("Unknown frame source '" + sid + "'");
  //        }
  //        return frameSource;
  //    } // this will be revisited later

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
