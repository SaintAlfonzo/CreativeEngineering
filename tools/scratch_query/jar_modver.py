#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Rewrite the version a scratch server's copy of a mod jar reports (SQ-CMV-03: a gated mod above its tested range).

    jar_modver.py JAR VERSION      (run in OUT/srv by `@@prep run`; JAR may be a glob matching exactly one jar)

Sets META-INF/MANIFEST.MF Implementation-Version (what a mods.toml `version="${file.jarVersion}"` reads) and a literal
mods.toml `version="..."` line to VERSION. The jar is written to a new file and renamed over the old path, so a hard
link into the pack's mods/ (qmodset.py / prep_srv.sh link jars) is broken, never written through. Refuses a path
outside the current directory, so it can not touch the pack's own jars.
"""
import glob
import os
import re
import sys
import zipfile


def main(argv):
	if len(argv) != 2:
		print(__doc__)
		return 2
	hits = glob.glob(argv[0])
	if len(hits) != 1:
		print('jar_modver: %r matches %d jars' % (argv[0], len(hits)), file=sys.stderr)
		return 1
	jar, ver = os.path.abspath(hits[0]), argv[1]
	if not jar.startswith(os.getcwd() + os.sep):
		print('jar_modver: %s is outside %s' % (jar, os.getcwd()), file=sys.stderr)
		return 1
	tmp = jar + '.modver'
	done = []
	with zipfile.ZipFile(jar) as src, zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as dst:
		for info in src.infolist():
			data = src.read(info.filename)
			if info.filename == 'META-INF/MANIFEST.MF':
				text, n = re.subn(rb'(?m)^Implementation-Version: .*?(\r?)$', b'Implementation-Version: ' + ver.encode()
								  + rb'\1', data)
				if n:
					data = text
					done.append('MANIFEST.MF Implementation-Version')
			elif info.filename == 'META-INF/mods.toml':
				text, n = re.subn(rb'(?m)^(\s*version\s*=\s*)"(?!\$\{)[^"]*"', rb'\1"' + ver.encode() + b'"', data, count=1)
				if n:
					data = text
					done.append('mods.toml version')
			dst.writestr(info, data)
	if not done:
		os.unlink(tmp)
		print('jar_modver: no version field in %s' % jar, file=sys.stderr)
		return 1
	os.replace(tmp, jar)
	print('jar_modver: %s -> %s (%s)' % (os.path.basename(jar), ver, ', '.join(done)))
	return 0


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
