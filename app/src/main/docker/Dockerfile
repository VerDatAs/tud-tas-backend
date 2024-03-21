#  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
#  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski)
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <http://www.gnu.org/licenses/>.

FROM eclipse-temurin:17.0.8_7-jre as builder
ARG JAR_FILE=./*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17.0.8_7-jre

COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./

RUN mkdir -p /var/log/tas-backend
RUN chown -R 33:33 /var/log/tas-backend

USER 33:33

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]