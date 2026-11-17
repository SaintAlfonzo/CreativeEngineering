#!/usr/bin/env python3
"""Install one built jar into mods/ (plan_process_overhaul.md P1.2 items 2 and 7; owner ruling D8). Called by
installToMods (build-logic ce.forge-mod.gradle); usable by hand with the same arguments.

Policy: a rebuild for testing is installed without asking. When the owner's game is running (a `java` process whose
cwd is the instance root and that holds logs/latest.log open; pid, cwd and open files only, never the command line,
so headless test JVMs run from the root do not count), a dialog (zenity, else yad) asks:
  Install now      re-checks the game; still running -> the dialog comes back with "game still running"
  Install on exit  a detached watcher (this script, --watch) installs a held copy of the jar when the game exits,
                   sends notify-send and writes the ledger row (on-exit). A newer watcher for the same jar replaces
                   the older one.
  Skip             nothing installed; ledger row `skipped`; jarstamp.py pending keeps listing it
No display or no dialog tool, or no answer in 15 minutes: behaves as Skip and prints the reason.

Install = move every other mods/ jar matching <name>-*.jar or a --replaces glob to --displaced, copy the jar in.
Ledger: jar_archive/installed.csv (time, jar, stamp, displaced, displaced stamp, mode, note).

Usage: install_jar.py --jar BUILT --name JAR_NAME --displaced DIR [--replaces GLOB,...] [--skip-checks]
       [--no-dialog] [--game-pid PID]   (test hooks: no-dialog = treat a running game as Skip; game-pid = pretend)
"""
import argparse
import fnmatch
import os
import shutil
import signal
import subprocess
import sys
import time
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import jarstamp  # noqa: E402
import table_io  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
MODS = ROOT / 'mods'
ARCHIVE = ROOT / 'jar_archive'
LEDGER = ARCHIVE / 'installed.csv'
HOLD = ARCHIVE / 'pending_install'
LEDGER_HEAD = ['time', 'jar', 'stamp', 'displaced', 'displaced_stamp', 'mode', 'note']
DIALOG_TIMEOUT = 900


def log(msg):
	print('[install] ' + msg, flush=True)


def game_pid(fake=None):
	"""Pid of a java process running from the instance root, or None. Reads /proc/<pid>/comm and cwd only."""
	if fake is not None:
		return fake if fake > 0 and Path('/proc/%d' % fake).exists() else None
	logs = {str((ROOT / 'logs' / n).resolve()) for n in ('latest.log', 'debug.log')}
	for d in Path('/proc').iterdir():
		if not d.name.isdigit():
			continue
		try:
			if (d / 'comm').read_text().strip() != 'java':
				continue
			if Path(os.readlink(d / 'cwd')).resolve() != ROOT.resolve():
				continue
			# The game keeps logs/latest.log open; headless test JVMs (also run from the root) never do.
			for fd in (d / 'fd').iterdir():
				try:
					if os.readlink(fd) in logs:
						return int(d.name)
				except OSError:
					continue
		except OSError:
			continue
	return None


def ledger(jar, stamp, displaced, displaced_stamp, mode, note=''):
	ARCHIVE.mkdir(exist_ok=True)
	table_io.append(LEDGER, LEDGER_HEAD, [time.strftime('%Y-%m-%dT%H:%M:%S'), jar, stamp or 'unstamped',
										  displaced or '-', displaced_stamp or '-', mode, note])


def install(src, name, replaces, displaced_dir, mode, note=''):
	"""Swap src into mods/ (under its own file name, which may differ from src's for a held copy)."""
	target = MODS / src.name
	patterns = ['%s-*.jar' % name] + replaces
	moved = []
	for f in sorted(MODS.iterdir()):
		if f.is_file() and f.name != target.name and any(fnmatch.fnmatch(f.name, p) for p in patterns):
			moved.append((f.name, jarstamp.stamp_of(f)))
			displaced_dir.mkdir(parents=True, exist_ok=True)
			shutil.move(str(f), str(displaced_dir / f.name))
			log('moved mods/%s -> %s/' % (f.name, displaced_dir.relative_to(ROOT)))
	if target.exists():
		moved.append((target.name + ' (same name)', jarstamp.stamp_of(target)))
	shutil.copy2(src, target)
	stamp = jarstamp.stamp_of(target)
	log('installed mods/%s %s' % (target.name, stamp or 'unstamped'))
	ledger(target.name, stamp, ', '.join(n for n, _ in moved), ', '.join(s or 'unstamped' for _, s in moved), mode, note)
	return stamp


