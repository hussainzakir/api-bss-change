# Dockerfile for api-bss
# docker build -t api-bss .

FROM stash.trinet-devops.com:8443/tomcat:8-jre8-slim
MAINTAINER eng.release@trinet.com

# copy our application in the image
COPY ./target/api-bss.war /usr/local/tomcat/webapps/api-bss.war

# Expose our port so clients can communicate to your app
EXPOSE 8080
