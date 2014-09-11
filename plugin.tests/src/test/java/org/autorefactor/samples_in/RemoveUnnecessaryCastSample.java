/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2014 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.samples_in;

public class RemoveUnnecessaryCastSample {

    private Object removeCast(Integer i) {
        return (Integer) i;
    }

    private Object removeCast(int i) {
        return (Integer) i;
    }

    private Integer doNotRemoveCast(Object o) {
        return (Integer) o;
    }

    private int doNotRemoveNarrowingCast(Object o) {
        return (Integer) o;
    }

    private long removeWideningPrimitiveCast(int i) {
        return (long) i;
    }

    private int doNotRemoveNarrowingCast(long l) {
        return (int) l;
    }

    private void removeAssignmentCasts(Integer oi, int pi, Object o) {
        Object o1;
        long l;
        o1 = (Integer) oi;
        o1 = (Integer) pi;
        l = (long) pi;
    }

    private void doNotRemoveAssignmentCasts(Object o, long l) {
        Integer oi;
        int i;
        oi = (Integer) o;
        i = (Integer) o;
        i = (int) l;
    }

    private void removeVariableDeclarationFragmentCasts(Integer oi, int pi, Object o) {
        Object o1 = (Integer) oi;
        Object o2 = (Integer) pi;
        long l = (long) pi;
    }

    private void doNotRemoveVariableDeclarationFragmentCasts(Object o, long l) {
        Integer oi1 = (Integer) o;
        int pi2 = (Integer) o;
        int pi3 = (int) l;
    }

    private boolean removeCasts(Integer oi, int pi, long l) {
        return oi != (Integer) pi
            && (Integer) pi != oi
            && (int) oi != pi
            && pi != (int) oi
            && l != (long) pi
            && (long) pi != l;
    }

    private boolean doNotRemoveCasts(Integer oi, int pi, long l, Object o) {
        return (int) l != pi
            && pi != (int) l
            && (Integer) o != pi // FIXME cast to (int) after moving to Java 7 builds
            && pi != (Integer) o // FIXME cast to (int) after moving to Java 7 builds
            && (Integer) o != oi // FIXME cast to (int) after moving to Java 7 builds
            && oi != (Integer) o; // FIXME cast to (int) after moving to Java 7 builds
    }

}
