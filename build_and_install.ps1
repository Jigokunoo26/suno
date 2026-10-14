$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;C:\Users\edeep\AppData\Local\Android\Sdk\platform-tools;$env:PATH"

Write-Output "Building Debug APK with Gradle (fresh KSP generation)..."
& .\gradlew.bat assembleDebug --no-build-cache

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Output "APK built successfully at $apkPath"
    
    $devices = & adb devices | Select-String "device$"
    if ($devices) {
        Write-Output "Connected device found. Pushing Suno.apk to phone..."
        & adb push $apkPath /sdcard/Download/Suno.apk
        Write-Output "Suno.apk updated in phone Download folder!"
        Write-Output "Installing updated Suno APK on phone..."
        & adb install -r $apkPath
        Write-Output "Installation complete!"
    } else {
        Write-Output "No USB device currently attached. Connect your phone anytime to install."
    }
} else {
    Write-Error "APK build failed."
}
