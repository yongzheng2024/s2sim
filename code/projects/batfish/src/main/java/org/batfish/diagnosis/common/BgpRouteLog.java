package org.batfish.diagnosis.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.SerializedName;
import java.util.List;

import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class BgpRouteLog {

    public Bgpv4Route toBgpv4Route() {
        RoutingProtocol protocol;
        try {
            protocol = RoutingProtocol.valueOf(_originProtocol.toUpperCase());
        }
        catch (Exception e) {
            protocol = RoutingProtocol.BGP;
        }

        // BgpRoute 构造函数只允许 BGP, IBGP, AGGREGATE
        if (protocol != RoutingProtocol.BGP && protocol != RoutingProtocol.IBGP
                && protocol != RoutingProtocol.AGGREGATE) {
            protocol = RoutingProtocol.BGP;
        }

        Bgpv4Route.Builder builder = Bgpv4Route.builder().setNetwork(getPrefix())
                .setNextHop(NextHopDiscard.instance()).setLocalPreference(_localPreference)
                .setMetric(_med).setOriginatorIp(Ip.ZERO).setReceivedFromIp(getPeerIp())
                .setProtocol(protocol)
                .setOriginType(_origin == null ? org.batfish.datamodel.OriginType.INCOMPLETE
                        : org.batfish.datamodel.OriginType
                                .valueOf(_origin.getOriginTypeName().toUpperCase()));

        if (_asPath != null) {
            builder.setAsPath(_asPath);
        }

        if (_nextHopIp != null && Ip.tryParse(_nextHopIp).isPresent()) {
            builder.setNextHopIp(Ip.parse(_nextHopIp));
        }

        builder.setCommunities(_communities);

        return builder.build();
    }

    public Set<Community> getCommunities() {
        return _communities;
    }

    public void setCommunities(Set<Community> communities) {
        _communities = communities;
    }

    public enum FromType {
        FROMORIGIN("FROMORIGIN"),
        FROMIBGP("FROMIBGP"),
        FROMEBGP("FROMEBGP");

        private String _type;

        private FromType(String type) {
            this._type = type;
        }
    }

    public enum OriginType {
        EGP("EGP", 1),
        IGP("IGP", 2),
        INCOMPLETE("INCOMPLETE", 0);

        private final String _name;

        private final int _preference;

        OriginType(String originType, int preference) {
            _name = originType;
            _preference = preference;
        }

        public String getOriginTypeName() {
            return _name;
        }

        public int getPreference() {
            return _preference;
        }
    }

    // prov BgpRouteLog attrs
    private int _id;
    private String _deviceName;
    // 就是网段信息
    @SerializedName("ipPrefix")
    private String _ipPrefixString;
    private String _nextHopIp;
    private String _nextHopDevice;

    private String _importType; // ?
    private String _originProtocol;
    private int _preferredValue;
    private long _localPreference;

    private String _fromType; // ?
    private AsPath _asPath;
    private OriginType _origin;
    private String _peerIp;
    private long _routerId;
    private long _med;
    private List<Long> _clusterList;
    // private String originalPreference;
    @SerializedName("curVpnName")
    private List<String> _curVpnList;

    // violated BgpRouteLog attrs
    private String _toDeviceName;
    private String _fromDeviceName;

    @SerializedName("exRoutePolicy")
    private String _exRoutePolicyName;

    @SerializedName("imRoutePolicy")
    private String _imRoutePolicyName;

    private Set<Community> _communities = Collections.emptySet();

    private String _jsonStr;

    private Bgpv4Route bgpv4Route;

    // 记录被拦截的BGP路由
    public void setJsonStr(Bgpv4Route bgpv4Route) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this._jsonStr = objectMapper.writeValueAsString(bgpv4Route);
        }
        catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.bgpv4Route = bgpv4Route;
    }

    // 记录被拦截的local路由
    public void setJsonStr(String routeStr) {
        this._jsonStr = routeStr;
    }

    public static BgpRouteLog of(Bgpv4Route bgpv4Route, String imPolicyName, String exPolicyName) {
        BgpRouteLog route = new BgpRouteLog();
        route.setIpPrefixString(bgpv4Route.getNetwork().toString());
        route.setNextHopIp(bgpv4Route.getNextHopIp().toString());
        route.setLocalPreference(bgpv4Route.getLocalPreference());
        // violatedPreferRoute.setNextHopDevice(remoteConfigId.getHostname());
        // route.setLocalPreference(bgpv4Route.getLocalPreference());
        route.setMed(bgpv4Route.getMed());
        route.setOriginProtocol(bgpv4Route.getProtocol().protocolName());
        route.setOrigin(OriginType.valueOf(bgpv4Route.getOriginType().toString().toUpperCase()));
        route.setPeerIp(bgpv4Route.getReceivedFromIp().toString());
        route.setCommunities(bgpv4Route.getCommunitiesAsSet());
        route.setImRoutePolicyName(imPolicyName);
        route.setExRoutePolicyName(exPolicyName);
        route.setAsPath(bgpv4Route.getAsPath());
        
        return route;
    }


    public String getJsonStr() {
        return _jsonStr;
    }

    public void setMed(long med) {
        this._med = med;
    }

    public long getMed() {
        return _med;
    }

    public void setLocalPreference(long localPreference) {
        this._localPreference = localPreference;
    }

    public long getLocalPreference() {
        return _localPreference;
    }

    public void setNextHopIp(String nextHopIp) {
        this._nextHopIp = nextHopIp;
    }

    public String getNextHopIp() {
        return _nextHopIp;
    }

    public void setNextHopDevice(String nextHopDevice) {
        this._nextHopDevice = nextHopDevice;
    }

    public String getNextHopDevice() {
        return _nextHopDevice;
    }

    public void setAsPath(AsPath asPath) {
        this._asPath = asPath;
    }

    public AsPath getAsPath() {
        return _asPath;
    }

    public void setImRoutePolicyName(String imRoutePolicyName) {
        this._imRoutePolicyName = imRoutePolicyName;
    }

    public String getImRoutePolicyName() {
        return _imRoutePolicyName;
    }

    public void setDeviceName(String deviceName) {
        this._deviceName = deviceName;
    }

    public String getDeviceName() {
        return _deviceName;
    }

    public void setIpPrefixString(String ipPrefixString) {
        this._ipPrefixString = ipPrefixString;
    }

    public String getIpPrefixString() {
        return _ipPrefixString;
    }

    public void setExRoutePolicyName(String exRoutePolicyName) {
        this._exRoutePolicyName = exRoutePolicyName;
    }

    public String getExRoutePolicyName() {
        return _exRoutePolicyName;
    }

    public String getImportType() {
        return _importType;
    }

    public void setOriginProtocol(String originProtocol) {
        this._originProtocol = originProtocol;
    }

    public String getOriginProtocol() {
        return _originProtocol;
    }

    public void setOrigin(OriginType origin) {      
        this._origin = origin;
    }

    public OriginType getOrigin() {
        return _origin;
    }

    public List<String> getCurVpnList() {
        return _curVpnList;
    }

    public Prefix getPrefix() {
        return Prefix.tryParse(_ipPrefixString).orElse(Prefix.ZERO);
    }

    public void setPeerIp(String peerIp) {
        this._peerIp = peerIp;
    }

    // public static BgpRouteLog deserialize(String jsonStr) {
    // return new Gson().fromJson(jsonStr, BgpRouteLog.class);
    // }

    public String getLatestVpnName() {
        if (_curVpnList != null && _curVpnList.size() > 0) {
            return _curVpnList.get(_curVpnList.size() - 1);
        }
        else {
            return null;
        }
    }

    public boolean isDefaultIpv4Route() {
        return _ipPrefixString.equals("0.0.0.0") || _ipPrefixString.equals("0.0.0.0/0");
    }

    public void setToDeviceName(String toDeviceName) {
        this._toDeviceName = toDeviceName;
    }

    public String getToDeviceName() {
        return _toDeviceName;
    }

    public void setFromDeviceName(String fromDeviceName) {
        this._fromDeviceName = fromDeviceName;
    }

    public String getFromDeviceName() {
        return _fromDeviceName;
    }

    /*
     * 获取路由发送方的名称：如果有fromDevName就返回，否则就以peerIp在layer2Topo上查找设备名称
     */

    public String getNextHopIpString() {
        return _nextHopIp;
    }

    public Ip getPeerIp() {
        if (Prefix.tryParse(_peerIp).isPresent()) {
            return Prefix.parse(_peerIp).getEndIp();
        }
        else if (Ip.tryParse(_peerIp).isPresent()) {
            return Ip.parse(_peerIp);
        }
        else {
            return Ip.ZERO;
        }
    }

    public String getPeerIpString() {
        return _peerIp;
    }

    public String getNextHopDev() {
        return _nextHopDevice;
    }

    public boolean ifTwoStringEqual(String str1, String str2) {
        if (str1 != null && str2 != null) {
            return str1.equals(str2);
        }
        else if (str1 != null || str2 != null) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof BgpRouteLog) {
            BgpRouteLog BgpRouteLog = (BgpRouteLog) object;
            return ifTwoStringEqual(BgpRouteLog.getDeviceName(), this._deviceName)
                    && ifTwoStringEqual(BgpRouteLog.getFromDeviceName(), this._fromDeviceName)
                    && ifTwoStringEqual(BgpRouteLog.getToDeviceName(), this._toDeviceName)
                    && ifTwoStringEqual(BgpRouteLog.getPeerIpString(), this._peerIp)
                    && ifTwoStringEqual(BgpRouteLog.getExRoutePolicyName(), this._exRoutePolicyName)
                    && ifTwoStringEqual(BgpRouteLog.getImRoutePolicyName(), this._importType)
                    && ifTwoStringEqual(BgpRouteLog.getImportType(), this._importType)
                    && ifTwoStringEqual(BgpRouteLog.getNextHopDev(), this._nextHopDevice)
                    && ifTwoStringEqual(BgpRouteLog.getOriginProtocol(), this._originProtocol)
                    && ifTwoStringEqual(BgpRouteLog.getIpPrefixString(), this._ipPrefixString);
        }
        return false;
    }

    public int hashCode() {
        return java.util.Objects.hash(_deviceName, _fromDeviceName, _toDeviceName, _peerIp,
                _exRoutePolicyName, _imRoutePolicyName, _importType, _nextHopDevice,
                _originProtocol, _ipPrefixString);
    }

}
