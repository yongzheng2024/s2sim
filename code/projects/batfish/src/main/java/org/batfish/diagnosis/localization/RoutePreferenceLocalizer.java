package org.batfish.diagnosis.localization;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.diagnosis.common.BgpRouteLog;
import org.batfish.diagnosis.common.ConfigurationLine;
import org.batfish.diagnosis.repair.Repairer;
import org.batfish.diagnosis.repair.RoutePreferenceRepairer;
import org.batfish.diagnosis.util.ConfigTaint;
import org.batfish.diagnosis.util.KeyWord;

public class RoutePreferenceLocalizer extends Localizer {

   // BgpRouteLog _shouldPreferRoute;
   // BgpRouteLog _actualPreferRoute;
   String _node;
   public String getNode() {
      return _node;
   }

   // 实际比当前希望的最优路由好的路由
   List<BgpRouteLog> _betterRoutes;
   // 当前希望的最优路由
   BgpRouteLog _desiredRoute;
   Configuration _configuration;

   public BgpRouteLog getDesiredRoute() {
      return _desiredRoute;
   }

   public void setDesiredRoute(BgpRouteLog desiredRoute) {
      _desiredRoute = desiredRoute;
   }

   public Configuration getConfiguration() {
      return _configuration;
   }

   private String _cfgPath;

   public String getCfgPath() {
      return _cfgPath;
   }

   public RoutePreferenceLocalizer(String node, List<BgpRouteLog> betterRoutes, String cfgPath,
         Configuration configuration, BgpRouteLog desiredRoute) {
      // 传入的bgp topo需要是假设的，不然可能找不到peer dev
      this._node = node;
      // 如果是因为export被deny的，在export那端会有记录
      this._betterRoutes = betterRoutes;
      this._cfgPath = cfgPath;
      this._configuration = configuration;
      this._desiredRoute = desiredRoute;
   }

   public List<BgpRouteLog> getBetterRoutes() {
      return _betterRoutes;
   }

   public int getHighestPreference() {
      int highestPreference = 0;
      for (BgpRouteLog r : _betterRoutes) {
         highestPreference = (int) Math.max(highestPreference, r.getLocalPreference());
      }
      return highestPreference;
   }

   @Override
   public List<ConfigurationLine> genErrorConfigLines() {
      for (BgpRouteLog r : _betterRoutes) {
         String peerIp = r.getPeerIpString();
         // 对每条记录的路由查找有没有配置route-map的命令
         String[] keyWords = { "neighbor", peerIp, KeyWord.ROUTE_POLICY, "in" };
         Map<Integer, String> peerRpLines = ConfigTaint.peerTaint(_node, keyWords, _cfgPath);
         // 存在策略，根据配置命令获得策略名，并查找相关的策略
         if (!peerRpLines.isEmpty()) {
            addErrorLines(peerRpLines);
            // 根据应用配置的命令查找真实的策略名
            for (Integer i : peerRpLines.keySet()) {
               String line = peerRpLines.get(i);
               if (line.contains(KeyWord.ROUTE_POLICY)) {
                  String[] parts = line.split(KeyWord.ROUTE_POLICY);
                  if (parts.length > 1) {
                     String policyName = parts[1].trim().split(" ")[0];
                     addErrorLines(findMatchingRuleLines(policyName, r));
                  }
               }
            }
         }
      }

      if (_desiredRoute != null) {
         String peerIp = _desiredRoute.getPeerIpString();
         // 对每条记录的路由查找有没有配置route-map的命令
         String[] keyWords = { "neighbor", peerIp, KeyWord.ROUTE_POLICY, "in" };
         Map<Integer, String> peerRpLines = ConfigTaint.peerTaint(_node, keyWords, _cfgPath);
         if (!peerRpLines.isEmpty()) {
            addErrorLines(peerRpLines);
            for (Integer i : peerRpLines.keySet()) {
               String line = peerRpLines.get(i);
               if (line.contains(KeyWord.ROUTE_POLICY)) {
                  String[] parts = line.split(KeyWord.ROUTE_POLICY);
                  if (parts.length > 1) {
                     String policyName = parts[1].trim().split(" ")[0];
                     addErrorLines(findMatchingRuleLines(policyName, _desiredRoute));
                  }
               }
            }
         }
      }

      return getErrorLines();
   }

   /**
    * 找到匹配当前路由的特定的 route-map 规则及其引用的资源（如 prefix-list）
    */
   private Map<Integer, String> findMatchingRuleLines(String policyName, BgpRouteLog route) {
      Map<Integer, String> allPolicyLines = ConfigTaint.policyLinesFinder(_node, policyName,
            _cfgPath);
      if (allPolicyLines.isEmpty()) {
         return Collections.emptyMap();
      }

      // 按 sequence (node) 分组
      Map<Integer, Map<Integer, String>> rules = new LinkedHashMap<>();
      int currentSeqStart = -1;
      for (Map.Entry<Integer, String> entry : allPolicyLines.entrySet()) {
         String line = entry.getValue().trim();
         if (line.startsWith(KeyWord.ROUTE_POLICY)) {
            currentSeqStart = entry.getKey();
            rules.put(currentSeqStart, new LinkedHashMap<>());
         }
         if (currentSeqStart != -1) {
            rules.get(currentSeqStart).put(entry.getKey(), entry.getValue());
         }
      }

      // 遍历每个规则检查是否匹配
      for (Map.Entry<Integer, Map<Integer, String>> ruleEntry : rules.entrySet()) {
         Map<Integer, String> ruleLines = ruleEntry.getValue();
         if (isRuleMatch(ruleLines, route)) {
            Map<Integer, String> matchedLines = new LinkedHashMap<>(ruleLines);
            matchedLines.putAll(findReferencedLines(ruleLines));
            return matchedLines;
         }
      }

      return Collections.emptyMap();
   }

