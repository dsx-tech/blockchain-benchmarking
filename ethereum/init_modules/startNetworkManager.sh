#!/usr/bin/env bash
sudo nohup java -Xms64m -Xmx128m -jar network-manager.jar $1 $2 >network-manager-stdout.log 2>network-manager-stderr.log &