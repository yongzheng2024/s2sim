package org.batfish.diagnosis.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.batfish.datamodel.Bgpv4Route;

/*
 * Each BGP Condition is for a router (for single prefix)
 */
public class BgpCondition {

    @JsonProperty("ipPrefix")
    private String _networkString;

    @JsonProperty("redistribution")
    private boolean _redistribution;

    @JsonProperty("rrClients")
    private Set<String> _rrClients;

    //
    @JsonProperty("selectionRoute")
    private Set<SelectionRoute> _routes;

    @JsonProperty("propNeighbors")
    private Set<String> _propNeighbors;

    @JsonProperty("acptNeighbors")
    private Set<String> _acptNeighbors;

    @JsonProperty("ibgpPeers")
    private Set<String> _ibgpPeers;

    @JsonProperty("ebgpPeers")
    private Set<String> _ebgpPeers;

    public BgpCondition(Builder builder) {
        this._networkString = builder._network;
        this._redistribution = builder._redistribution;
        this._routes = builder._routes;
        if (this._routes==null) {
            this._routes = new HashSet<>();
        }
        this._propNeighbors = builder._propNeighbors;
        this._acptNeighbors = builder._acptNeighbors;
        this._rrClients = builder._rrClients;
        this._ibgpPeers = builder._ibgpPeers;
        this._ebgpPeers = builder._ebgpPeers;
    }

//    public static Map<String, BgpCondition> deserialize(String filePath) {
//        File file = new File(filePath);
//        String jsonStr;
//        try {
//            jsonStr = FileUtils.readFileToString(file,"UTF-8");
//            Map<String, BgpCondition> conds = new Gson().fromJson(jsonStr, new TypeToken<Map<String, BgpCondition>>() {}.getType());
//            return conds;
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static void serialize(Map<String, BgpCondition> conditions, String filePath) {
        // 创建 ObjectMapper 实例
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // 将对象序列化为 JSON，并写入文件
            objectMapper.writeValue(new File(filePath), conditions);
            System.out.println("Serialization successful.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDesiredBestRoute(Bgpv4Route transformedIncomingRoute) {
        // Batfish仿真过程中route的下一跳不会记录子网掩码长度；但condition会记录，会导致不匹配
            String nexthopIp = transformedIncomingRoute.getNextHopIp().toString() + "/32";
            // Batfish的AsPath会用方括号标记，condition记录不会，会导致不匹配
            String asPath = transformedIncomingRoute.getAsPath().toString();
            String network = transformedIncomingRoute.getNetwork().toString();
            List<String> selectNexthopIps = get_route().get_nextHopIps();
            List<Long> selectAsPath = get_route().get_asPath();
            String selectNetwork = get_route().get_networkString();
            return selectNexthopIps.contains(nexthopIp) && selectAsPath.toString().equals(asPath)
                && selectNetwork.equals(network);
    }

    public void setRedistribute(boolean value) {
        this._redistribution = value;
    }

    public String get_networkString() {return _networkString;}

    public boolean getRedistribution() {return _redistribution;}

    public Set<String> get_rrClients() {return _rrClients;}

    public SelectionRoute get_route() {
        if (_routes.iterator().hasNext()) {
            return _routes.iterator().next();
        } else {
            // 处理没有元素的情况，可以返回 null 或者抛出异常等
            return null;
        }
    }

    public Set<String> get_propNeighbors() {return _propNeighbors;}

    public Set<String> get_acptNeighbors() {return _acptNeighbors;}

    public Set<String> get_ebgpPeers() {return _ebgpPeers;}

    public Set<String> get_ibgpPeers() {return _ibgpPeers;}

    public static class Builder {
        private String _network;

        private boolean _redistribution;

        private Set<String> _rrClients;

        private Set<SelectionRoute> _routes;

        private Set<String> _propNeighbors;

        private Set<String> _acptNeighbors;

        private Set<String> _ibgpPeers;

        private Set<String> _ebgpPeers;

        private String _vpnName;

        public Builder(String network) {
            this._network = network;
        }

        public Builder vpnName(String name) {
            this._vpnName = name;
            return this;
        }

        public Builder redistribution(boolean flag) {
            this._redistribution = flag;
            return this;
        }

        public Builder rrClient(Set<String> rrClients) {
            this._rrClients = rrClients;
            return this;
        }

        public Builder selectionRoutes(Set<SelectionRoute> routes) {
            this._routes = routes;
            return this;
        }

        public Builder propNeighbors(Set<String> nodes) {
            this._propNeighbors = nodes;
            return this;
        }

        public Builder acptNeighbors(Set<String> nodes) {
            this._acptNeighbors = nodes;
            return this;
        }

        public Builder ibgpPeers(Set<String> peers) {
            this._ibgpPeers = peers;
            return this;
        }

        public Builder ebgpPeers(Set<String> peers) {
            this._ebgpPeers = peers;
            return this;
        }

        public BgpCondition build() {
            return new BgpCondition(this);
        }
    }

}
