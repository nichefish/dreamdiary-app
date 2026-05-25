# DreamDiary Mobile — Android 에뮬레이터에서 Expo 실행
# IntelliJ: Gradle > mobile > mobileAndroid 우클릭 Run
# 또는 run-android.bat / 이 ps1 실행

$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding $false

$mobileDir = $PSScriptRoot
$workspaceRoot = Resolve-Path (Join-Path $mobileDir "..\..")

function Find-GradleNode {
    $nodejsRoot = Join-Path $workspaceRoot ".gradle\nodejs"
    if (-not (Test-Path $nodejsRoot)) { return $null }
    $dirs = Get-ChildItem -Path $nodejsRoot -Directory -Filter "node-v*" -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending
    if (-not $dirs) { return $null }
    $nodeExe = Join-Path $dirs[0].FullName "node.exe"
    if (-not (Test-Path $nodeExe)) { return $null }
    $npmCli = Join-Path $dirs[0].FullName "node_modules\npm\bin\npm-cli.js"
    return @{ Node = $nodeExe; NpmCli = $npmCli }
}

function Invoke-Npm {
    param([string[]]$NpmArgs)
    $gradle = Find-GradleNode
    if (Get-Command npm -ErrorAction SilentlyContinue) {
        & npm @NpmArgs
        return
    }
    if ($null -eq $gradle) {
        throw "npm not found. Node 20.19+ PATH 등록 또는 루트에서: .\gradlew.bat npmSetup"
    }
    & $gradle.Node $gradle.NpmCli @NpmArgs
}

$envFile = Join-Path $mobileDir ".env"
$envExample = Join-Path $mobileDir ".env.example"
if (-not (Test-Path $envFile)) {
    if (-not (Test-Path $envExample)) { throw ".env.example not found" }
    $defaultEnv = "EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080`r`n"
    [System.IO.File]::WriteAllText($envFile, $defaultEnv, $utf8)
    Write-Host "[mobile] Created .env for Android emulator (10.0.2.2:8080)"
} else {
    $envText = [System.IO.File]::ReadAllText($envFile, $utf8)
    if ($envText -notmatch "10\.0\.2\.2") {
        Write-Host "[mobile] WARN: .env may not target emulator. Android AVD expects http://10.0.2.2:8080"
    }
}

Set-Location $mobileDir
if (-not (Test-Path "node_modules")) {
    Write-Host "[mobile] npm install ..."
    Invoke-Npm @("install")
}

Write-Host "[mobile] Starting Expo on Android (AVD 실행 + backend localhost:8080 확인) ..."
Invoke-Npm @("run", "android")