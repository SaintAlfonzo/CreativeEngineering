package com.SaintAlfonzo.creativecommon.perf;

import com.SaintAlfonzo.creativecommon.perf.PerfJfrEvents.KubeJSHandler;
import dev.latvian.mods.kubejs.event.EventExit;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.IEventHandler;

/**
 * Wraps each KubeJS event handler as it is registered (KubeJSEventHandlerContainerMixin), so its time
 * is known per script and line. Rhino interprets scripts, so sampling only sees the dispatcher.
 */
public final class KubeJSHandlerTiming implements IEventHandler {
	/** The event being listened to, set by KubeJSEventHandlerListenMixin while EventHandler.listen runs. */
	public static String pendingEvent;

	private final IEventHandler inner;
	private final PerfEvents.HandlerStats stats;

	private KubeJSHandlerTiming(IEventHandler inner, PerfEvents.HandlerStats stats) {
		this.inner = inner;
		this.stats = stats;
	}

	public static IEventHandler wrap(IEventHandler handler, String source, int line) {
		if (handler == null || handler instanceof KubeJSHandlerTiming) return handler;
		String event = pendingEvent == null ? "?" : pendingEvent;
		return new KubeJSHandlerTiming(handler, PerfEvents.handlerStats(event, source == null ? "?" : source, line));
	}

	@Override
	public Object onEvent(EventJS event) throws EventExit {
		if (!PerfEvents.kubejs) return inner.onEvent(event);
		KubeJSHandler e = new KubeJSHandler();
		e.begin();
		long t0 = System.nanoTime();
		try {
			return inner.onEvent(event);
		} finally {
			long dt = System.nanoTime() - t0;
			e.end();
			if (e.shouldCommit()) PerfEvents.slowHandler(e, stats);
			PerfEvents.handlerRan(stats, dt);
		}
	}
}
