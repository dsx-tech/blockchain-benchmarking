#!/usr/bin/env bash
sudo nohup java -jar network-manager.jar $1 >network-manager-stdout.log 2>network-manager-stderr.log &