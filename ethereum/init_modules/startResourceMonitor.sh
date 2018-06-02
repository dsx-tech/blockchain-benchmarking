#!/usr/bin/env bash
nohup java -Xms64m -Xmx128m -jar resource-monitor.jar >/dev/null 2>resource-monitor-stdout.log &