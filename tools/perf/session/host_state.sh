#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-only
# Host CPU and power state for perf sessions (collect.sh). The tick-time step of 2026-11-20 / 2026-11-21 (idle MSPT
# 1.2-2.5 ms -> 5-6 ms with no pack cause found) could not be traced because no boot recorded the machine state.
#   host_state.sh meta    KEY=VALUE lines for a boot's meta.env: HOST_BOOT (machine boot time), KERNEL, CPU_DRIVER,
#                         CPU_GOVERNOR, CPU_EPP, CPU_MAX_KHZ (scaling max), CPU_HW_MAX_KHZ, CPU_BOOST, PLATFORM_PROFILE,
#                         AC_ONLINE, HOST_LOAD1, HOST_TEMP_C
#   host_state.sh header  the host.tsv header line
#   host_state.sh row     one host.tsv row: time, MHz median and max over all cores, load1, hottest zone C, AC online
# Missing sysfs files print "-". Reads only /proc and /sys.
set -u
C=/sys/devices/system/cpu
rd() { cat "$1" 2>/dev/null || echo -; }
ac() {
  local p v=-
  for p in /sys/class/power_supply/*; do
    [ "$(cat "$p/type" 2>/dev/null)" = Mains ] && v=$(rd "$p/online")
  done
  echo "$v"
}
temp() {
  local z t m=-
  for z in /sys/class/thermal/thermal_zone*/temp; do
    t=$(cat "$z" 2>/dev/null) || continue
    t=$((t / 1000))
    if [ "$m" = - ] || [ "$t" -gt "$m" ]; then m=$t; fi
  done
  echo "$m"
}
mhz() {  # median and max scaling_cur_freq in MHz
  cat $C/cpu[0-9]*/cpufreq/scaling_cur_freq 2>/dev/null | sort -n |
    awk '{v[NR]=$1} END {if (NR == 0) {print "-\t-"; exit} printf "%d\t%d\n", v[int((NR + 1) / 2)] / 1000, v[NR] / 1000}'
}
case "${1:-}" in
meta)
  echo "HOST_BOOT=$(awk '/^btime/{print $2}' /proc/stat)"
  echo "KERNEL=$(uname -r)"
  echo "CPU_DRIVER=$(rd $C/cpu0/cpufreq/scaling_driver)"
  echo "CPU_GOVERNOR=$(rd $C/cpu0/cpufreq/scaling_governor)"
  echo "CPU_EPP=$(rd $C/cpu0/cpufreq/energy_performance_preference)"
  echo "CPU_MAX_KHZ=$(rd $C/cpu0/cpufreq/scaling_max_freq)"
  echo "CPU_HW_MAX_KHZ=$(rd $C/cpu0/cpufreq/cpuinfo_max_freq)"
  echo "CPU_BOOST=$(rd $C/cpufreq/boost)"
  echo "PLATFORM_PROFILE=$(rd /sys/firmware/acpi/platform_profile)"
  echo "AC_ONLINE=$(ac)"
  echo "HOST_LOAD1=$(cut -d' ' -f1 /proc/loadavg)"
  echo "HOST_TEMP_C=$(temp)"
  ;;
header) printf 'time\tmhz_med\tmhz_max\tload1\ttemp_c\tac\n' ;;
row) printf '%s\t%s\t%s\t%s\t%s\n' "$(date +%s)" "$(mhz)" "$(cut -d' ' -f1 /proc/loadavg)" "$(temp)" "$(ac)" ;;
*) echo "usage: host_state.sh meta|header|row" >&2; exit 2 ;;
esac
