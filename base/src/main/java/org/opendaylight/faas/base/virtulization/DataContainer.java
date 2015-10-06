/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.virtulization;

import java.util.Map;

public interface DataContainer {

    Map<String, Link> getLinkList();

    Map<String, VIF> getVIFList();

    Map<String, NetNode> getNodeList();
}