def ask(title, text):
	"""'now' | 'exit' | 'skip' | (None, reason)."""
	if not (os.environ.get('DISPLAY') or os.environ.get('WAYLAND_DISPLAY')):
		return None, 'no display'
	tool = shutil.which('zenity') or shutil.which('yad')
	if not tool:
		return None, 'no zenity or yad'
	if os.path.basename(tool) == 'zenity':
		cmd = [tool, '--question', '--title', title, '--text', text, '--ok-label', 'Install now',
			   '--cancel-label', 'Skip', '--extra-button', 'Install on exit', '--width', '420',
			   '--timeout', str(DIALOG_TIMEOUT)]
	else:
		cmd = [tool, '--title', title, '--text', text, '--button', 'Install now:0', '--button', 'Install on exit:2',
			   '--button', 'Skip:1', '--width', '420', '--timeout', str(DIALOG_TIMEOUT)]
	try:
		r = subprocess.run(cmd, capture_output=True, text=True, timeout=DIALOG_TIMEOUT + 30)
	except (OSError, subprocess.TimeoutExpired) as e:
		return None, 'dialog failed: %s' % e
	if r.returncode == 0:
		return 'now'
	if r.returncode == 5 or (os.path.basename(tool) == 'yad' and r.returncode == 70):
		return None, 'no answer in %d min' % (DIALOG_TIMEOUT // 60)
	if 'Install on exit' in r.stdout or (os.path.basename(tool) == 'yad' and r.returncode == 2):
		return 'exit'
	return 'skip'


def pid_file(name):
	return ARCHIVE / ('install_watch_%s.pid' % name)


def start_watcher(src, name, replaces, displaced_dir, pid, mode_note):
	HOLD.mkdir(parents=True, exist_ok=True)
	held = HOLD / src.name
	shutil.copy2(src, held)   # the confirmed build, not whatever build/libs holds at exit time
	pf = pid_file(name)
	if pf.exists():
		try:
			old = int(pf.read_text().split()[0])
			cmdline = Path('/proc/%d/cmdline' % old).read_bytes().decode(errors='replace')   # our own watcher
			if 'install_jar.py' in cmdline and '--watch' in cmdline:
				os.kill(old, signal.SIGTERM)
				log('replaced the older watcher (pid %d)' % old)
		except (OSError, ValueError):
			pass
	args = [sys.executable, str(Path(__file__).resolve()), '--watch', str(pid), '--jar', str(held), '--name', name,
			'--displaced', str(displaced_dir), '--replaces', ','.join(replaces), '--note', mode_note]
	p = subprocess.Popen(args, stdin=subprocess.DEVNULL, stdout=open(ARCHIVE / 'install_watch.log', 'a'),
						 stderr=subprocess.STDOUT, start_new_session=True, cwd=str(ROOT))
	pf.write_text('%d %s\n' % (p.pid, held.name))
	log('install on exit: watcher pid %d waits for the game (pid %d)' % (p.pid, pid))


def watch(a):
	pid = int(a.watch)
	while Path('/proc/%d' % pid).exists():
		time.sleep(3)
	time.sleep(2)
	src = Path(a.jar)
	stamp = install(src, a.name, split(a.replaces), Path(a.displaced), 'on-exit', a.note or '')
	try:
		src.unlink()
	except OSError:
		pass
	try:
		if pid_file(a.name).read_text().split()[0] == str(os.getpid()):
			pid_file(a.name).unlink()
	except OSError:
		pass
	if shutil.which('notify-send'):
		subprocess.run(['notify-send', 'Creative jar installed', '%s %s' % (src.name, stamp or '')])
	return 0


def split(s):
	return [x.strip() for x in (s or '').split(',') if x.strip()]


def main():
	ap = argparse.ArgumentParser()
	ap.add_argument('--jar', required=True)
	ap.add_argument('--name', required=True)
	ap.add_argument('--displaced', required=True)
	ap.add_argument('--replaces', default='')
	ap.add_argument('--skip-checks', action='store_true')
	ap.add_argument('--no-dialog', action='store_true')
	ap.add_argument('--game-pid', type=int)
	ap.add_argument('--watch')
	ap.add_argument('--note')
	a = ap.parse_args()
	if a.watch:
		return watch(a)
	src = Path(a.jar).resolve()
	replaces, displaced = split(a.replaces), Path(a.displaced)
	note = 'skipChecks' if a.skip_checks else ''
	stamp = jarstamp.stamp_of(src)
	pid = game_pid(a.game_pid)
	if pid is None:
		install(src, a.name, replaces, displaced, 'installed', note)
		return 0
	log('the game is running (pid %d)' % pid)
	if a.no_dialog:
		choice = (None, '--no-dialog')
	else:
		extra = ''
		while True:
			choice = ask('Install %s' % src.name, '%s<b>%s</b>\n%s\n\nThe game is running.' % (
				extra, src.name, stamp or 'unstamped'))
			if choice == 'now':
				pid = game_pid(a.game_pid)
				if pid is not None:
					extra = 'Game still running.\n\n'
					continue
				install(src, a.name, replaces, displaced, 'installed', note)
				return 0
			break
	if choice == 'exit':
		start_watcher(src, a.name, replaces, displaced, pid, note)
		return 0
	reason = choice[1] if isinstance(choice, tuple) else 'owner chose Skip'
	log('skipped: %s; mods/ unchanged (tools/jarstamp.py pending lists it)' % reason)
	ledger(src.name, stamp, '-', '-', 'skipped', ('%s; %s' % (note, reason)).strip('; '))
	return 0


if __name__ == '__main__':
	sys.exit(main())
