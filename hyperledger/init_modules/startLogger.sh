#!/usr/bin/env bash
nohup java8 -Xms2m -Xmx64m -jar blockchain-logger.jar ${LOG_PARAMS} >/dev/null 2>blockchain-logger-stdout.log &