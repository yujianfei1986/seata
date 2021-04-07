/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.seata.rm.tcc.api;

import com.alibaba.fastjson.JSON;
import io.seata.common.Constants;
import io.seata.core.exception.TransactionException;
import io.seata.core.model.BranchStatus;
import io.seata.core.model.BranchType;
import io.seata.rm.DefaultResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Business action context.
 */
public class BusinessActionContext implements Serializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessActionContext.class);

    private static final long serialVersionUID = 6539226288677737991L;

    private static ThreadLocal<Map<String, Object>> tccContextMap = new ThreadLocal<>();

    private String xid;

    private String branchId;

    private String actionName;

    private Map<String, Object> actionContext;

    private static boolean delayed;

    /**
     * Instantiates a new Business action context.
     */
    public BusinessActionContext() {
    }

    /**
     * Instantiates a new Business action context.
     *
     * @param xid           the xid
     * @param branchId      the branch id
     * @param actionContext the action context
     */
    public BusinessActionContext(String xid, String branchId, Map<String, Object> actionContext) {
        this.xid = xid;
        this.branchId = branchId;
        this.setActionContext(actionContext);
    }

    public static boolean isDelayed() {
        return delayed;
    }

    public static void setDelayed(boolean delayed) {
        BusinessActionContext.delayed = delayed;
    }



    public void addActionContext(Map<String, Object> dataMap) {
        Map<String, Object> map = new HashMap<>(dataMap);
        if (delayed) {
            tccContextMap.set(map);
        } else {
            Map<String, Object> applicationContext = new HashMap<>(4);
            applicationContext.put(Constants.TCC_ACTION_CONTEXT, tccContextMap.get());
            try {
                DefaultResourceManager.get().branchReport(BranchType.TCC, xid, Long.parseLong(branchId),
                        BranchStatus.PhaseOne_Done, JSON.toJSONString(applicationContext));
            } catch (TransactionException e) {
                String msg = String.format("TCC branch Report error, xid: %s", xid);
                LOGGER.error(msg, e);
            } finally {
                tccContextMap.remove();
            }
        }
    }

    public Map<String, Object> getTccContextMap() {
        return tccContextMap.get();
    }

    /**
     * Gets action context.
     *
     * @param key the key
     * @return the action context
     */
    public Object getActionContext(String key) {
        return actionContext.get(key);
    }

    /**
     * Gets branch id.
     *
     * @return the branch id
     */
    public long getBranchId() {
        return branchId != null ? Long.parseLong(branchId) : -1;
    }

    /**
     * Sets branch id.
     *
     * @param branchId the branch id
     */
    public void setBranchId(long branchId) {
        this.branchId = String.valueOf(branchId);
    }

    /**
     * Gets action context.
     *
     * @return the action context
     */
    public Map<String, Object> getActionContext() {
        return actionContext;
    }

    /**
     * Sets action context.
     *
     * @param actionContext the action context
     */
    public void setActionContext(Map<String, Object> actionContext) {
        this.actionContext = actionContext;
    }

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public String getXid() {
        return xid;
    }

    /**
     * Sets xid.
     *
     * @param xid the xid
     */
    public void setXid(String xid) {
        this.xid = xid;
    }

    /**
     * Sets branch id.
     *
     * @param branchId the branch id
     */
    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    /**
     * Gets action name.
     *
     * @return the action name
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Sets action name.
     *
     * @param actionName the action name
     */
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[xid:").append(xid)
            .append(",branch_Id:").append(branchId).append(",action_name:").append(actionName)
            .append(",action_context:")
            .append(actionContext).append("]");
        return sb.toString();
    }
}
