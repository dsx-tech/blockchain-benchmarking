#!/usr/bin/env bash
nohup java -jar load-generator.jar ${LOAD_PARAMS} >/dev/null 2>load-generator-stdout.log &