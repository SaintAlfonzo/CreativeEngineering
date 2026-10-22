#!/usr/bin/env python3
"""
Lint for the player changelog files (feature_writeup/changelog.md 3.2, 3.5), plus the stamping helpers
tools/build_deploy.py uses to ship the working list as a version file.

Folder: kubejs/assets/creativeengineering/changelog/
  unreleased.md   the working list: first line `# Unreleased` (optionally `# Unreleased - Subtitle`), no date line
  <version>.md    first line `# <version>` (optionally ` - Subtitle`), second line `Released yyyy-mm-dd`

Errors (stop the deploy build):
  - file name not `unreleased` or a dotted version; two files with the same numeric version (1.1.01 and 1.1.1)
  - title / date lines missing or wrong; a date line in unreleased.md
  - a `##` family with no bullets, or used twice in one file; `###` or deeper headings
  - a bullet before the first `##`; a top-level bullet without a New / Changed / Fixed / Removed label
  - nesting deeper than one sub-bullet level (sub-bullets are `  - ` exactly); `*` or `+` bullets; numbered lists
  - icon comments that don't name a well-formed `namespace:id`; any other HTML
  - tables, images, code blocks, quotes, `---` right under a text line (that makes a heading)
  - emoji; dev words: patcherMod, KubeJS, mixin, CEConfig, .js / .java / .json, TODO, test ids (AB-12),
    TODO ids (family/sub #3), registry ids (namespace:id) outside icon comments
Warnings: a bullet (with its continuation lines) over 300 characters; an intro over three sentences.

Run: python3 tools/changelog_lint.py [FILE_OR_FOLDER ...]   (default: the changelog folder)
     python3 tools/changelog_lint.py --self-test
Exit code 1 when there are errors.
"""
import datetime
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FOLDER = os.path.join(ROOT, 'kubejs', 'assets', 'creativeengineering', 'changelog')
KUBEJS_PREFIX = 'assets/creativeengineering/changelog/'   # path inside kubejs/ (build_deploy's file keys)
UNRELEASED = 'unreleased'

VERSION = re.compile(r'\d+(\.\d+)*')
LABELS = ('New', 'Changed', 'Fixed', 'Removed')
ICON = re.compile(r'\s*<!--\s*icon:\s*(\S+)\s*-->\s*$')
ICON_ID = re.compile(r'[a-z0-9_.-]+:[a-z0-9_./-]+')
BULLET = re.compile(r'^( *)([-*+]) (.*)$')
DATE_LINE = re.compile(r'Released (\d{4}-\d{2}-\d{2})')
DEV_WORDS = [
    (re.compile(r'patcherMod', re.I), 'patcherMod'),
    (re.compile(r'KubeJS', re.I), 'KubeJS'),
    (re.compile(r'\bmixins?\b', re.I), 'mixin'),
    (re.compile(r'CEConfig'), 'CEConfig'),
    (re.compile(r'\.js\b'), '.js file'),
    (re.compile(r'\.java\b'), '.java file'),
    (re.compile(r'\.json\b'), '.json file'),
    (re.compile(r'\bTODO\b'), 'TODO'),
    (re.compile(r'\b[A-Z]{2}-\d{2,3}\b'), 'test id'),
    (re.compile(r'\b\w+/\w+ #\d+'), 'TODO id'),
    (re.compile(r'(?<![\w/.-])[a-z][a-z0-9_.-]*:[a-z0-9_][a-z0-9_./-]*'), 'registry id'),
]
EMOJI = re.compile('[\U0001F000-\U0001FAFF☀-➿⬀-⯿️‍]')


def version_key(v):
    return tuple(int(p) for p in v.split('.'))


def same_version(a, b):
    x, y = version_key(a), version_key(b)
    n = max(len(x), len(y))
    return x + (0,) * (n - len(x)) == y + (0,) * (n - len(y))


def has_bullets(text):
    return bool(re.search(r'(?m)^\s*[-*+]\s+\S', text))


def count_bullets(text):
    return len(re.findall(r'(?m)^- ', text))


def title_parts(line):
    """('Unreleased' or a version, subtitle or None) of a `# ...` title line, or None."""
    m = re.fullmatch(r'# (\S+)(?: - (.+))?', line.rstrip())
    return (m.group(1), m.group(2)) if m else None


