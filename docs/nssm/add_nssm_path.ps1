$NssmPath = "C:\Dev\toolkits\nssm-2.24\win64"

if (-not (Test-Path "$NssmPath\nssm.exe")) {
    Write-Host "nssm.exe 를 찾을 수 없습니다: $NssmPath"
    exit
}

$CurrentPath = [Environment]::GetEnvironmentVariable("Path", "User")

if ($CurrentPath -like "*$NssmPath*") {
    Write-Host "이미 PATH에 등록되어 있습니다."
}
else {
    $NewPath = "$CurrentPath;$NssmPath"

    [Environment]::SetEnvironmentVariable(
        "Path",
        $NewPath,
        "User"
    )

    Write-Host "PATH 등록 완료."
}

Write-Host ""
Write-Host "새 PowerShell/cmd 창에서 확인:"
Write-Host "nssm"