// guardThrowable(fn) -> { ok, value, error }
//
// JS try/catch in this Rhino fork does NOT catch java.lang.Error (NoClassDefFoundError, LinkageError,
// AbstractMethodError, ExceptionInInitializerError...): the interpreter gives Errors no JS catch state,
// and Context.throwAsScriptRuntimeEx rethrows them unwrapped. They then also skip Commands' own
// catch (Exception), and a chat command runs inside a CompletableFuture that swallows the Throwable -
// so the command just silently stops, with nothing in the log. (That's what killed /dumprecipes.)
//
// CompletableFuture.thenApply catches any Throwable, and on an already-completed future it runs
// synchronously on this thread - so it doubles as a Throwable-level try/catch callable from JS.
// error is the underlying Throwable (CompletionException unwrapped), or a JS error object.

var guardThrowable = (function () {
	var $CompletableFuture = null
	return function (fn) {
		if (!$CompletableFuture) $CompletableFuture = Java.loadClass('java.util.concurrent.CompletableFuture')
		var out = { ok: false, value: undefined, error: null }
		$CompletableFuture.completedFuture(null)
			.thenApply(ignored => { out.value = fn(); out.ok = true; return null })
			.exceptionally(t => {
				var cause = null
				try { cause = t.getCause() } catch (e) {}
				out.error = cause || t
				return null
			})
		return out
	}
})()