def stamp(text, version, date):
    """The working list as `<version>.md`: title `# <version>` (keeping a subtitle), `Released <date>` under it."""
    lines = text.splitlines()
    sub = None
    if lines and lines[0].startswith('# '):
        parts = title_parts(lines[0])
        sub = parts[1] if parts else None
        lines = lines[1:]
    while lines and not lines[0].strip():
        lines = lines[1:]
    head = f'# {version}' + (f' - {sub}' if sub else '')
    return '\n'.join([head, f'Released {date}', ''] + lines).rstrip('\n') + '\n'


def strip_inline(text):
    """Text with link targets, inline code and icon comments taken out (for the dev-word checks)."""
    text = ICON.sub('', text)
    text = re.sub(r'\]\([^)]*\)', ']', text)
    text = re.sub(r'https?://\S+', '', text)
    return text


def lint_text(name, text):
    """(errors, warnings) for one file's text; name is the file name without .md."""
    errors, warnings = [], []
    err = lambda n, msg: errors.append(f'line {n}: {msg}')
    warn = lambda n, msg: warnings.append(f'line {n}: {msg}')
    lines = text.splitlines()
    unreleased = name == UNRELEASED
    if not unreleased and not VERSION.fullmatch(name):
        errors.append('file name must be "unreleased" or a version like 1.1.01')
        return errors, warnings
    if not lines:
        errors.append('empty file')
        return errors, warnings

    parts = title_parts(lines[0])
    want = 'Unreleased' if unreleased else name
    if not parts or parts[0] != want:
        err(1, f'first line must be "# {want}" (optionally "# {want} - Subtitle")')
    body_start = 1
    if unreleased:
        if len(lines) > 1 and lines[1].startswith('Released'):
            err(2, 'unreleased.md has no date line (the deploy build stamps it)')
    else:
        m = DATE_LINE.fullmatch(lines[1].rstrip()) if len(lines) > 1 else None
        if not m:
            err(2, 'second line must be "Released yyyy-mm-dd"')
        else:
            try:
                datetime.date.fromisoformat(m.group(1))
            except ValueError:
                err(2, f'not a date: {m.group(1)}')
        body_start = 2

    families = {}            # heading text -> bullet count
    family = None
    intro = []
    bullet = None            # (line no, text) of the bullet being read, for the length warning
    prev_blank = True
    in_list = False

    def end_bullet():
        nonlocal bullet
        if bullet and len(bullet[1]) > 300:
            warn(bullet[0], f'bullet is {len(bullet[1])} characters (over 300: split it or use a sub-bullet)')
        bullet = None

    for i, raw in enumerate(lines[body_start:], start=body_start + 1):
        line = raw.rstrip()
        stripped = line.strip()
        if not stripped:
            prev_blank = True
            continue
        plain = strip_inline(line)
        for rx, what in DEV_WORDS:
            hit = rx.search(plain)
            if hit:
                err(i, f'{what} "{hit.group(0)}" (write for players: in-game names only)')
        if EMOJI.search(line):
            err(i, 'emoji')
        if '![' in line:
            err(i, 'images are not supported')
        if re.search(r'<(?!!--\s*icon:)[!/a-zA-Z]', ICON.sub('', line)):
            err(i, 'HTML is not supported (only an icon comment at the end of a ## heading)')
        if stripped.startswith('|'):
            err(i, 'tables are not supported')
        if stripped.startswith(('```', '~~~')):
            err(i, 'code blocks are not supported (use `backticks` inline)')
        if stripped.startswith('>'):
            err(i, 'quotes are not supported')
        if re.match(r'\s*\d+[.)] ', line):
            err(i, 'numbered lists are not supported (use "- ")')

        if line.startswith('#'):
            end_bullet()
            in_list = False
            level = len(line) - len(line.lstrip('#'))
            if level == 1:
                err(i, 'only the first line is a "# " title')
            elif level > 2:
                err(i, 'only "## " family headings are supported (no ### or deeper)')
            else:
                icon = ICON.search(line)
                if icon and not ICON_ID.fullmatch(icon.group(1)):
                    err(i, f'icon "{icon.group(1)}" is not a namespace:id')
                if '<!--' in line and not icon:
                    err(i, 'icon comment must be "<!-- icon: namespace:id -->" at the end of the heading')
                family = ICON.sub('', line)[3:].strip()
                if not family:
                    err(i, 'empty family heading')
                elif family in families:
                    err(i, f'family "{family}" is used twice in this file')
                families.setdefault(family, 0)
            prev_blank = False
            continue

        if stripped == '---':
            end_bullet()
            in_list = False
            if not prev_blank:
                err(i, 'put a blank line before "---" (right under text it makes a heading)')
            prev_blank = False
            continue

        b = BULLET.match(line)
        if b:
            indent, marker, rest = len(b.group(1)), b.group(2), b.group(3)
            if marker != '-':
                err(i, f'bullets use "- ", not "{marker} "')
            if family is None:
                err(i, 'bullet before the first "## " family heading')
            else:
                families[family] += 1
            if indent == 0:
                end_bullet()
                if not re.match(r'\*\*(%s):\*\* \S' % '|'.join(LABELS), rest):
                    err(i, 'a bullet starts with **New:**, **Changed:**, **Fixed:** or **Removed:** and then text')
                bullet = (i, rest)
                in_list = True
            elif indent == 2:
                if not in_list:
                    err(i, 'sub-bullet with no bullet above it')
                if bullet:
                    bullet = (bullet[0], bullet[1] + ' ' + rest)
            else:
                err(i, 'one sub-bullet level only, indented exactly two spaces ("  - ")')
            prev_blank = False
            continue

        if in_list and line.startswith('  ') and not prev_blank:
            # continuation of the bullet above
            if bullet:
                bullet = (bullet[0], bullet[1] + ' ' + stripped)
            prev_blank = False
            continue
        if line.startswith('    '):
            err(i, 'indented text (4+ spaces) is a code block; continue a bullet with two spaces')
        end_bullet()
        in_list = False
        if family is None:
            intro.append(stripped)
        prev_blank = False
    end_bullet()

    for fam, n in families.items():
        if n == 0:
            errors.append(f'family "{fam}" has no bullets')
    sentences = len(re.findall(r'[.!?](\s|$)', ' '.join(intro)))
    if sentences > 3:
        warnings.append(f'intro is {sentences} sentences (keep it to 1 to 3)')
    return errors, warnings


