package org.batfish.diagnosis.repair;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.diagnosis.common.BgpRouteLog;
import org.batfish.diagnosis.common.ConfigurationLine;
import org.batfish.diagnosis.localization.RouteForbiddenLocalizer;
import org.batfish.diagnosis.localization.RoutePreferenceLocalizer;
import org.batfish.diagnosis.util.ConfigTaint;
import org.batfish.diagnosis.util.KeyWord;
import org.checkerframework.checker.units.qual.min;

public class RoutePreferenceRepairer extends Repairer {
   RoutePreferenceLocalizer localizer;


   //TODO: 考虑同一个policy多次被localized，生成repair的时候序号不要重叠
   public RoutePreferenceRepairer(RoutePreferenceLocalizer localizer) {
       this.localizer = localizer;
   }



   /*
    * 修复步骤
      STEP 0: 找到拦截的那条规则：
      *       只考虑
      *        a) [ip-prefix ... deny + route-policy ... permit] / [ip-prefix ... permit + route-policy ... deny]
      *        b) [ip-prefix ... deny + route-policy ... deny]
      *       最终都是找到那条explicitly match 的 ip-prefix + route-policy 改成permit
      STEP 1: 没有这样一条完全匹配的ip规则拦截，则在route-policy最前面新添规则
      STEP 2: 如何在现有配置上改动最小（可以都先按STEP 0做 然后压缩配置？）
    *
    */
   @Override
   public void genRepair() {
        Configuration configuration = localizer.getConfiguration();
        String policyName;
        BgpRouteLog matchRoute;
        int localPref;
        // 如果desired route没有相应policy且只有一条better route且better route有policy，就改better route的policy, 否则改desired route的policy
        if (this.localizer.getBetterRoutes().size() == 1 ) {
            matchRoute = this.localizer.getBetterRoutes().get(0);
            policyName = ConfigTaint.getRouteMapName(this.localizer.getCfgPath(), matchRoute.getPeerIpString(), true);
            localPref = (int) matchRoute.getLocalPreference() - 10;
        } else {
            matchRoute = this.localizer.getDesiredRoute();
            policyName = ConfigTaint.getRouteMapName(this.localizer.getCfgPath(), matchRoute.getPeerIpString(), true);
            localPref = this.localizer.getHighestPreference() + 10;
        }

        

        if (matchRoute == null) {
            System.out.println("Cannot find a target Route");
            return;
        }

        ConfigurationLine startLine = new ConfigurationLine(1, "");
        int minSeqNum = Integer.MAX_VALUE;
        if (policyName != null) {
            // STEP 1: 没有这样一条完全匹配的ip规则拦截，则在route-policy最前面新添规则【startLine是route-policy最前面】
            for (ConfigurationLine line : localizer.getErrorLines()) {
                    String l = line.getLine();
                    if (l.startsWith(KeyWord.ROUTE_POLICY)) {
                        String[] words = line.getLine().split(" ");
                        int seqNum = Integer.parseInt(words[words.length-1]);
                        if (seqNum < minSeqNum) {
                            minSeqNum = seqNum;
                        }
                    }
            }
            
        } else {
            policyName = matchRoute.getDeviceName() + "_From_" + matchRoute.getFromDeviceName();
            minSeqNum = 10;
        }

        
        String prefixStr = "";
        String asPathStr = "";
        int insertLine = startLine.getLineNumber();
        

        prefixStr = matchRoute.getIpPrefixString();
        asPathStr = matchRoute.getAsPath().toString().replace("[", "").replace("]", "");
        
        // 生成ip prefix-list
        String newPrefixListName = "FILTER_" + prefixStr.replace("/", "_").replace(".", "_");
        String newPrefixList = "ip prefix-list " + newPrefixListName + " seq 10 permit " + prefixStr + "\n" + "!";
        addAddedLine(insertLine, newPrefixList);
        String newAsListSeq = "10";
        String newAsList = "ip as-path access-list " + newAsListSeq + " permit " + "^" + asPathStr + "$" + "\n" + "!";
        addAddedLine(insertLine, newAsList);
        // 生成route-policy
        
        String newRoutePolicy = "route-policy " + policyName + " permit " + (minSeqNum);
        addAddedLine(insertLine, newRoutePolicy);
        addAddedLine(insertLine, "match as-path "+ newAsListSeq);
        addAddedLine(insertLine, "match ip-prefix " + newPrefixListName);
        addAddedLine(insertLine, "set local-preference " + localPref);
        addAddedLine(insertLine, "!");
   }



}
