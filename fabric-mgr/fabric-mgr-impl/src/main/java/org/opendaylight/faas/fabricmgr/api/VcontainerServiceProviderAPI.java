/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.fabricmgr.api;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.faas.fabricmgr.FabricMgrProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer2Input;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.vcontainer.netnode.rev151010.CreateLneLayer3Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VcontainerServiceProviderAPI {

    private static final Logger LOG = LoggerFactory.getLogger(VcontainerServiceProviderAPI.class);
    private static final LogicalDatastoreType OPERATIONAL = LogicalDatastoreType.OPERATIONAL;
    private static FabricMgrProvider fabricMgrProvider;

    public static void createLneLayer2(Uuid tenantId, CreateLneLayer2Input lne2Input) {
        VcontainerServiceProviderAPI.fabricMgrProvider.createLneLayer2(tenantId, lne2Input);
    }

    public static void createLneLayer3(Uuid tenantId, CreateLneLayer3Input lne3Input) {
        VcontainerServiceProviderAPI.fabricMgrProvider.createLneLayer3(tenantId, lne3Input);
    }

    public static void setFabricMgrProvider(FabricMgrProvider fabricMgrProvider) {
        VcontainerServiceProviderAPI.fabricMgrProvider = fabricMgrProvider;
    }

    public static FabricMgrProvider getFabricMgrProvider() {
        return VcontainerServiceProviderAPI.fabricMgrProvider;
    }
}