def lint_paths(paths):
    """{file: (errors, warnings)} for the given files and folders, plus folder-wide checks under '(folder)'."""
    files = []
    for p in paths:
        if os.path.isdir(p):
            files += [os.path.join(p, f) for f in sorted(os.listdir(p)) if f.endswith('.md')]
        else:
            files.append(p)
    out = {}
    versions = []
    for path in files:
        name = os.path.basename(path)[:-3]
        with open(path, encoding='utf8') as f:
            out[path] = lint_text(name, f.read())
        if VERSION.fullmatch(name):
            versions.append(name)
    clashes = [f'{a}.md and {b}.md are the same version' for i, a in enumerate(versions)
               for b in versions[i + 1:] if same_version(a, b)]
    if clashes:
        out['(folder)'] = (clashes, [])
    return out


def self_test():
    good = '\n'.join([
        '# 1.1.01 - The Brewing Update', 'Released 2026-10-25', '',
        'A big one for brewers.', '',
        '## Brewing & Drinks <!-- icon: farmersdelight:cooking_pot -->', '',
        '- **Changed:** Drinks fill half a hunger point and make you *queasy*.',
        '  Look for `Intoxicating Drink` in the tooltip.',
        '- **New:** Wine ages faster in a cellar.',
        '  - Warm rooms slow it down a little.', '',
        '## Interface & Tooltips', '',
        '- **Fixed:** Raw names like `item.foo.bar` are gone. See the [wiki](https://example.org/a:b).', '',
        '---', ''])
    cases = [
        ('good file', '1.1.01', good, 0),
        ('working list', UNRELEASED, '# Unreleased\n', 0),
        ('working list with a bullet', UNRELEASED, '# Unreleased\n\n## Gameplay\n\n- **New:** A thing.\n', 0),
        ('wrong title', '1.1.01', good.replace('# 1.1.01', '# 1.1.02', 1), 1),
        ('no date', '1.1.01', good.replace('Released 2026-10-25', 'Out now'), 1),
        ('date in working list', UNRELEASED, '# Unreleased\nReleased 2026-10-25\n', 1),
        ('no label', '1.1.01', good.replace('**New:** Wine', 'Wine'), 1),
        ('deep nesting', '1.1.01', good.replace('  - Warm', '    - Warm'), 1),
        ('star bullet', '1.1.01', good.replace('- **New:** Wine', '* **New:** Wine'), 1),
        ('### heading', '1.1.01', good.replace('## Interface', '### Interface'), 1),
        ('empty family', '1.1.01', good.replace('## Interface & Tooltips', '## Interface & Tooltips\n\n## Empty'), 1),
        ('duplicate family', '1.1.01', good.replace('## Interface & Tooltips', '## Brewing & Drinks'), 1),
        ('bad icon', '1.1.01', good.replace('farmersdelight:cooking_pot', 'Cooking Pot'), 1),
        ('html', '1.1.01', good.replace('A big one', 'A <b>big</b> one'), 1),
        ('table', '1.1.01', good.replace('A big one for brewers.', '| a | b |'), 1),
        ('emoji', '1.1.01', good.replace('A big one', 'A big \U0001F37A one'), 1),
        ('dev word', '1.1.01', good.replace('Wine ages', 'The KubeJS script ages wine'), 1),
        ('registry id', '1.1.01', good.replace('Wine ages', 'vinery:red_wine ages'), 1),
        ('test id', '1.1.01', good.replace('Wine ages', 'Wine (AL-12) ages'), 1),
        ('setext rule', '1.1.01', good.replace('gone. See the [wiki](https://example.org/a:b).\n\n---',
                                               'gone.\n---'), 1),
        ('bullet before family', '1.1.01', good.replace('A big one for brewers.', '- **New:** Early.'), 1),
    ]
    failed = 0
    for what, name, text, want in cases:
        errors, _ = lint_text(name, text)
        ok = (len(errors) == 0) if want == 0 else (len(errors) >= 1)
        print(f"  {'ok  ' if ok else 'FAIL'} {what}" + ('' if ok else f': {errors}'))
        failed += not ok
    long_bullet = good.replace('Wine ages faster in a cellar.', 'Wine ages. ' * 40)
    _, warnings = lint_text('1.1.01', long_bullet)
    ok = any('characters' in w for w in warnings)
    print(f"  {'ok  ' if ok else 'FAIL'} long bullet warns")
    failed += not ok
    clash = [same_version('1.1.01', '1.1.1'), not same_version('1.0.10', '1.0.1')]
    ok = all(clash)
    print(f"  {'ok  ' if ok else 'FAIL'} 1.1.01 clashes with 1.1.1, 1.0.10 not with 1.0.1")
    failed += not ok
    stamped = stamp('# Unreleased - The Brewing Update\n\n## Gameplay\n\n- **New:** A thing.\n', '1.1.01', '2026-10-25')
    ok = stamped.startswith('# 1.1.01 - The Brewing Update\nReleased 2026-10-25\n\n## Gameplay') and not lint_text('1.1.01', stamped)[0]
    print(f"  {'ok  ' if ok else 'FAIL'} stamp keeps the subtitle and lints clean")
    failed += not ok
    print('self-test: ' + ('all passed' if not failed else f'{failed} FAILED'))
    return failed == 0


def main():
    args = sys.argv[1:]
    if args == ['--self-test']:
        sys.exit(0 if self_test() else 1)
    paths = args or [FOLDER]
    if not args and not os.path.isdir(FOLDER):
        print(f'no changelog folder ({os.path.relpath(FOLDER, ROOT)})')
        return
    results = lint_paths(paths)
    n_err = n_warn = 0
    for path, (errors, warnings) in results.items():
        label = path if path.startswith('(') else os.path.relpath(path, ROOT)
        for e in errors:
            print(f'ERROR {label}: {e}')
        for w in warnings:
            print(f'WARNING {label}: {w}')
        n_err += len(errors)
        n_warn += len(warnings)
    files = sum(1 for p in results if not p.startswith('('))
    print(f'changelog lint: {files} file(s), {n_err} error(s), {n_warn} warning(s)')
    sys.exit(1 if n_err else 0)


if __name__ == '__main__':
    main()
