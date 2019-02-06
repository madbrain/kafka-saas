#!/usr/bin/env python3

import sys
import json
import requests

username = "admin"
password = "secret"
baseurl = "http://localhost:9000"

if len(sys.argv) > 2 and sys.argv[1] == "get":
    r = requests.get('%s/api/v1/%s/' % (baseurl, sys.argv[2]), auth=(username, password))
    print(json.dumps(r.json(), indent=4, sort_keys=True))

elif len(sys.argv) > 2 and sys.argv[1] == "create":
    content = open(sys.argv[2], 'r').read()
    parsed = json.loads(content)
    namespace = None
    if "metadata" in parsed and "namespace" in parsed["metadata"]:
        namespace = parsed["metadata"]["namespace"]
    if namespace:
        url = '%s/api/v1/namespaces/%s/%ss' % (baseurl, namespace, parsed["kind"].lower())
    else:
        url = '%s/api/v1/%ss' % (baseurl, parsed["kind"].lower())
    r = requests.post(url, auth=(username, password), headers={ 'Content-Type': 'application/json' }, data=content)
    if r.status_code not in [ requests.codes.ok, requests.codes.created ]:
        print(json.dumps(r.json(), indent=4, sort_keys=True))
else:
    print ("usage: apictl.py get <path> | create <file>")