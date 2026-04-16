param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs = @("test")
)

$candidates = @(
    $env:FINTRACK_JAVA_HOME,
    $env:JAVA21_HOME,
    "C:\Program Files\Eclipse Adoptium\jdk-21",
    "C:\Program Files\Java\jdk-21",
    "C:\Users\Dell\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64",
    "C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot",
    $env:JAVA_HOME
) | Where-Object { $_ -and (Test-Path (Join-Path $_ "bin\java.exe")) }

if (-not $candidates) {
    Write-Error "Java 21+ not found. Set FINTRACK_JAVA_HOME, JAVA21_HOME or JAVA_HOME to a JDK 21+ installation."
    exit 1
}

$javaHome = $candidates[0]
$env:JAVA_HOME = $javaHome
$env:Path = "$javaHome\bin;$env:Path"

Write-Host "Using JAVA_HOME=$javaHome"

$repoRoot = Split-Path -Parent $PSScriptRoot
$wrapper = Join-Path $repoRoot ".mvn\wrapper\maven-wrapper.jar"

if (-not (Test-Path $wrapper)) {
    Write-Error "Maven wrapper jar not found at $wrapper"
    exit 1
}

& java "-Dmaven.multiModuleProjectDirectory=$repoRoot" -classpath $wrapper org.apache.maven.wrapper.MavenWrapperMain @MavenArgs
exit $LASTEXITCODE
