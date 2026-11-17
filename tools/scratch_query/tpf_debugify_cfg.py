#!/usr/bin/env python3
"""Writes a Debugify Reforged 1.1.0 config (config/debugify.json) with exactly the given fixes on (run r1113tpf,
feature_writeup/thirdparty_fixes.md).

    tpf_debugify_cfg.py OUT_JSON [ID ...]     every fix off except the ids given (MC-206922 or 206922)
    tpf_debugify_cfg.py OUT_JSON all          every fix on
    tpf_debugify_cfg.py OUT_JSON @SETNAME     a named set from SETS below (sets can be combined: @s1 @c1 MC-577)

default_disabled is true (a fix missing from the file stays off) and opt_out_updater is true (no update check).
gameplay_fixes_in_multiplayer stays false (upstream default). Note the upstream id typo: MC-124177 is registered as
MC-124117, so the key is MC-124117.
"""
import json
import sys

ALL = """MC-100991 MC-108948 MC-111516 MC-116379 MC-119417 MC-119754 MC-121706 MC-121772 MC-121903 MC-122477
MC-122627 MC-123739 MC-124117 MC-127970 MC-12829 MC-129909 MC-132878 MC-135971 MC-140646 MC-143474 MC-145748
MC-147605 MC-14923 MC-151412 MC-155509 MC-159163 MC-160095 MC-162253 MC-165381 MC-165595 MC-179072 MC-183776
MC-183990 MC-193343 MC-197260 MC-199467 MC-200418 MC-2025 MC-206922 MC-214147 MC-215530 MC-215531 MC-217716
MC-223153 MC-224729 MC-22882 MC-228976 MC-231097 MC-231743 MC-232869 MC-233042 MC-237493 MC-249059 MC-30391
MC-31819 MC-4490 MC-46766 MC-55347 MC-577 MC-69216 MC-72151 MC-79545 MC-80859 MC-84873 MC-88371 MC-89146
MC-90084 MC-93018 MC-93384""".split()

# Tranches of the r1113tpf run (thirdparty_fixes.md section 3 has the reasons per fix).
SETS = {
	# server rows on a dedicated server (SQ-TPF-10 to -19)
	"s1": "MC-206922 MC-2025 MC-84873 MC-200418 MC-214147 MC-232869 MC-223153 MC-121903 MC-72151 MC-224729",
	# client tranches in a singleplayer world (integrated server: client and server mixins both apply)
	"c1": "MC-31819 MC-93018 MC-155509 MC-231743 MC-124117 MC-135971 MC-14923 MC-12829 MC-237493",
	"c2": "MC-577 MC-143474 MC-231097 MC-80859 MC-147605 MC-140646 MC-249059 MC-165381 MC-183776 MC-79545 "
		  "MC-122627 MC-151412 MC-233042",
	"c3": "MC-4490 MC-116379 MC-127970 MC-108948 MC-197260 MC-93384 MC-165595 MC-90084",
	"c4": "MC-69216 MC-129909 MC-119754 MC-215530 MC-215531 MC-217716 MC-193343 MC-119417 "
		  "MC-179072 MC-46766",
	"c5": "MC-162253",
	# MC-122477 is only switched on by the comparison rows CQ-TPF-61 and -63 (thirdparty_fixes.md 4.1)
}


def norm(i):
	i = i.upper()
	return i if i.startswith("MC-") else "MC-" + i


def main():
	out, args = sys.argv[1], sys.argv[2:]
	on = set()
	for a in args:
		if a == "all":
			on |= set(ALL)
		elif a.startswith("@"):
			on |= set(SETS[a[1:]].split())
		else:
			on.add(norm(a))
	unknown = on - set(ALL)
	if unknown:
		sys.exit("unknown ids: " + " ".join(sorted(unknown)))
	cfg = {"opt_out_updater": True, "gameplay_fixes_in_multiplayer": False, "default_disabled": True}
	for i in sorted(ALL, key=lambda s: int(s[3:])):
		cfg[i] = i in on
	with open(out, "w") as f:
		json.dump(cfg, f, indent=2)
	print("debugify.json: %d of %d fixes on -> %s" % (len(on), len(ALL), out))


if __name__ == "__main__":
	main()
