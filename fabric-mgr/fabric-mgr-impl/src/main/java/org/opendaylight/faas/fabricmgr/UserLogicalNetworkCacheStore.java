/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;

/**
 * Maintains a store of UserLogicalNetworkCache instances per tenant UuId.
 *
 * @author Thomas Pantelis
 */
public interface UserLogicalNetworkCacheStore {
    <T extends Uuid> UserLogicalNetworkCache get(T tenantId);

    <T extends Uuid> void putIfAbsent(T tenantId, UserLogicalNetworkCache cache);

    <T extends Uuid>  Set<Entry<T, UserLogicalNetworkCache>> entrySet();
}
