#!/bin/sh
#
# All children are deleted when parent gets a DELETE
#
curl -i -X DELETE http://localhost:8087/ambis/services/v1.0/strategy/delete/$1