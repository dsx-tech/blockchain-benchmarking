#!/usr/bin/env bash
nohup java -Xms2m -Xmx32m -jar resource-monitor.jar >/dev/null 2>resource-monitor-stdout.log &