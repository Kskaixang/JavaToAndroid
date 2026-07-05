# 還原 Android 專案檔案的腳本
Copy-Item -Path "app/src/main/java/com/example/javatoandroid/MainActivity.java.bak" -Destination "app/src/main/java/com/example/javatoandroid/MainActivity.java" -Force
Copy-Item -Path "app/src/main/res/layout/activity_main.xml.bak" -Destination "app/src/main/res/layout/activity_main.xml" -Force

# 清除備份與模擬器檔案
Remove-Item -Path "app/src/main/java/com/example/javatoandroid/MainActivity.java.bak" -ErrorAction SilentlyContinue
Remove-Item -Path "app/src/main/res/layout/activity_main.xml.bak" -ErrorAction SilentlyContinue
Remove-Item -Path "android_simulator.html" -ErrorAction SilentlyContinue

Write-Host "專案已成功還原至初始狀態！" -ForegroundColor Green
# 刪除此還原腳本自身
Remove-Item -Path $MyInvocation.MyCommand.Path -ErrorAction SilentlyContinue
