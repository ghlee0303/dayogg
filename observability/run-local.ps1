<#
.SYNOPSIS
    로컬 로그 뷰어(Loki + Alloy + Grafana)를 네이티브로 내려받아 실행한다.

.DESCRIPTION
    앱은 IntelliJ 에서 local 프로파일로 실행하고, 이 스크립트가 뷰어 3종을 띄운다.
    Docker 를 쓰지 않는 구성이다.

        IntelliJ (앱)  ->  logs/app.json  ->  Alloy  ->  Loki  ->  Grafana

    자세한 건 docs/logging.md 참고.

.EXAMPLE
    .\run-local.ps1 install    # 최초 1회, bin\ 에 바이너리 3종 내려받기
    .\run-local.ps1 start      # 백그라운드로 기동 -> http://localhost:3000
    .\run-local.ps1 status
    .\run-local.ps1 stop
#>
[CmdletBinding()]
param(
    [ValidateSet('install', 'start', 'stop', 'status')]
    [string]$Command = 'status'
)

$ErrorActionPreference = 'Stop'

# ── 버전 ─────────────────────────────────────────────────────────────
$LokiVersion    = 'v3.7.4'
$AlloyVersion   = 'v1.18.0'
$GrafanaVersion = '13.1.1'

$Root     = $PSScriptRoot
$BinDir   = Join-Path $Root 'bin'
$DataDir  = Join-Path $Root 'data'
$PidFile  = Join-Path $DataDir 'pids.json'
# 앱이 logs/app.json 을 떨군다 (application.yml 의 local 프로파일)
$AppLog   = Join-Path (Split-Path $Root -Parent) 'logs\app.json'

$LokiExe  = Join-Path $BinDir 'loki-windows-amd64.exe'
$AlloyExe = Join-Path $BinDir 'alloy-windows-amd64.exe'

function Get-GrafanaHome {
    if (-not (Test-Path $BinDir)) { return $null }
    $dir = Get-ChildItem -Path $BinDir -Directory -Filter 'grafana*' | Select-Object -First 1
    if ($null -eq $dir) { return $null }
    return $dir.FullName
}

function Expand-Remote($Url, $Dest) {
    $tmp = Join-Path $env:TEMP ([IO.Path]::GetRandomFileName() + '.zip')
    Write-Host "  받는 중: $Url"
    $old = $ProgressPreference
    $ProgressPreference = 'SilentlyContinue'   # 진행률 표시가 다운로드를 크게 느리게 만든다
    try {
        Invoke-WebRequest -Uri $Url -OutFile $tmp -UseBasicParsing
        Expand-Archive -Path $tmp -DestinationPath $Dest -Force
    }
    finally {
        $ProgressPreference = $old
        if (Test-Path $tmp) { Remove-Item $tmp -Force }
    }
}

function Invoke-Install {
    New-Item -ItemType Directory -Force -Path $BinDir  | Out-Null
    New-Item -ItemType Directory -Force -Path $DataDir | Out-Null

    Write-Host "Loki $LokiVersion"
    Expand-Remote "https://github.com/grafana/loki/releases/download/$LokiVersion/loki-windows-amd64.exe.zip" $BinDir

    Write-Host "Alloy $AlloyVersion"
    Expand-Remote "https://github.com/grafana/alloy/releases/download/$AlloyVersion/alloy-windows-amd64.exe.zip" $BinDir

    Write-Host "Grafana $GrafanaVersion"
    Expand-Remote "https://dl.grafana.com/oss/release/grafana-$GrafanaVersion.windows-amd64.zip" $BinDir

    Write-Host ""
    Write-Host "설치 완료 -> $BinDir"
    Write-Host "다음: .\run-local.ps1 start"
}

# pids.json 에 적힌 것 중 실제로 살아 있는 프로세스만 돌려준다.
# 재부팅하면 프로세스는 죽고 기록만 남으므로 파일 존재만으로 판단하면 안 된다.
# PID 는 재사용되므로 실행 파일 경로가 bin\ 아래인지까지 확인한다.
function Get-LiveProcess {
    if (-not (Test-Path $PidFile)) { return @() }
    $recorded = Get-Content $PidFile -Raw | ConvertFrom-Json
    $live = @()
    foreach ($p in $recorded.PSObject.Properties) {
        $proc = Get-Process -Id $p.Value -ErrorAction SilentlyContinue
        if ($null -eq $proc) { continue }
        if ($proc.Path -and $proc.Path.StartsWith($BinDir, [StringComparison]::OrdinalIgnoreCase)) {
            $live += [pscustomobject]@{ Name = $p.Name; Id = $p.Value }
        }
    }
    return $live
}

function Test-Installed {
    if (-not (Test-Path $LokiExe))  { return $false }
    if (-not (Test-Path $AlloyExe)) { return $false }
    if ($null -eq (Get-GrafanaHome)) { return $false }
    return $true
}

