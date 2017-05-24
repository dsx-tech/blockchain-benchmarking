#!/usr/bin/env bash
nohup java8 -jar load-generator.jar ${LOAD_PARAMS} >/dev/null 2>load-generator-stdout.log &