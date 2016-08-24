/**
 * Copyright (c) 2016 Huawei Technologies Co. Ltd. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.fabric.utils;

import com.google.common.net.InetAddresses;

import java.util.Arrays;
import java.util.Map.Entry;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

public class IpAddressUtils {

    public static IpAddress getIpAddress(IpPrefix ip) {
        StringBuilder buf = new StringBuilder();
        for (char x : ip.getValue()) {
            if (x == '/') {
                break;
            }
            buf.append(x);
        }
        return new IpAddress(buf.toString().toCharArray());
    }

    public static int getMask(IpPrefix ip) {
        StringBuilder buf = new StringBuilder();
        boolean foundSlash = false;
        for (char x : ip.getValue()) {
            if (!foundSlash && x == '/') {
                foundSlash = true;
                continue;
            }
            if (foundSlash) {
                buf.append(x);
            }
        }
        return Integer.parseInt(buf.toString());
    }

    public static IpPrefix createGwPrefix(IpAddress ipAddress, IpPrefix network) {
        StringBuilder buf = new StringBuilder();
        buf.append(ipAddress.getValue());
        String str = network.getIpv4Prefix().getValue();
        buf.append(str.substring(str.indexOf("/")));
        return new IpPrefix(new Ipv4Prefix(buf.toString()));
    }

    public static IpPrefix createDefaultPrefix(IpAddress ipAddress) {
        if (ipAddress.getIpv4Address() == null) {
            return null;
        }
        String ipv4 = ipAddress.getIpv4Address().getValue();
        long mask = getDefaultMask(ipv4);
        return new IpPrefix(new Ipv4Prefix(String.format("%s/%d", ipv4, mask)));
    }

    public static long getDefaultMask(String ipv4Address) {
        long ipLong = (InetAddresses.coerceToInteger(InetAddresses.forString(ipv4Address))) & 0xFFFFFFFFL;
        if (ipLong < 2147483647L) { // 0.0.0.0 - 127.255.255.255
            return 8;
        }
        if (ipLong < 3221225471L) { // 128.0.0.0 - 191.255.255.255
            return 16;
        }
        if (ipLong < 3758096383L) { // 192.0.0.0 - 223.255.255.255
            return 24;
        }
        return 32;// other
    }

    public static Ipv4Prefix canonicalizeIpPrefixToNetAddress(Ipv4Prefix oldIpv4Prefix) {

        Entry<Ipv4Address, Integer> ipv4PrefixEntry = IetfInetUtil.INSTANCE.splitIpv4Prefix(oldIpv4Prefix);

        byte[] addressValue = IetfInetUtil.INSTANCE.ipv4AddressBytes(ipv4PrefixEntry.getKey());

        int prefixLengthValue = ipv4PrefixEntry.getValue();

        byte[] result = new byte[addressValue.length];

        if (prefixLengthValue == 0) {
            for (int i = 0; i < 4; i++) {
                result[i] = 0;
            }

            return IetfInetUtil.INSTANCE.ipv4PrefixFor(result, prefixLengthValue);
        }

        result = Arrays.copyOf(addressValue, addressValue.length);

        // Set all bytes after the end of the prefix to 0
        int lastByteIndex = (prefixLengthValue - 1) / Byte.SIZE;
        for (int i = lastByteIndex; i < 4; i++) {
            result[i] = 0;
        }

        byte lastByte = addressValue[lastByteIndex];
        byte mask = 0;
        byte msb = (byte) 0x80;
        int lastBit = (prefixLengthValue - 1) % Byte.SIZE;
        for (int i = 0; i < Byte.SIZE; i++) {
            if (i <= lastBit) {
                mask |= (msb >> i);
            }
        }

        result[lastByteIndex] = (byte) (lastByte & mask);

        return IetfInetUtil.INSTANCE.ipv4PrefixFor(result, prefixLengthValue);
    }
}
