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

import java.util.logging.Logger;
import org.yamcs.YConfiguration;
import org.yamcs.jsle.Constants.ApplicationIdentifier;
import org.yamcs.jsle.provider.CltuServiceProvider;
import org.yamcs.jsle.provider.FrameSource;
import org.yamcs.jsle.provider.RafServiceProvider;
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
    Object[] keys = config.getKeys().toArray();
    String id = "";
    System.out.println("SII-->" + sii);

    for (int i = 0; i < keys.length; i++) {
      if (sii.equals(config.get((String) keys[i]))
          && ((String) keys[i]).startsWith("service.")
          && ((String) keys[i]).endsWith(".sii")) {
        id = ((String) keys[i]).substring(8, ((String) keys[i]).length() - 4);
      }
    }

    String servType = (String) config.get(("service." + id + ".type"));

    if ("raf".equals(servType)) {
      if (appId != ApplicationIdentifier.rtnAllFrames) {
        logger.info("Requested application " + appId + " does not match defined service type raf");
        return negativeResponse(6); // inconsistent service type
      }
      return createRafProvider(id); // will be implemented properly later

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
  /////////////////////// this will be revisited later//////////////
  private ServiceInitResult createRafProvider(String id) {
    FrameSource f = new rafFrameSource(config);
    f.startup();
    RafServiceProvider rsp = new RafServiceProvider(f);
    System.out.println(rsp);
    return positiveResponse(id, rsp);
  }
  //  private ServiceInitResult createRafProvider(String id) {
  //	         RafServiceProvider rsp = new RafServiceProvider(getFrameSource(id));
  //	         return positiveResponse(id, rsp);
  //	     }

  //      private ServiceInitResult createRcfProvider(String id) {
  //          RcfServiceProvider rsp = new RcfServiceProvider(getFrameSource(id));
  //          return positiveResponse(id, rsp); x
  //      }
  //////////////////////////////////////////////////////////////////////////

  private ServiceInitResult createCltuProvider(String id) {
    CltuServiceProvider csp = new CltuServiceProvider(new SimFrameSink(1000));
    return positiveResponse(id, csp);
  }
  //////////////////// this will be revisited later///////////////////////////
  //      private FrameSource getFrameSource(String id) {
  //          String sid = SimUtil.getProperty(properties, "service." + id + ".fsource");
  //          FrameSource frameSource = FrameSources.getSource(sid);
  //          if (frameSource == null) {
  //              throw new SimConfigurationException("Unknown frame source '" + sid + "'");
  //          }
  //          return frameSource;
  //      }
  ////////////////////////////////////////////////////////////////////////////

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
