package org.batfish.diagnosis.repair;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.diagnosis.common.ConfigurationLine;
import org.batfish.diagnosis.localization.RouteForbiddenLocalizer;
import org.batfish.diagnosis.util.KeyWord;

public class RouteForbiddenRepairer extends Repairer {
   RouteForbiddenLocalizer localizer;

   public AbstractRoute getForbiddenRoute() {
       return localizer.getRoute();
   }

   //TODO: 考虑同一个policy多次被localized，生成repair的时候序号不要重叠
   public RouteForbiddenRepairer(RouteForbiddenLocalizer localizer) {
       this.localizer = localizer;
   }


   public String getPolicyName() {
       return localizer.getPolicyName();
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

       // STEP 0.1: 找到拦截的那条route-policy规则（Batfish 模型名与配置文件 route-map 名可能不一致）
       String batfishPolicyName = localizer.getPolicyName();
       String configRouteMapName = localizer.getConfigRouteMapName();
       if (configRouteMapName == null || configRouteMapName.isEmpty()) {
           configRouteMapName = batfishPolicyName;
       }
       if (batfishPolicyName != null) {
           RoutingPolicy routingPolicy = configuration.getRoutingPolicies().get(batfishPolicyName);
       } else {
            System.out.println("没有找到policyName");
           return;
       }
       // STEP 0.2: 找到拦截的那条ip-prefix规则


       // STEP 1: 没有这样一条完全匹配的ip规则拦截，则在route-policy最前面新添规则【startLine是route-policy最前面】
       ConfigurationLine startLine = new ConfigurationLine(Integer.MAX_VALUE, "");
       for (ConfigurationLine line : localizer.getErrorLines()) {
            String lineText = line.getLine();
            if (lineText == null || lineText.trim().isEmpty()) {
                // policyLinesFinder 会把 route-map 块里的空行放进 errorLines；空行 split 后最后一个 token 是 "" 会触发 parseInt 异常
                continue;
            }
            if (line.getLineNumber() < startLine.getLineNumber()
                    && !lineText.contains(KeyWord.BGP_NEIGHBOR)) {
                startLine.setLineNumber(line.getLineNumber());
                startLine.setLine(lineText.trim());
            }
       }
       // 若仅有 neighbor 行（或其它原因未选到 startLine），则按策略名找第一条 route-map 头行
       if (startLine.getLineNumber() == Integer.MAX_VALUE || startLine.getLine().isEmpty()) {
           String routeMapPrefix = KeyWord.ROUTE_POLICY + " " + configRouteMapName + " ";
           for (ConfigurationLine line : localizer.getErrorLines()) {
               String t = line.getLine();
               if (t == null) {
                   continue;
               }
               t = t.trim();
               if (t.startsWith(routeMapPrefix)) {
                   startLine.setLineNumber(line.getLineNumber());
                   startLine.setLine(t);
                   break;
               }
           }
       }
       // 仍找不到时：任取一条 route-map 头行（permit|deny + 序号），用于 Batfish 内部名与配置名不一致且 peer 解析失败时
       if (startLine.getLineNumber() == Integer.MAX_VALUE || startLine.getLine().isEmpty()) {
           for (ConfigurationLine line : localizer.getErrorLines()) {
               String t = line.getLine();
               if (t == null) {
                   continue;
               }
               t = t.trim();
               if (!t.startsWith(KeyWord.ROUTE_POLICY + " ")) {
                   continue;
               }
               String[] parts = t.split("\\s+");
               if (parts.length >= 4
                       && (KeyWord.PERMIT.equals(parts[2]) || KeyWord.DENY.equals(parts[2]))
                       && parts[3].matches("\\d+")) {
                   startLine.setLineNumber(line.getLineNumber());
                   startLine.setLine(t);
                   break;
               }
           }
       }
       if (startLine.getLineNumber() == Integer.MAX_VALUE || startLine.getLine().isEmpty()) {
           System.out.println("RouteForbiddenRepairer: 无法在错误行中定位 route-map，跳过生成修复");
           return;
       }
       String prefixStr = "";
       String asPathStr = "";
       int insertLine = startLine.getLineNumber();
       if (localizer.getBgpv4Route()!=null) {
            prefixStr = localizer.getBgpv4Route().getNetwork().toString();
            asPathStr = localizer.getBgpv4Route().getAsPath().toString().replace("[", "").replace("]", "");
       } else {
            prefixStr = localizer.getBgpRouteLog().getIpPrefixString();
            asPathStr = localizer.getBgpRouteLog().getAsPath().toString().replace("[", "").replace("]", "");
       }
       // 生成ip prefix-list
       String newPrefixListName = "FILTER_" + prefixStr.replace("/", "_").replace(".", "_");
       String newPrefixList = "ip prefix-list " + newPrefixListName + " seq 10 permit " + prefixStr + "\n" + "!";
       addAddedLine(insertLine, newPrefixList);
       String newAsListSeq = "10";
       String newAsList = "ip as-path access-list " + newAsListSeq + " permit " + "^" + asPathStr + "$" + "\n" + "!";
       addAddedLine(insertLine, newAsList);
       // 生成route-policy（Cisco route-map 行末为序号；避免连续空格或尾部空格导致空 token）
       String[] words = startLine.getLine().trim().split("\\s+");
       String seqToken = words[words.length - 1];
       if (!seqToken.matches("\\d+")) {
           System.out.println("RouteForbiddenRepairer: 无法从行解析 route-map 序号: " + startLine.getLine());
           return;
       }
       int seqNum = Integer.parseInt(seqToken);
       String repairMapName = configRouteMapName;
       String[] hdrParts = startLine.getLine().trim().split("\\s+");
       if (hdrParts.length >= 2 && KeyWord.ROUTE_POLICY.equals(hdrParts[0])) {
           repairMapName = hdrParts[1];
       }
       String newRoutePolicy = "route-policy " + repairMapName + " permit " + (seqNum - 1);
       addAddedLine(insertLine, newRoutePolicy);
       addAddedLine(insertLine, "match as-path "+ newAsListSeq);
       addAddedLine(insertLine, "match ip-prefix " + newPrefixListName);
       addAddedLine(insertLine, "!");
   }



}
