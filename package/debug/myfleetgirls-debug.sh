#!/bin/sh

cd `dirname $0`
java -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog \
    -Dorg.apache.commons.logging.simplelog.showdatetime=true \
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG \
    -Dorg.apache.commons.logging.simplelog.log.org.apache.http.wire=ERROR \
    -jar MyFleetGirls.jar
