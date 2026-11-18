package com.SaintAlfonzo.creativecommon.config;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Offline checks of Common's config watcher rules (OwnConfigLogic, plan_post_scratch_1115.md G11): own mod ids,
 * debounce timing, content guard (loop guard), and that a JDK WatchService registered for MODIFY and CREATE sees both an
 * in-place save and a rename save of a file. Run by tools/test_config_watcher.sh.
 */
public final class ConfigWatcherTest {

	private static int fails;
	private static int oks;

	private static void check(String name, boolean ok) {
		if (ok) { oks++; System.out.println("ok   " + name); }
		else { fails++; System.out.println("FAIL " + name); }
	}

	public static void main(String[] args) throws Exception {
		// own ids
		check("own: creativecompat", OwnConfigLogic.isOwn("creativecompat"));
		check("own: creativecommon", OwnConfigLogic.isOwn("creativecommon"));
		check("own: solraspberrypie", OwnConfigLogic.isOwn("solraspberrypie"));
		check("own: vinery member", OwnConfigLogic.isOwn("vinery"));
		check("not own: quark", !OwnConfigLogic.isOwn("quark"));
		check("not own: forge", !OwnConfigLogic.isOwn("forge"));
		check("not own: null", !OwnConfigLogic.isOwn(null));

		// debounce
		Path a = Path.of("/cfg/a.toml"), b = Path.of("/cfg/b.toml");
		OwnConfigLogic.Debounce d = new OwnConfigLogic.Debounce(1000);
		check("debounce: idle -> -1", d.nextDueInMs(0) == -1);
		d.event(a, 0);
		d.event(a, 300); // second write of the same save restarts the quiet time
		check("debounce: not due at 1000 after event at 300", d.takeDue(1000).isEmpty());
		check("debounce: due in 299 at 1001", d.nextDueInMs(1001) == 299);
		d.event(b, 500);
		List<Path> due = d.takeDue(1300);
		check("debounce: a due at 1300, b not", due.equals(List.of(a)));
		check("debounce: a fires once", d.takeDue(1310).isEmpty());
		check("debounce: b due at 1500", d.takeDue(1500).equals(List.of(b)));
		check("debounce: nothing pending", d.pending() == 0);
		d.event(a, 2000);
		d.event(a, 2100);
		d.event(a, 2200);
		check("debounce: burst = one reload", d.takeDue(3200).size() == 1 && d.pending() == 0);

		// content guard
		OwnConfigLogic.ContentGuard g = new OwnConfigLogic.ContentGuard();
		long h1 = OwnConfigLogic.hash("x = 1\n".getBytes(StandardCharsets.UTF_8));
		long h2 = OwnConfigLogic.hash("x = 2\n".getBytes(StandardCharsets.UTF_8));
		check("hash differs for different content", h1 != h2);
		check("hash stable", h1 == OwnConfigLogic.hash("x = 1\n".getBytes(StandardCharsets.UTF_8)));
		check("guard: unknown file counts as changed", g.changed(a, h1));
		g.applied(a, h1);
		check("guard: same content skipped (touch, own save)", !g.changed(a, h1));
		check("guard: new content reloads", g.changed(a, h2));
		g.applied(a, h2); // after the reload's correction save
		check("guard: correction save's own event skipped", !g.changed(a, h2));

		// JDK watch service: MODIFY + CREATE see in-place and rename saves
		Path dir = Files.createTempDirectory("cwtest");
		Path f = dir.resolve("creativecompat-common.toml");
		Files.writeString(f, "a = 1\n");
		try (WatchService ws = FileSystems.getDefault().newWatchService()) {
			dir.register(ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
			Files.writeString(f, "a = 2\n", StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			check("watch: in-place save seen", sawEvent(ws, dir, f));
			Path tmp = dir.resolve("sedXYZ");
			Files.writeString(tmp, "a = 3\n");
			drain(ws);
			Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			check("watch: rename save seen", sawEvent(ws, dir, f));
		} finally {
			try (var s = Files.list(dir)) { s.forEach(p -> p.toFile().delete()); }
			Files.deleteIfExists(dir);
		}

		if (fails == 0) System.out.println("ALL " + oks + " OK");
		else System.out.println(fails + " FAILED");
	}

	private static void drain(WatchService ws) throws InterruptedException {
		WatchKey k;
		while ((k = ws.poll(200, TimeUnit.MILLISECONDS)) != null) { k.pollEvents(); k.reset(); }
	}

	private static boolean sawEvent(WatchService ws, Path dir, Path f) throws InterruptedException {
		long end = System.currentTimeMillis() + 5000;
		while (System.currentTimeMillis() < end) {
			WatchKey k = ws.poll(250, TimeUnit.MILLISECONDS);
			if (k == null) continue;
			boolean hit = false;
			for (WatchEvent<?> ev : k.pollEvents()) {
				if (ev.kind() == StandardWatchEventKinds.OVERFLOW) continue;
				if (dir.resolve((Path) ev.context()).equals(f)) hit = true;
			}
			k.reset();
			if (hit) return true;
		}
		return false;
	}
}
