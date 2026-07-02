#!/bin/sh
#
#  Create parent and children with one PUT to parent
#
curl -i -X PUT -H "Content-Type:application/json" \
-d '{"name":"The Best Strategy","companies":[{"companyName":"GOOGLE","code":"ABCD"},{"companyName":"YAHOO","code":"NUNUNU"}],"startDate":"2014-12-19","endDate":"2019-06-13","type":"manaul"}' \
http://localhost:8087/ambis/services/v1.0/strategy/create