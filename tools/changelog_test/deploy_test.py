#!/usr/bin/env python3
"""
Offline check of the changelog steps in tools/build_deploy.py (feature_writeup/changelog.md 3.5, 6), in a scratch
folder: staging stamps the working list as <version>.md without touching the tree, --snapshot freezes it into the
tree and starts a fresh working list, deployed files are frozen. Run by tools/test_changelog.sh.
"""
import datetime
import json
import os
import shutil
import sys
import tempfile

TOOLS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, TOOLS)
import build_deploy as bd  # noqa: E402

failures = 0


def check(ok, what):
    global failures
    print(('  ok   ' if ok else '  FAIL ') + what)
    failures += not ok


def main():
    s = tempfile.mkdtemp(prefix='ce-changelog-test-')
    try:
        run(s)
    finally:
        shutil.rmtree(s, True)
    print('all passed' if not failures else f'{failures} FAILED')
    sys.exit(1 if failures else 0)


def run(s):
    folder = os.path.join(s, 'kubejs', 'assets', 'creativecommon', 'changelog')
    os.makedirs(folder)
    for d in ('history', 'latest'):
        os.makedirs(os.path.join(s, 'deploy', d))
    bd.ROOT, bd.CHANGELOG_DIR = s, folder
    bd.HISTORY, bd.OUT = os.path.join(s, 'deploy', 'history'), os.path.join(s, 'deploy', 'latest')
    stage = os.path.join(s, 'stage')
    os.makedirs(stage)
    unreleased = os.path.join(folder, 'unreleased.md')
    working = '# Unreleased - Test\n\n## Gameplay\n\n- **New:** A thing to try.\n'
    open(unreleased, 'w').write(working)
    target = bd.changelog_path('1.1.01')
    tree_file = os.path.join(s, 'kubejs', target)

    pre, files = bd.Preflight(), {'kubejs': {}}
    bd.check_changelog(pre, files, '1.1.01', {}, stage)
    staged = files['kubejs'].get(target)
    check(not pre.errors and staged is not None, f'staging adds {target} ({pre.errors})')
    text = open(staged).read()
    today = datetime.date.today().isoformat()
    check(text.startswith(f'# 1.1.01 - Test\nReleased {today}\n\n## Gameplay'), 'stamped title + date')
    check(open(unreleased).read() == working and not os.path.exists(tree_file), 'tree untouched by staging')

    # stage: history json + payload copy, as main() writes them
    json.dump({'version': '1.1.01', 'date': today + 'T10:00:00', 'files': {'kubejs': {target: bd.sha1(staged)}}},
              open(bd.history_path('1.1.01'), 'w'))
    payload = os.path.join(bd.OUT, 'kubejs', target)
    os.makedirs(os.path.dirname(payload))
    shutil.copy2(staged, payload)

    bd.freeze_changelog('1.1.01')
    check(os.path.isfile(tree_file) and bd.sha1(tree_file) == bd.sha1(staged), 'snapshot writes the shipped text into the tree')
    check(open(unreleased).read() == '# Unreleased\n', 'snapshot starts a fresh working list')
    bd.freeze_changelog('1.1.01')
    check(bd.sha1(tree_file) == bd.sha1(staged), 'second snapshot (other target) changes nothing')

    deployed = {'log': [{'version': '1.1.01'}]}
    pre, files = bd.Preflight(), {'kubejs': {}}
    bd.check_changelog(pre, files, '1.1.02', deployed, stage)
    check(not pre.errors and any('no bullets' in w for w in pre.warnings), f'next version: no errors, no-bullets warning ({pre.errors})')

    open(tree_file, 'a').write('- **Fixed:** sneaky edit\n')
    pre = bd.Preflight()
    bd.check_changelog(pre, {'kubejs': {}}, '1.1.02', deployed, stage)
    check(any('changed after it shipped' in e for e in pre.errors), 'editing a deployed file is an error')
    os.remove(tree_file)
    pre = bd.Preflight()
    bd.check_changelog(pre, {'kubejs': {}}, '1.1.02', deployed, stage)
    check(any('is gone' in e for e in pre.errors), 'deleting a deployed file is an error')

    shutil.copy2(staged, tree_file)
    pre = bd.Preflight()
    bd.check_changelog(pre, {'kubejs': {}}, '1.1.01', {}, stage)
    check(any('already exists' in e for e in pre.errors), 'a tree file for the version being built is an error')

    # snapshot with deploy/latest gone: rebuilt from unreleased.md + the history date
    os.remove(tree_file)
    open(unreleased, 'w').write(working)
    os.remove(payload)
    bd.freeze_changelog('1.1.01')
    check(os.path.isfile(tree_file) and bd.sha1(tree_file) == bd.sha1(staged), 'snapshot without the payload rebuilds the same text')

    # working list edited after staging: tree file from the payload copy, working list left alone
    os.remove(tree_file)
    shutil.copy2(staged, payload)
    edited = working + '- **New:** Another thing, added after staging.\n'
    open(unreleased, 'w').write(edited)
    bd.freeze_changelog('1.1.01')
    check(bd.sha1(tree_file) == bd.sha1(staged) and open(unreleased).read() == edited,
          'working list edited after staging: shipped text written, working list kept')


if __name__ == '__main__':
    main()
