/*
 * Copyright (c) 2017 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.faas.uln.cache.UserLogicalNetworkCache;
import org.opendaylight.yang.gen.v1.urn.opendaylight.faas.logical.faas.common.rev151013.Uuid;

/**
 * UserLogicalNetworkCacheStore implementation.
 *
 * @author Thomas Pantelis
 */
public class UserLogicalNetworkCacheStoreImpl implements UserLogicalNetworkCacheStore {
    private final Map<Uuid, UserLogicalNetworkCache> ulnStore = new ConcurrentHashMap<>();

    @Override
    public UserLogicalNetworkCache get(Uuid tenantId) {
        return ulnStore.get(tenantId);
    }

    @Override
    public void putIfAbsent(Uuid tenantId, UserLogicalNetworkCache cache) {
        ulnStore.putIfAbsent(tenantId, cache);
    }

    @Override
    public Set<Entry<Uuid, UserLogicalNetworkCache>> entrySet() {
        return Collections.unmodifiableSet(ulnStore.entrySet());
    }
}
