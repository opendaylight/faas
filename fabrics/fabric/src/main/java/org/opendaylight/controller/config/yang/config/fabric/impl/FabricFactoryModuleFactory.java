/**
 * Copyright (c) 2015 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.config.yang.config.fabric.impl;

import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.spi.Module;
import org.osgi.framework.BundleContext;

public class FabricFactoryModuleFactory extends AbstractFabricFactoryModuleFactory {

    @Override
    public Module createModule(String instanceName,
            DependencyResolver dependencyResolver,
            BundleContext bundleContext) {

        return super.createModule(instanceName, dependencyResolver, bundleContext);
    }
}
