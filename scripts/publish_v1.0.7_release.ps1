param(
    [string]$Owner = "Aetik-yue",
    [string]$Repo = "Vitamin",
    [string]$Version = "1.0.7"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

$tag = "v$Version"
$repoFullName = "$Owner/$Repo"
$apkPath = Join-Path $root "version/$Version/Vitamin$Version.apk"
$notesPath = Join-Path $root "version/$Version/CHANGELOG.md"

if (-not (Test-Path -LiteralPath $apkPath)) {
    throw "APK not found: $apkPath"
}
if (-not (Test-Path -LiteralPath $notesPath)) {
    throw "CHANGELOG not found: $notesPath"
}

gh auth status

$remoteNames = @(git remote)
if ($remoteNames -notcontains "origin") {
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    gh repo view $repoFullName *> $null
    $repoExists = ($LASTEXITCODE -eq 0)
    $ErrorActionPreference = $previousErrorActionPreference

    if ($repoExists) {
        git remote add origin "https://github.com/$repoFullName.git"
        git push -u origin (git branch --show-current)
        git push origin $tag
    } else {
        gh repo create $repoFullName --public --source . --remote origin --push
        git push origin $tag
    }
} else {
    git push -u origin (git branch --show-current)
    git push origin $tag
}

$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"
gh release view $tag --repo $repoFullName *> $null
$releaseExists = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = $previousErrorActionPreference

if ($releaseExists) {
    gh release upload $tag $apkPath --repo $repoFullName --clobber
} else {
    gh release create $tag $apkPath --repo $repoFullName --title "Vitamin $tag" --notes-file $notesPath
}

Write-Host "Published $tag to https://github.com/$repoFullName/releases/tag/$tag"


