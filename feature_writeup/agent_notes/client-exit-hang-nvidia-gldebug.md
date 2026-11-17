# client-exit-hang-nvidia-gldebug

> CreativeEngineeringClient JVM never exits after quitting (window gone, 0 CPU, ATLauncher stays hidden) — NVIDIA GLX destructor deadlocks with LWJGL-attached driver thread from async GL debug callback

Moved from agent memory 2026-11-16 (last edited there 2026-09-27). Repo status docs win over any status line here.

Symptom (2026-09-26): quitting the client from the main menu leaves 2 java processes — hidden ATLauncher (keepLauncherOpen=false, waits on child) + the game JVM (window gone, 0 CPU, jstack/jcmd attach times out).

Root cause, proven with `sudo gdb -p <pid> -batch -ex "thread apply all bt"` (ptrace_scope=1 blocks non-root gdb/jhsdb):
- Render thread: System.exit -> JVM_Halt -> vm_exit, waiting on VM Thread.
- VM Thread: libc exit() -> libGLX_nvidia destructor -> pthread_join(NVIDIA driver worker thread; inherits name "Render thread", so the game shows 2 "Render thread"s).
- Driver worker: TSD destructor -> liblwjgl -> jni_DetachCurrentThread -> VM_Exit::wait_if_vm_exited -> blocks forever.
- The worker got JVM-attached because GL debug output is ASYNC: vanilla calls RenderSystem.initRenderer(options.glDebugVerbosity, false) (sync=false), and Distant Horizons 3.0.2 (overrideVanillaGLLogger=true) swaps in its own callback. The pack throws lots of GL errors, so a callback fires on the driver thread.

FIX IMPLEMENTED 2026-09-26: patcherMod client mixin `SyncGlDebugOutputMixin` — @Inject TAIL of RenderSystem.initRenderer (SRG m_69580_, static) does glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS) when caps.OpenGL43||GL_KHR_debug. Unconditional on purpose: vanilla only sets it when sync=true AND verbosity>0, but DH's GLProxy calls GLUtil.setupDebugMessageCallback on the same (main) context and enables GL_DEBUG_OUTPUT itself regardless. Built + deployed to CE2 mods/ and CreativeEngineeringClient mods/ (md5 matched); NOT yet confirmed in game (quit and check no java lingers). Config-only fallback: options.txt glDebugVerbosity:0 + DH overrideVanillaGLLogger=false. NVIDIA driver 580.173.

Diagnostic script pattern: /proc/<pid>/task/*/comm+stat+wchan works without ptrace; jstack works on ATLauncher but not on a JVM stuck in vm_exit.
