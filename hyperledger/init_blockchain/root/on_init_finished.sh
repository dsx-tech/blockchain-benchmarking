#!/usr/bin/env bash
NEWLINE=$"\n"


body="{${NEWLINE}
    \"jsonrpc\": \"2.0\",${NEWLINE}
    \"method\": \"deploy\",${NEWLINE}
    \"params\": {${NEWLINE}
    \"type\": 1,${NEWLINE}
    \"chaincodeID\":{${NEWLINE}
        \"name\": \"mycc\"${NEWLINE}
    },${NEWLINE}
    \"CtorMsg\": {${NEWLINE}
    \"args\":[\"init\"]${NEWLINE}
    }${NEWLINE}
    },${NEWLINE}
    \"id\": 1${NEWLINE}}"

echo -e ${body} > body

curl -X POST --header "Content-Type: application/json" --header "Accept: application/json" -d "$(cat body)" 0.0.0.0:7050/chaincode > deploy.result
