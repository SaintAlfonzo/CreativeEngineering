// Scratch-only probe for the Create: Power Grid port (SQ-PWG-11, plan_power_grid.md P1). Copy into a scratch server's
// kubejs/server_scripts as zz_pg_solver_probe.js, never into the pack. Runs the ported solver inside the game's class
// loader, so EJML comes from the PG jar's jar-in-jar (META-INF/jarjar), which the offline harness cannot show.
//   /pgsolve   10 V source, 1 k / 1 k divider to ground, solved on the network type the game uses
//              (GraphedElectricalNetwork, G_MIN on, backend from CSolver.SolverBackend.JAVA.create); logs
//              "[pgsolve] divider V=<v> I=<mA> converged=<bool>" then "[pgsolve] done" (or "[pgsolve] error: ...").
(() => {
    function out(s) { console.info('[pgsolve] ' + s) }
    function run() {
        try {
            let Graphed = Java.loadClass('org.patryk3211.powergrid.electricity.sim.GraphedElectricalNetwork')
            let Graph = Java.loadClass('org.patryk3211.powergrid.electricity.sim.NetworkGraph')
            let Backend = Java.loadClass('org.patryk3211.powergrid.config.CSolver$SolverBackend')
            let FloatingNode = Java.loadClass('org.patryk3211.powergrid.electricity.sim.node.FloatingNode')
            let Source = Java.loadClass('org.patryk3211.powergrid.electricity.sim.node.VoltageSourceCoupling')
            let Wire = Java.loadClass('org.patryk3211.powergrid.electricity.sim.ElectricWire')
            let net = new Graphed(new Graph(), true, ElectricalNetworkFactory(Backend))
            net.warmUp(-1)
            let top = new FloatingNode()
            let mid = new FloatingNode()
            net.addNode(top)
            net.addNode(mid)
            let src = new Source(top, null, 0.0, 10.0)
            net.addNode(src)
            net.addWire(new Wire(1000.0, top, mid))
            net.addWire(new Wire(1000.0, mid, null))
            net.calculate(1)
            out('divider V=' + mid.getVoltage() + ' I=' + (src.getCurrent() * 1000) + ' converged=' + net.isConverged())
        } catch (err) { out('error: ' + err) }
        out('done')
    }
    // Function<ElectricalNetwork, IMNA> for the graphed constructor: the enum's create(network), as WorldNetworks passes it.
    function ElectricalNetworkFactory(Backend) {
        return network => Backend.JAVA.create(network)
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('pgsolve').executes(ctx => { run(); return 1 }))
    })
})()
