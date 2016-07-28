/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.faas.uln.cache;

/**
 * LogicalEdgeType - defines the type of the logical Edge between two logical entities.
 * An edge has the following type:
 * LR to LR
 * LSW to LSW
 * LSW to LR
 * LSW to Subnet
 * Subnet to EndpointLocation
 */
public enum LogicalEdgeType {
    UNKNOWNTYPE, LR_LR, LSW_LSW, LR_LSW, LSW_SUBNET, SUBNET_EPLOCATION;
}
