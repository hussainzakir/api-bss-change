#!/bin/bash

function jsonValue() {
	KEY=$1
	awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"'
}

if [ "$#" -ne 1 ]; then
    echo "Usage: ./invalidate_cache.bash <BIB|CAB|STAGER|LR|VAL|PROD>"
    exit
fi
env=$1

if [ -z "$env" ]
then
   echo -e "INVALID ENV"
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
  SIGNON_URL="https://trinetlte1.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "VAL" ]
then
  SERVER_URL="https://trinetval.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microval.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi

if [ $env == "QEN2" ]
then
  SERVER_URL="https://trinetqen2.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://microqen2.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi


if [ $env == "PROD" ]
then
  SERVER_URL="https://services.hrpassport.com/api-bss/v1.0"
  SIGNON_URL="https://services.hrpassport.com/api-trinet-auth/services/v1.0/authentication/signon/mobile"
fi


TOKEN_DETAILS=$(curl -X POST -H "Content-Type: application/json" -d@signon.json $SIGNON_URL)
SESSION_TOKEN=$(echo "$TOKEN_DETAILS" | jsonValue sessionToken)
echo $SESSION_TOKEN

## Below are the possible values for 
#OBJECT_TYPE = "ALL","PLAN-RATES","BENEFIT-PLANS","OMS-BENEFIT-PLAN-RATES","STRATEGY_DATA","BASIC_COMPANY_DETAILS"
#LEVEL = "REALM-PLAN-YEAR","COMPANY","STRATEGY"(STRATEGY level applicable only for STRATEGY_DATA Object_type)
#Value for REALM-PLAN-YEAR level will be realmPlayYearId
#Value for COMPANY level will be companyCode
#Value for STRATEGY level will be strategy Id

## Change the below parameter to invalidate respective cache objects 
OBJECT_TYPE="ALL"
LEVEL="COMPANY"
VALUE="0014z00001e1gYNAAY"

echo "Initiating.."

if [ $env == "BIB" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieBIB=$SESSION_TOKEN" -X DELETE -L "https://trinetbib.hrpassport.com/api-bss/v1.0/benefits/001/00001437489/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi

if [ $env == "CAB" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieCAB=$SESSION_TOKEN" -X DELETE -L "https://trinetcab.hrpassport.com/api-bss/v1.0/benefits/001/00001437489/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi


if [ $env == "STAGER" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieSR=$SESSION_TOKEN" -X DELETE -L "https://trinetsr.hrpassport.com/api-bss/v1.0/benefits/001/00001437489/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi

if [ $env == "LR" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieLTE1=$SESSION_TOKEN" -X DELETE -L "https://trinetlte1.hrpassport.com/api-bss/v1.0/benefits/001/00002222263/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi

if [ $env == "VAL" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieVAL=$SESSION_TOKEN" -X DELETE -L "https://trinetval.hrpassport.com/api-bss/v1.0/benefits/001/00001437489/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi

if [ $env == "QEN2" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookieQEN2=$SESSION_TOKEN" -X DELETE -L "https://trinetqen2.hrpassport.com/api-bss/v1.0/benefits/001/00002222263/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"
fi

if [ $env == "PROD" ]
then
curl -H "Content-type: application/json" -H "Cookie:TriNetAuthCookie=$SESSION_TOKEN" -X DELETE -L "https://trinet.hrpassport.com/api-bss/v1.0/benefits/001/00002222263/cache/001?objectType=$OBJECT_TYPE&level=$LEVEL&value=$VALUE"

fi
