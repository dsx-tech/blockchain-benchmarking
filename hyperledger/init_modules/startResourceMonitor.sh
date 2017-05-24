#!/usr/bin/env bash
nohup java8 -Xms2m -Xmx32m -jar resource-monitor.jar >/dev/null 2>resource-monitor-stdout.log &