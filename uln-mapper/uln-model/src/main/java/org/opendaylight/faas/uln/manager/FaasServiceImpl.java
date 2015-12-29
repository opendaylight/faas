/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.manager;

import java.util.concurrent.Future;

import org.opendaylight.faas.uln.datastore.api.UlnDatastoreApi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.FaasEndpointsLocationsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.endpoints.locations.rev151013.RegisterEndpointLocationInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import com.google.common.util.concurrent.Futures;

public class FaasServiceImpl implements FaasEndpointsLocationsService {

    @Override
    public Future<RpcResult<Void>> registerEndpointLocation(RegisterEndpointLocationInput input) {
        UlnDatastoreApi.attachEndpointToSubnet(input);
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

}
