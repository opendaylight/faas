/**
 *
 */
package com.huawei.enterprise.ce.data;

/**
 * @author xingjun
 *
 */
public class CESystemInfo {
    private final String sysName;
    private final String chassisID;
    private final  String subType;
    public CESystemInfo(String sysName, String chassisID, String subType) {
        super();
        this.sysName = sysName;
        this.chassisID = chassisID;
        this.subType = subType;
    }
    public String getSysName() {
        return sysName;
    }
    public String getChassisID() {
        return chassisID;
    }
    public String getSubType() {
        return subType;
    }
    @Override
    public String toString() {
        return "CESystemInfo [sysName=" + sysName + ", chassisID=" + chassisID + ", subType=" + subType + "]";
    }

}