   /**
    * 检查 route-map 的一个 sequence 是否匹配当前路由
    */
   private boolean isRuleMatch(Map<Integer, String> ruleLines, BgpRouteLog routeLog) {
      Bgpv4Route route = routeLog.toBgpv4Route();

      for (String line : ruleLines.values()) {
         line = line.trim();
         if (line.startsWith(KeyWord.POLICY_MATCH)) {
            // 匹配 prefix-list (ip address prefix-list NAME)
            if (line.contains("ip address prefix-list")) {
               String plName = extractName(line, "prefix-list");
               RouteFilterList rfl = _configuration.getRouteFilterLists().get(plName);
               if (rfl != null && !rfl.permits(route.getNetwork())) {
                  return false;
               }
            }
            // 匹配 as-path (match as-path NAME)
            else if (line.contains("as-path")) {
               String aclName = extractName(line, "as-path");
               AsPathAccessList acl = _configuration.getAsPathAccessLists().get(aclName);
               if (acl != null && !acl.permits(route.getAsPath())) {
                  return false;
               }
            }
            // 匹配 community (match community NAME)
            else if (line.contains("community")) {
               String clName = extractName(line, "community");
               CommunityList cl = _configuration.getCommunityLists().get(clName);
               if (cl != null
                     && !cl.matchCommunities(null, route.getCommunities().getCommunities())) {
                  return false;
               }
            }
            // 匹配 next-hop (match ip next-hop NAME 或 match ip next-hop prefix-list NAME)
            else if (line.contains("next-hop")) {
               if (line.contains("prefix-list")) {
                  String nhName = extractName(line, "prefix-list");
                  RouteFilterList rfl = _configuration.getRouteFilterLists().get(nhName);
                  if (rfl != null && !rfl
                        .permits(Prefix.create(route.getNextHopIp(), Prefix.MAX_PREFIX_LENGTH))) {
                     return false;
                  }
               }
               else {
                  String nhName = extractName(line, "next-hop");
                  // 可能是 access-list
                  IpAccessList acl = _configuration.getIpAccessLists().get(nhName);
                  if (acl != null) {
                     // 检查 IpAccessList 是否允许该下一跳 IP
                     // 这里采用简化实现：只要 ACL 中存在 PERMIT 且没有显式 DENY 该 IP 则认为可能匹配
                     // 实际应使用 acl.filter()，但此处为简化诊断
                     boolean hasPermit = acl.getLines().stream().anyMatch(
                           aclLine -> aclLine instanceof ExprAclLine && ((ExprAclLine) aclLine)
                                 .getAction() == org.batfish.datamodel.LineAction.PERMIT);
                     if (!hasPermit) {
                        return false;
                     }
                  }
               }
            }
         }
      }
      return true;
   }

   private String extractName(String line, String keyword) {
      String[] parts = line.split(keyword);
      if (parts.length > 1) {
         return parts[1].trim().split(" ")[0];
      }
      return "";
   }

   /**
    * 查找规则中引用的 prefix-list 或 as-path-list 的配置行
    */
   private Map<Integer, String> findReferencedLines(Map<Integer, String> ruleLines) {
      Map<Integer, String> refLines = new LinkedHashMap<>();
      for (String line : ruleLines.values()) {
         line = line.trim();
         if (line.startsWith(KeyWord.POLICY_MATCH)) {
            if (line.contains("ip address prefix-list")) {
               String[] parts = line.split("prefix-list");
               if (parts.length > 1) {
                  String plName = parts[1].trim().split(" ")[0];
                  refLines.putAll(ConfigTaint.taint(_node,
                        new String[] { "ip", "prefix-list", plName }, _cfgPath));
               }
            }
            else if (line.contains("as-path")) {
               String aclName = extractName(line, "as-path");
               refLines.putAll(ConfigTaint.taint(_node,
                     new String[] { "ip", "as-path", "access-list", aclName }, _cfgPath));
            }
            else if (line.contains("community")) {
               String clName = extractName(line, "community");
               refLines.putAll(ConfigTaint.taint(_node,
                     new String[] { "ip", "community-list", clName }, _cfgPath));
            }
            else if (line.contains("next-hop")) {
               if (line.contains("prefix-list")) {
                  String nhName = extractName(line, "prefix-list");
                  refLines.putAll(ConfigTaint.taint(_node,
                        new String[] { "ip", "prefix-list", nhName }, _cfgPath));
               }
               else {
                  String nhName = extractName(line, "next-hop");
                  refLines.putAll(
                        ConfigTaint.taint(_node, new String[] { "access-list", nhName }, _cfgPath));
               }
            }
         }
      }
      return refLines;
   }

   @Override
   public Repairer genRepairer() {
      // TODO Auto-generated method stub
      RoutePreferenceRepairer repairer = new RoutePreferenceRepairer(this);
      repairer.genRepair();
      return repairer;
   }

}
