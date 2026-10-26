#!/usr/bin/env bash
# Prints "pid cwd" for every java process that is not Gradle (game clients and servers). Reads only
# /proc/PID/cmdline through grep -q, so no argument (access token) is ever printed.
for p in $(pgrep -x java); do
  tr '\0' ' ' < /proc/$p/cmdline 2>/dev/null | grep -qiE 'gradle|kotlin-daemon' && continue
  echo "$p $(readlink /proc/$p/cwd)"
done
