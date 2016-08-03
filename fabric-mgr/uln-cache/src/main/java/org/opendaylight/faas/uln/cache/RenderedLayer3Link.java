/*
 * Copyright (c) 2015, 2016 Huawei Technologies and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.faas.uln.cache;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

public final class RenderedLayer3Link {
    private final RenderedRouter sourceLr;
    private final RenderedRouter destLr;
    private final Uuid  sourceGWPort;
    private final IpAddress srcGWIP;
    private final Uuid  destGWPort;
    private final IpAddress destGWIP;
    private final RenderedSwitch lswOnSource;
    private final RenderedSwitch lswOnDest;
    private final RenderedLayer2Link link;

    public RenderedLayer3Link(RenderedRouter sourceLr, RenderedRouter destLr, RenderedSwitch lswOnSource,
            RenderedSwitch lswOnDest, Uuid sourceGWPort, IpAddress srcGWIP,  Uuid destGWPort, IpAddress destGWIP, RenderedLayer2Link link) {
        super();
        this.sourceLr = sourceLr;
        this.destLr = destLr;
        this.lswOnSource = lswOnSource;
        this.lswOnDest = lswOnDest;
        this.link = link;
        this.sourceGWPort = sourceGWPort;
        this.srcGWIP = srcGWIP;
        this.destGWPort = destGWPort;
        this.destGWIP = destGWIP;
    }


    public IpAddress getSrcGWIP() {
        return srcGWIP;
    }


    public IpAddress getDestGWIP() {
        return destGWIP;
    }


    public RenderedRouter getSourceLr() {
        return sourceLr;
    }

    public RenderedRouter getDestLr() {
        return destLr;
    }

    public RenderedSwitch getLswOnSource() {
        return lswOnSource;
    }

    public RenderedSwitch getLswOnDest() {
        return lswOnDest;
    }

    public Uuid getSourceGWPort() {
        return sourceGWPort;
    }

    public Uuid getDestGWPort() {
        return destGWPort;
    }

    public RenderedLayer2Link getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "RenderedLayer3Link [sourceLr=" + sourceLr + ", destLr=" + destLr + ", sourceGWPort=" + sourceGWPort
                + ", destGWPort=" + destGWPort + ", lswOnSource=" + lswOnSource + ", lswOnDest=" + lswOnDest + ", link="
                + link + "]";
    }

}
