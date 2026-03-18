# 基于 JRE 17 运行 JAR 包
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S spring && adduser -S spring -G spring

# 复制 JAR 包（构建时从本地复制）
COPY cms/*.jar app.jar

# 修改文件所有者
RUN chown spring:spring app.jar

USER spring:spring

# Spring Boot 默认端口
EXPOSE 11006

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar","--spring.cloud.nacos.discovery.server-addr=172.17.0.1:10002","--spring.cloud.nacos.config.server-addr=172.17.0.1:10002"]
