# Jar handle leak (420+ jar client crash)

Fixes TODO `pack/server #39`: on Linux a client with about 420 or more jars in `mods/` died while opening its window,
with no crash report (`*** stack smashing detected ***: terminated` in the launcher output, sometimes a SIGSEGV in
`libglfw.so` with an hs_err file). Core code in Creative Common 1.2.0. Tests: `CQ-FDR-` in
[scratch_queue.md](testing/scratch_queue.md).

## 1. Cause (confirmed 2026-11-14)

- FML opens every jar in `mods/` twice. `ModDirTransformerDiscoverer` opens each one (`SecureJar.from`) to look for
  services and drops the jar object; the mod scan opens it again for the game. Each open is a securejarhandler
  `UnionFileSystem` over its own zip filesystem. `UnionFileSystemProvider` keeps every one in a private map for good,
  and `UnionFileSystem.close()` only removes it from that map, never closing the zip. So the discovery copy leaks one
  descriptor per jar for the whole session (still referenced: a forced GC frees nothing).
- With 421 jars: 799 mod-jar and 225 library-jar descriptors, 1091 open just before glfwInit.
- Linux hands out the lowest free number, so GLFW's X11 connection and wakeup pipe (opened in glfwInit) got numbers
  of 1024 or higher. LWJGL 3.3.1's GLFW waits for the new window with `select()`, whose `fd_set` holds only 0 to
  1023: `FD_SET` writes past the buffer on the stack. Result: glibc's stack canary abort, or a SIGSEGV in
  `glfwCreateWindow` (hs_err frame `libglfw.so+0x2257b`).
- The limit is on descriptor numbers, so `ulimit -n` does not help. Java's own files are unaffected (poll/epoll).

## 2. Fix

| Part | Where | What |
|---|---|---|
| Release point | `mixin/FdReleaseMixin` (client) | HEAD of `RenderSystem.initBackendSystem` (`m_240147_`, which calls glfwInit) calls `StaleJarHandles.release()` once |
| Leak fix | `util/StaleJarHandles` | Lists the provider's union filesystems (private map, read with `sun.misc.Unsafe`). A filesystem is stale when its jar is directly in `mods/`, the mod list (`LoadingModList`) holds a different filesystem object for the same jar, and it is not that object. For each stale one: close its zip filesystem (frees the descriptor), then the union filesystem (drops it from the map). Logs `[jar handles] closed N stale mod jar filesystems (N zip files): A -> B descriptors open` |

The zip filesystem sits in a private record (`EmbeddedFileSystemMetadata(Path path, FileSystem fs)`), and Unsafe
refuses record fields, so the offsets come from a twin class with the same two fields. Every read is checked: the
record must have exactly those two fields, and the twin's `path` must be the very object the map is keyed by,
before `fs` is used. If a check fails it logs a warning and leaves that filesystem alone.

Not touched: service jars (their discovery copy is the live one), plain library jars with no entry in the mod list,
the dedicated server (the mixin is client-only; the leak is harmless there).

Tried first and dropped: a separate early-boot service jar (Creative Boot) that held 128 low descriptors and freed
them before glfwInit. It worked (422 jars reached the title screen), but Forge hides any jar that provides a service
from mod loading, so the service could not live inside Common. Releasing at window creation instead of before
glfwInit was also tried, and still crashed.

## 3. Results (scratch client, 2026-11-14)

| Run | Jars | Result |
|---|---|---|
| 15:30, no fix | 421 | died at window creation, stack smashing, 1097 descriptors at the peak |
| 15:52, union close + GC only | 421 | 419 closed, descriptors unchanged (1091), stack smashing |
| 16:00, zip filesystems closed | 421 | 1091 -> 672 before glfwInit; title screen; GLFW sockets and pipe at about 235 to 250; 765 open at the title screen; no ClosedFileSystemException |

World join, a long session and an F3+T resource reload are queued (CQ-FDR-02, -03).

Scratch run r1115 phase 10 (2026-11-15, [scratch_run_2026-11-15.md](testing/scratch_run_2026-11-15.md) 8): gate PASSED. CQ-FDR-01 (3 launches at 421 jars, 419 stale filesystems closed, 1091 -> 672, title in 69 s), -02 (0 ClosedFileSystemException / ClosedChannelException over 3 sessions with sounds, chat, JEI, world reloads and F3+T), -03 (mixin removed: stack smashing again, so the fix is what holds it), -04 (no `[jar handles]` line on a dedicated server) PASS.

## 4. Headroom

Every new jar adds about one descriptor after the fix (the game copy). The crash comes back near 900 jars.
