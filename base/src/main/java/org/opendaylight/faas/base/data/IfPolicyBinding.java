/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.base.data;

import java.util.List;
import java.util.Map;

@Immutable
final public class IfPolicyBinding {

    final private String ifname;
    final private Map<String, List<String>> dirPolicy;

    public IfPolicyBinding(String ifname, Map<String, List<String>> dirPolicy) {
        super();
        this.ifname = ifname;
        this.dirPolicy = dirPolicy;
    }

    @Override
    public String toString() {
        return "IfPolicyBinding [ifname=" + ifname + ", dirPolicy=" + dirPolicy + "]";
    }

    public String getIfname() {
        return ifname;
    }

    public Map<String, List<String>> getDirPolicy() {
        return dirPolicy;
    }
}
