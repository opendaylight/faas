/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabMgrDatastoreUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FabMgrDatastoreUtil.class);

    public static <T extends DataObject> Optional<T> readData(final LogicalDatastoreType storeType,
            final InstanceIdentifier<T> path) {
        final ReadTransaction tx = FabMgrDatastoreDependency.getDataProvider().newReadOnlyTransaction();
        CheckedFuture<Optional<T>, ReadFailedException> resultFuture = tx.read(storeType, path);
        try {
            return resultFuture.checkedGet();
        } catch (ReadFailedException e) {
            LOG.error("FABMGR: ERROR: Read failed from DS.", e);
            return Optional.absent();
        }
    }

    public static <T extends DataObject> void deleteData(final LogicalDatastoreType storeType,
            final InstanceIdentifier<T> path) {
        final WriteTransaction tx = FabMgrDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        tx.delete(storeType, path);
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.trace("FABMGR: Data has deleted from datastore {} {}", storeType, path);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("FABMGR: ERROR: Can not delete data from datastore [store: {}] [path: {}] [exception: {}]",
                        storeType, path, t);
            }

        });
    }

    public static <T extends DataObject> void putData(final LogicalDatastoreType storeType,
            final InstanceIdentifier<T> path, final T data) {
        final WriteTransaction tx = FabMgrDatastoreDependency.getDataProvider().newWriteOnlyTransaction();
        tx.put(storeType, path, data, true);
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.trace("FABMGR: Data has put into datastore {} {}", storeType, path);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.error("FABMGR: ERROR: Can not put data into datastore [store: {}] [path: {}] [exception: {}]",
                        storeType, path, t);
            }
        });

    }
}
