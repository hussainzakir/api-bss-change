#!/bin/bash

function jsonValue() {
	KEY=$1
	awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"'
}

if [ "$#" -ne 1 ]; then
    echo "Usage: ./bulk_import.bash <QEN2|LR|PROD>"
    exit
fi
env=$1

if [ -z "$env" ]
then
   echo -e "INVAID ENV"
   exit 
fi

if [ $env == "QEN2" ]
then
  SERVER_URL="https://trinetqen2.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microqen2.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "LR" ]
then
  SERVER_URL="https://trinetlr.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microlr.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "PROD" ]
then
  SERVER_URL="https://services.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://services.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi


TOKEN_DETAILS=$(curl -X POST -H "Content-Type: application/json" -d@signon.json $SIGNON_URL)
SESSION_TOKEN=$(echo "$TOKEN_DETAILS" | jsonValue sessionToken)

echo "Initiating.." $SESSION_TOKEN

if [ $env == "QEN2" ]
then
curl -X POST https://trinetqen2.hrpassport.com/api-bs-hw-bss-core/graphql \
  -H "Content-Type: application/json" \
  -H "Cookie:TriNetDCPAuthCookie=$SESSION_TOKEN" \
  -d '{
    "query": "mutation Convert($input: ConvertProspectToClientInput!) { convertProspectToClient(prospectToClientConversionRequest: $input) }",
    "variables": {
      "input": {
        "prospectId": "<PROSPECT_ID>",
        "companyCode": "<COMPANY_CODE>"
      }
    }
  }'
fi

if [ $env == "LR" ]
then
curl -X POST https://trinetlr.hrpassport.com/api-wf-hw-bss-prospect/graphql \
  -H "Content-Type: application/json" \
  -H "Cookie:TriNetDCPAuthCookie=$SESSION_TOKEN" \
  -d '{
    "query": "mutation Convert($input: ConvertProspectToClientInput!) { convertProspectToClient(prospectToClientConversionRequest: $input) }",
    "variables": {
      "input": {
        "prospectId": "<PROSPECT_ID>",
        "companyCode": "<COMPANY_CODE>"
      }
    }
  }'
fi


if [ $env == "PROD" ]
then
curl -X POST https://trinet.hrpassport.com/api-bs-hw-bss-core/graphql \
  -H "Content-Type: application/json" \
  -H "Cookie:TriNetDCPAuthCookie=$SESSION_TOKEN" \
  -d '{
    "query": "mutation Convert($input: ConvertProspectToClientInput!) { convertProspectToClient(prospectToClientConversionRequest: $input) }",
    "variables": {
      "input": {
        "prospectId": "<PROSPECT_ID>",
        "companyCode": "<COMPANY_CODE>"
      }
    }
  }'
fi
