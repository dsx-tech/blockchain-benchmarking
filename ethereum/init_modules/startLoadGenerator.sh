#!/usr/bin/env bash
nohup java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 load-generator.jar ${LOAD_PARAMS} >/dev/null 2>load-generator-stdout.log &