FROM openliberty/open-liberty:22.0.0.6-full-java11-openj9-ubi
EXPOSE 5005
ADD server.xml /config/server.xml
RUN configure.sh

COPY content.txt /opt/content.txt