#!/usr/bin/env bash
nohup java -Xms64m -Xmx128m -jar blockchain-logger.jar ${LOG_PARAMS} >/dev/null 2>blockchain-logger-stdout.log &