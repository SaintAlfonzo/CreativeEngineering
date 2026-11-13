// Copyright 2026 SaintAlfonzo. Written for the Forge 1.19.2 port of Create: Power Grid (backportedPowerGrid); GPLv3
// (LICENSE.txt). Lets the solver core (phase P1) refer to transmission lines without the game-side TransmissionLine
// class (world networks, wire entities), which ports with the grid base (P2) and implements this interface.
package org.patryk3211.powergrid.electricity.sim.special;

import org.patryk3211.powergrid.electricity.sim.node.IElectricNode;

/**
 * A transmission line as seen by {@code NetworkGraph} and {@code TransmissionLinePort}. Upstream used the concrete
 * {@code TransmissionLine} (an {@code ElectricWire}) there; its {@code getNode1()} / {@code getNode2()} satisfy this.
 */
public interface ITransmissionLine {
    IElectricNode getNode1();

    IElectricNode getNode2();
}
