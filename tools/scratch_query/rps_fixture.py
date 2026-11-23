#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Fixture jars for the SQ-RPS rows (plan_rp_standalone.md section 6), run in OUT/srv by `@@prep run`.

    rps_fixture.py table JAR NAME JSON     add solraspberrypie/diversity/NAME.json (text JSON) to JAR
    rps_fixture.py stub JAR [FIELD]        rename FIELD (default lastDuration) to FIELDStub in Alex's Mobs EffectFleetFooted

JAR may be a glob matching exactly one jar under the current directory. The jar is written to a new file and renamed
over the old path, so a hard link into the pack's mods/ is broken, never written through (same rule as jar_modver.py).
`table` serves SQ-RPS-07 and -10 (Raspberry Pie reads diversity tables from loaded mod files only, so the table rides
in a copy of a loaded mod jar). `stub` serves SQ-RPS-08: RpMixinPlugin checks the target ClassNode for the int field
`lastDuration` and applyEffectTick, so renaming the field's name constant (declaration and every reference share it)
makes the check fail while the class still loads and runs.
"""
import glob
import os
import struct
import sys
import zipfile

FLEET = 'com/github/alexthe666/alexsmobs/effect/EffectFleetFooted.class'


def one_jar(pattern):
	hits = glob.glob(pattern)
	if len(hits) != 1:
		sys.exit('rps_fixture: %r matches %d jars' % (pattern, len(hits)))
	jar = os.path.abspath(hits[0])
	if not jar.startswith(os.getcwd() + os.sep):
		sys.exit('rps_fixture: %s is outside %s' % (jar, os.getcwd()))
	return jar


def rewrite(jar, change):
	"""Copy JAR entry by entry through change(name, data) -> data or None (drop); extra entries from change.extra."""
	tmp = jar + '.tmp'
	with zipfile.ZipFile(jar) as src, zipfile.ZipFile(tmp, 'w', zipfile.ZIP_DEFLATED) as dst:
		for info in src.infolist():
			data = change(info.filename, src.read(info.filename))
			if data is not None:
				dst.writestr(info, data)
		for name, data in getattr(change, 'extra', {}).items():
			dst.writestr(name, data)
	os.replace(tmp, jar)


def rename_member(cls, field):
	"""Class file bytes with the CONSTANT_Utf8 FIELD renamed to FIELD + 'Stub'. The field declaration and every
	Fieldref to it share that constant, so the class still loads and runs (its constructor writes the field: dropping
	the declaration instead throws NoSuchFieldError at registry time, r1123rp), while a ClassNode check for FIELD fails."""
	out = bytearray(cls[:10])
	pos = 8
	(count,) = struct.unpack('>H', cls[pos:pos + 2])
	pos += 2
	i, hits = 1, 0
	while i < count:
		tag = cls[pos]
		if tag == 1:
			(n,) = struct.unpack('>H', cls[pos + 1:pos + 3])
			text = cls[pos + 3:pos + 3 + n]
			if text == field.encode():
				text = (field + 'Stub').encode()
				hits += 1
			out += bytes([1]) + struct.pack('>H', len(text)) + text
			pos += 3 + n
			i += 1
			continue
		if tag in (3, 4, 9, 10, 11, 12, 17, 18):
			size = 5
		elif tag in (5, 6):
			size = 9
		elif tag in (7, 8, 16, 19, 20):
			size = 3
		elif tag == 15:
			size = 4
		else:
			sys.exit('rps_fixture: unknown constant tag %d' % tag)
		out += cls[pos:pos + size]
		pos += size
		i += 2 if tag in (5, 6) else 1
	if not hits:
		sys.exit('rps_fixture: no constant %s in the class' % field)
	return bytes(out) + cls[pos:]


def main(argv):
	if len(argv) >= 4 and argv[0] == 'table':
		jar, name, text = one_jar(argv[1]), argv[2], argv[3]
		path = 'solraspberrypie/diversity/%s.json' % name
		def change(n, data):
			return None if n == path else data
		change.extra = {path: text}
		rewrite(jar, change)
		print('rps_fixture: %s added to %s' % (path, os.path.basename(jar)))
		return 0
	if len(argv) in (2, 3) and argv[0] == 'stub':
		jar = one_jar(argv[1])
		field = argv[2] if len(argv) == 3 else 'lastDuration'
		seen = []
		def change(n, data):
			if n == FLEET:
				seen.append(n)
				return rename_member(data, field)
			if n.startswith('META-INF/') and n.endswith(('.SF', '.RSA', '.DSA', '.EC')):
				return None  # a signed jar would refuse the changed class
			return data
		rewrite(jar, change)
		if not seen:
			sys.exit('rps_fixture: %s not in %s' % (FLEET, os.path.basename(jar)))
		print('rps_fixture: field %s renamed in EffectFleetFooted in %s' % (field, os.path.basename(jar)))
		return 0
	print(__doc__)
	return 2


if __name__ == '__main__':
	sys.exit(main(sys.argv[1:]))
