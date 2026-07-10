Set-Location "$PSScriptRoot"
./gradlew jar --no-daemon
Copy-Item build/libs/tfc_alloy_ext-neoforge-0.0.1.jar ../../mods/
Write-Host "Done."