function Invoke-Start {
    if (-not (Test-Installed)) {
        Write-Host "바이너리가 없다. 먼저: .\run-local.ps1 install" -ForegroundColor Yellow
        return
    }
    if (Test-Path $PidFile) {
        $live = Get-LiveProcess
        if ($live.Count -gt 0) {
            Write-Host "이미 떠 있다: $(($live | ForEach-Object { $_.Name }) -join ', '). 다시 띄우려면 먼저 stop." -ForegroundColor Yellow
            return
        }
        # 재부팅 등으로 프로세스는 죽고 기록만 남은 경우 — 그냥 지우고 진행한다
        Remove-Item $PidFile -Force
    }

    $grafanaHome = Get-GrafanaHome
    New-Item -ItemType Directory -Force -Path $DataDir | Out-Null
    # 앱을 아직 안 띄웠어도 Alloy 가 붙을 디렉터리는 있어야 한다
    New-Item -ItemType Directory -Force -Path (Split-Path $AppLog -Parent) | Out-Null

    # Alloy 의 glob 은 역슬래시를 이스케이프로 해석한다. 슬래시로 바꿔 넘긴다.
    $env:APP_LOG_PATH               = $AppLog -replace '\\', '/'
    $env:GF_PATHS_PROVISIONING      = Join-Path $Root 'grafana-provisioning'
    $env:GF_AUTH_ANONYMOUS_ENABLED  = 'true'
    $env:GF_AUTH_ANONYMOUS_ORG_ROLE = 'Admin'
    $env:GF_AUTH_DISABLE_LOGIN_FORM = 'true'

    $procs = @{}

    $procs.loki = (Start-Process -FilePath $LokiExe `
        -ArgumentList '--config.file=loki-config.yaml' `
        -WorkingDirectory $Root -WindowStyle Hidden -PassThru).Id

    $procs.alloy = (Start-Process -FilePath $AlloyExe `
        -ArgumentList @('run', "--storage.path=$(Join-Path $DataDir 'alloy')",
                        '--server.http.listen-addr=127.0.0.1:12345', 'config.alloy') `
        -WorkingDirectory $Root -WindowStyle Hidden -PassThru).Id

    $procs.grafana = (Start-Process -FilePath (Join-Path $grafanaHome 'bin\grafana.exe') `
        -ArgumentList @('server', "--homepath=$grafanaHome") `
        -WorkingDirectory $grafanaHome -WindowStyle Hidden -PassThru).Id

    $procs | ConvertTo-Json | Set-Content -Path $PidFile -Encoding utf8

    Write-Host "기동 중... (loki=$($procs.loki) alloy=$($procs.alloy) grafana=$($procs.grafana))"
    Invoke-Status -WaitSeconds 30
}

function Invoke-Stop {
    if (-not (Test-Path $PidFile)) {
        Write-Host "떠 있는 프로세스 기록이 없다."
        return
    }
    $live = Get-LiveProcess
    if ($live.Count -eq 0) {
        Write-Host "살아 있는 프로세스 없음 (기록만 정리한다)."
    }
    foreach ($p in $live) {
        try {
            Stop-Process -Id $p.Id -Force -ErrorAction Stop
            Write-Host "  중지: $($p.Name) (pid $($p.Id))"
        }
        catch {
            Write-Host "  중지 실패: $($p.Name) — $($_.Exception.Message)"
        }
    }
    Remove-Item $PidFile -Force
}

function Test-Endpoint($Url) {
    try {
        Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3 | Out-Null
        return $true
    }
    catch { return $false }
}

function Invoke-Status {
    param([int]$WaitSeconds = 0)

    $checks = @(
        @{ Name = 'Loki';    Url = 'http://localhost:3100/ready' },
        @{ Name = 'Alloy';   Url = 'http://localhost:12345/-/ready' },
        @{ Name = 'Grafana'; Url = 'http://localhost:3000/api/health' }
    )

    $deadline = (Get-Date).AddSeconds($WaitSeconds)
    do {
        $results = @()
        foreach ($c in $checks) { $results += [pscustomobject]@{ Name = $c.Name; Up = (Test-Endpoint $c.Url) } }
        $allUp = -not ($results | Where-Object { -not $_.Up })
        if ($allUp -or (Get-Date) -ge $deadline) { break }
        Start-Sleep -Seconds 2
    } while ($true)

    Write-Host ""
    foreach ($r in $results) {
        if ($r.Up) { Write-Host ("  {0,-8} UP" -f $r.Name) -ForegroundColor Green }
        else       { Write-Host ("  {0,-8} DOWN" -f $r.Name) -ForegroundColor Red }
    }
    Write-Host ""
    if ($allUp) {
        Write-Host "Grafana -> http://localhost:3000  (Explore -> Loki)"
        Write-Host "질의    -> {job=`"statistics`"} | json | elapsedMs > 1000"
        Write-Host ""
        Write-Host "앱은 IntelliJ 에서 Active profiles=local 로 실행할 것. 안 그러면 $AppLog 가 안 생긴다." -ForegroundColor Yellow
    }
}

switch ($Command) {
    'install' { Invoke-Install }
    'start'   { Invoke-Start }
    'stop'    { Invoke-Stop }
    'status'  { Invoke-Status }
}
