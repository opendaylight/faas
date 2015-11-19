/*
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;

public class FabMgrDatastoreDependency {

    private static DataBroker dataProvider;
    private static RpcProviderRegistry rpcRegistry;
    private static NotificationService notificationService;

    public static DataBroker getDataProvider() {
        return dataProvider;
    }

    public static void setDataProvider(DataBroker dataProvider) {
        if (dataProvider != null)
            FabMgrDatastoreDependency.dataProvider = dataProvider;
    }

    public static RpcProviderRegistry getRpcRegistry() {
        return rpcRegistry;
    }

    public static void setRpcRegistry(RpcProviderRegistry rpcRegistry) {
        if (rpcRegistry != null)
            FabMgrDatastoreDependency.rpcRegistry = rpcRegistry;
    }

    public static NotificationService getNotificationService() {
        return notificationService;
    }

    public static void setNotificationService(NotificationService notificationService) {
        if (notificationService != null)
            FabMgrDatastoreDependency.notificationService = notificationService;
    }

}
