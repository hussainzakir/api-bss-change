#!/bin/bash

function jsonValue() {
	KEY=$1
	awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"'
}

if [ "$#" -ne 3 ]; then
    echo "Usage: ./refresh.bash <BIB|CAB|STAGER|LR|VAL|PROD> realmYearId companyCode"
    exit
fi
env=$1

if [ -z "$env" ]
then
   echo -e "INVAID ENV"
   exit 
fi

if [ $env == "BIB" ]
then
  SERVER_URL="https://trinetbib.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microbib.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "CAB" ]
then
  SERVER_URL="https://trinetcab.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microcab.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "STAGER" ]
then
  SERVER_URL="https://trinetsr.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microsr.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "LR" ]
then
  SERVER_URL="https://trinetlr.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microlr.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "VAL" ]
then
  SERVER_URL="https://trinetval.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microval.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi



if [ $env == "PROD" ]
then
  SERVER_URL="https://services.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://services.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

TOKEN_DETAILS=$(curl -X POST -H "Content-Type: application/json" -d@signon.json $SIGNON_URL)
SESSION_TOKEN=$(echo "$TOKEN_DETAILS" | jsonValue sessionToken)

EMPLID=00001437489
REALM_YEAR_ID=$2
COMPANY_CODE=$3

echo
echo "Initiating..."
echo "refreshCompanyMidYear( $2, $3 )"

if [ $env == "BIB" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieBIB=$SESSION_TOKEN" -X PUT -L "https://trinetbib.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"
fi

if [ $env == "CAB" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieCAB=$SESSION_TOKEN" -X PUT -L "https://trinetcab.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"
fi


if [ $env == "STAGER" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieSR=$SESSION_TOKEN" -X PUT -L "https://trinetsr.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"
fi

if [ $env == "LR" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieLR=$SESSION_TOKEN" -X PUT -L "https://trinetlr.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"
fi

if [ $env == "VAL" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieVAL=$SESSION_TOKEN" -X PUT -L "https://trinetval.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"
fi


if [ $env == "PROD" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookie=$SESSION_TOKEN" -X PUT -L "https://trinet.hrpassport.com/api-bss/v1.0/benefits/001/$EMPLID/refresh-company-mid-year/$REALM_YEAR_ID/$COMPANY_CODE"

fi
