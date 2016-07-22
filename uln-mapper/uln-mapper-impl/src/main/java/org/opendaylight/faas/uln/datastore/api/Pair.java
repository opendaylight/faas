/*
 * Copyright (c) 2015 Huawei Technologies and others. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.faas.uln.datastore.api;

/**
 * This class holds a pair of values
 *
 * @param <TypeA>
 *        - first value
 * @param <TypeB>
 *        - second value
 */
public class Pair<TypeA, TypeB> {

    private TypeA first;
    private TypeB second;

    /**
     * Constructor
     *
     * @param first
     *        first value
     * @param second
     *        second value
     */
    public Pair(TypeA first, TypeB second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Calculates hash code
     */
    @Override
    public int hashCode() {
        int hashFirst = (first != null) ? first.hashCode() : 0;
        int hashSecond = (second != null) ? second.hashCode() : 0;

        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    /**
     * Checks for equality
     *
     * @param obj
     *        the other object to compare against
     * @return TRUE if objects are equal to each other
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (isEqual(first, other.first) && isEqual(second, other.second)) {
            return true;
        }
        return false;
    }

    private boolean isEqual(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if (first != null && second != null) {
            if (first.getClass() != second.getClass()) {
                return false;
            }
            return first.equals(second);
        }
        return false;
    }

    @Override
    public String toString() {
        return "{Pair (" + first + ", " + second + ")}";
    }

    public TypeA getFirst() {
        return first;
    }

    public void setFirst(TypeA first) {
        this.first = first;
    }

    public TypeB getSecond() {
        return second;
    }

    public void setSecond(TypeB second) {
        this.second = second;
    }
}
