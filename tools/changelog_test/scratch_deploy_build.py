#!/usr/bin/env python3
"""
Scratch run of tools/build_deploy.py (queue SQ-OM-17): the same main(), with every output (OUT, HISTORY, LOG,
DEPLOYED) pointed into a scratch folder and the in-tree jar archive step disabled, so nothing in deploy/ or
development/ changes. DEPLOYED.json and history/ are copied in first, so the default mode is the same as a real build.

    python3 tools/changelog_test/scratch_deploy_build.py SCRATCH [build_deploy args...]
"""
import os
import shutil
import sys

TOOLS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, TOOLS)
import build_deploy as bd  # noqa: E402

s = os.path.abspath(sys.argv[1])
os.makedirs(s, exist_ok=True)
if os.path.isdir(bd.HISTORY):
    shutil.copytree(bd.HISTORY, os.path.join(s, 'history'), dirs_exist_ok=True)
if os.path.isfile(bd.DEPLOYED):
    shutil.copy2(bd.DEPLOYED, os.path.join(s, 'DEPLOYED.json'))
if os.path.isfile(bd.LOG):
    shutil.copy2(bd.LOG, os.path.join(s, 'DEPLOY_LOG.md'))
bd.OUT, bd.HISTORY = os.path.join(s, 'latest'), os.path.join(s, 'history')
bd.LOG, bd.DEPLOYED = os.path.join(s, 'DEPLOY_LOG.md'), os.path.join(s, 'DEPLOYED.json')
bd.archive_in_tree_mods = lambda files: None
sys.argv = [sys.argv[0]] + sys.argv[2:]
bd.main()
