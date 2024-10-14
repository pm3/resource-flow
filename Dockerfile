FROM openjdk:21-jdk-slim as build

WORKDIR /app

RUN jlink --add-modules java.base,java.sql,java.naming,java.xml,java.management,java.desktop,java.instrument,java.security.jgss,jdk.crypto.ec,jdk.localedata,java.rmi,java.net.http,jdk.zipfs,jdk.unsupported --include-locales en,sk-SK  --output jre

FROM debian:buster-slim

RUN apt-get update && apt-get install -y curl tzdata git openssh-client apt-transport-https ca-certificates gnupg lsb-release \
 && curl -sL https://aka.ms/InstallAzureCLIDeb | bash \
 && apt-get update && apt-get install azure-cli \
 && rm -rf /var/cache /var/lib/apt /var/lib/dpkg /var/log/apt \
 && curl -L -o /usr/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl \
 && chmod 755 /usr/bin/kubectl

COPY --from=build /app/jre /usr/jre/

COPY target/resource-flow-*.jar /app/app.jar

WORKDIR /app
ENV LANG=C.UTF-8 TZ=Europe/Bratislava JAVA_HOME=/usr/jre PATH=$PATH:/usr/jre/bin
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
