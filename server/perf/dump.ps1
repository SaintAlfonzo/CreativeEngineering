# Saves the server's always-on performance recording to perf\recordings\ (see perf\README.md).
# Run through dump.bat:  perf\dump.bat  |  perf\dump.bat 10m  |  perf\dump.bat 10m lagspike
param([string]$Age = "", [string]$Tag = "", [int]$ServerPid = 0)

$root = Split-Path -Parent $PSScriptRoot
$procs = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
    Where-Object { $_.CommandLine -like '*StartFlightRecording=name=ce*' })
if ($ServerPid) { $procs = @($procs | Where-Object { $_.ProcessId -eq $ServerPid }) }
if ($procs.Count -eq 0) {
    Write-Host "No running server with the recording found."
    Write-Host "Is it running, and was it started with LaunchServer.bat (jvmargs has -XX:StartFlightRecording)?"
    exit 1
}
if ($procs.Count -gt 1) {
    Write-Host ("Several servers record: PIDs " + (($procs | ForEach-Object { $_.ProcessId }) -join ", "))
    Write-Host "Pick one: powershell -ExecutionPolicy Bypass -File perf\dump.ps1 -ServerPid <pid> [-Age 10m]"
    exit 1
}
$proc = $procs[0]

# jcmd from the same Java that runs the server; fall back to one on PATH.
$jcmd = Join-Path (Split-Path -Parent $proc.ExecutablePath) "jcmd.exe"
if (-not (Test-Path $jcmd)) {
    $found = Get-Command jcmd -ErrorAction SilentlyContinue
    if ($found) { $jcmd = $found.Source } else {
        Write-Host "No jcmd found (the server's Java is a JRE without tools). Without it, copy the finished chunk"
        Write-Host "files instead: perf\jfr-repo\<newest folder>\*.jfr - perf_report.py reads a folder of them."
        exit 1
    }
}

$dir = Join-Path $root "perf\recordings"
New-Item -ItemType Directory -Force -Path $dir | Out-Null
$name = "server-" + (Get-Date -Format "yyyyMMdd-HHmmss") + $(if ($Tag) { "-$Tag" } else { "" }) + ".jfr"
$out = Join-Path $dir $name
$jargs = @($proc.ProcessId, "JFR.dump", "name=ce", "filename=$out")
if ($Age) { $jargs += "maxage=$Age" }
& $jcmd @jargs
if ((Test-Path $out) -and ((Get-Item $out).Length -gt 0)) {
    Write-Host ("Saved {0:N0} MB to {1}" -f ((Get-Item $out).Length / 1MB), $out)
    Write-Host "Copy it to the pack's perf/recordings/ and run: python3 tools/perf/perf_report.py perf/recordings/$name"
} else {
    Write-Host "Dump failed (see above)."
    exit 1
}
