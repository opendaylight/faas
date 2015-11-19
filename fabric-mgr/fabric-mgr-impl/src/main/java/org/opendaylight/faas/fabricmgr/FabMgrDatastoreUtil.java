/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabricmgr;

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.faas.fabricmgr.FabMgrDatastoreDependency;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

public class FabMgrDatastoreUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FabMgrDatastoreUtil.class);
    private static final LogicalDatastoreType logicalDatastoreType = LogicalDatastoreType.OPERATIONAL;

    public static <T extends DataObject> Optional<T> readFromDs(InstanceIdentifier<T> path, ReadTransaction rTx) {
        CheckedFuture<Optional<T>, ReadFailedException> resultFuture = rTx.read(logicalDatastoreType, path);
        try {
            return resultFuture.checkedGet();
        } catch (ReadFailedException e) {
            LOG.error("FABMGR: ERROR: Read failed from DS.", e);
            return Optional.absent();
        }
    }

    public static boolean submitToDs(WriteTransaction wTx) {
        CheckedFuture<Void, TransactionCommitFailedException> submitFuture = wTx.submit();
        try {
            submitFuture.checkedGet();
            return true;
        } catch (TransactionCommitFailedException e) {
            LOG.error("FABMGR: ERROR: Transaction commit failed to DS.", e);
            return false;
        }
    }

    public static <T extends DataObject> Optional<T> removeIfExists(InstanceIdentifier<T> path,
            ReadWriteTransaction rwTx) {
        Optional<T> potentialResult = readFromDs(path, rwTx);
        if (potentialResult.isPresent()) {
            rwTx.delete(logicalDatastoreType, path);
        }
        return potentialResult;
    }

}
