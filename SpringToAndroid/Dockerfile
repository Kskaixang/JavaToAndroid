# 使用輕量級的 Java 21 執行環境作為基底
FROM eclipse-temurin:21-jre

# 設定容器內的工作目錄
WORKDIR /app

# 將本機編譯好的 JAR 檔複製到容器內，並重新命名為 app.jar
# (請確認 target/ 下的檔名是否為 SpringToAndroid-0.0.1-SNAPSHOT.jar)
COPY target/SpringToAndroid-0.0.1-SNAPSHOT.jar app.jar

# 設定容器系統時區為台北時間 (Asia/Taipei)，解決日本伺服器時區問題
ENV TZ=Asia/Taipei
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 宣告容器對外運行的連接埠 (Port)
EXPOSE 8080

# 啟動指令：執行 Java 程式
ENTRYPOINT ["java", "-jar", "app.jar"]