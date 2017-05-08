#!/usr/bin/env bash
> instances
for instance in $(aws ec2 describe-instances   --query "Reservations[*].Instances[*].PublicIpAddress"   --output=text); do
    echo "$instance" >> instances
done