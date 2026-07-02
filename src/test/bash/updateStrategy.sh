#!/bin/sh
#
# 1) Update parent and child fields with one PUT to parent
#
# 2) When parent receives a PUT and the JSON child collection data has fewer members that it did on the last GET, 
# those members removed from the collection should be deleted from DB with one PUT to the parent
#
#
curl -i -X PUT -H "Content-Type:application/json" \
-d '{"strategyId":"'"$1"'", "name":"The Best Strategy Everesttttt","companies":[{"companyName":"GOOGLE5","code":"A3333333"}],"startDate":"2525-12-19","endDate":"3636-06-13","type":"manauldddddd"}' \
http://localhost:8087/ambis/services/v1.0/strategy/update

#curl -i -X PUT -H "Content-Type:application/json" \
#-d '{"strategyId":"'"$1"'", "name":"The Best Strategy Everesttttt","companies":[{"companyName":"GOOGLE4","code":"A3333333"},{"companyName":"YAHOO4","code":"NUNU3"}],"startDate":"2525-12-19","endDate":"3636-06-13","type":"manauldddddd"}' \
#http://localhost:8087/ambis/services/v1.0/strategy/update