param([switch]$BuildOnly, [switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

# Prefer an explicitly selected JDK; otherwise find an installed Java 21.
$candidates = @()
if ($env:JAVA_HOME) { $candidates += Join-Path $env:JAVA_HOME 'bin\java.exe' }
$candidates += @('C:\Program Files\Java\jdk-21.0.12\bin\java.exe',
    'C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot\bin\java.exe')
$onPath = Get-Command java.exe -ErrorAction SilentlyContinue
if ($onPath) { $candidates += $onPath.Source }
$javaCommand = $null
foreach ($candidate in $candidates) {
    if (Test-Path -LiteralPath $candidate) {
        $ErrorActionPreference = 'Continue'
        try { $version = (& $candidate -version 2>&1 | Out-String) }
        finally { $ErrorActionPreference = 'Stop' }
        if ($version -match 'version "21[.\"]') { $javaCommand = $candidate; break }
    }
}
if (!$javaCommand) { throw 'Java 21 is required. Set JAVA_HOME to your JDK 21 folder and retry.' }
Write-Host "Using $javaCommand"

$jars = @('javafx.base','javafx.controls','javafx.fxml','javafx.graphics',
    'javafx.media','javafx.swing','javafx.web','javafx-swt') |
    ForEach-Object { Join-Path $PSScriptRoot "lib\javafx\lib\$_.jar" }
$jars += Join-Path $PSScriptRoot 'lib\mysql-connector-java-8.0.25.jar'
foreach ($dependency in ($jars + (Join-Path $PSScriptRoot 'tools\ecj.jar'))) {
    if (!(Test-Path -LiteralPath $dependency)) { throw "Missing dependency: $dependency. Extract the entire ZIP first." }
}
$modulePath = $jars -join ';'
New-Item -ItemType Directory -Path 'build','bin\application' -Force | Out-Null
$sources = Get-ChildItem 'src' -Filter '*.java' -Recurse | ForEach-Object {
    '"' + $_.FullName.Replace('\','/') + '"'
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
[IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'build\sources.txt'), [string[]]$sources, $utf8)
$ErrorActionPreference = 'Continue'
try { & $javaCommand -jar 'tools\ecj.jar' -21 -proc:none -encoding UTF-8 --module-path $modulePath -d bin '@build/sources.txt' 2> 'build\compile.log' }
finally { $ErrorActionPreference = 'Stop' }
if ($LASTEXITCODE -ne 0) { Get-Content 'build\compile.log'; throw 'Compilation failed. See build\compile.log.' }
Get-ChildItem 'src\application' -File | Where-Object Extension -ne '.java' |
    Copy-Item -Destination 'bin\application' -Force
Write-Host 'Java 21 build passed. Compiler warnings, if any, are in build\compile.log.'
if ($Test) {
    foreach ($testName in @('CatchGeometryTest','LaunchDirectionTest','RunningAnimationTest','RunnerMotionTest','CatcherMovementTest','FielderAllocationTest','FieldOutcomeClassifierTest','GameModeTest','OfflineModeSmokeTest','BattingBalanceTest','GameplaySimulationTest','DefensiveTimingTest','ParkCameraTest','ParkPresentationTest',
        'CompetitiveFieldLayoutTest','OverheadFieldGeometryTest','RulesEditionValidationTest','MascotAtlasValidationTest','ResourceLoadingTest','PhysicalBallTest','LiveRunnerTest','FielderAllocationTest','GameIntegrationTest')) {
        & $javaCommand --module-path "bin;$modulePath" -m "final_homerun/application.$testName"
        if ($LASTEXITCODE -ne 0) { throw "Test failed: $testName" }
    }
    exit 0
}
if ($BuildOnly) { exit 0 }
& $javaCommand "-Djava.library.path=$PSScriptRoot\lib\javafx\bin" --module-path "bin;$modulePath" -m final_homerun/application.Main
exit $LASTEXITCODE

