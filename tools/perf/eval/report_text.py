#!/usr/bin/env python3
"""Plain text of a perf_report HTML page (tables as | rows), for grepping: report_text.py REPORT.html"""
import html
import re
import sys

s = open(sys.argv[1]).read()
s = re.sub(r"<script.*?</script>|<style.*?</style>|<svg.*?</svg>", "", s, flags=re.S)
s = re.sub(r"</(tr|h\d|p|li|div|caption)>", "\n", s)
s = re.sub(r"</t[dh]>", " | ", s)
s = html.unescape(re.sub(r"<[^>]+>", "", s))
print(re.sub(r"\n\s*\n+", "\n", s))
