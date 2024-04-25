#!/bin/bash

mvn test -Dbrowser=$BROWSER -Dbrowser.version=$VERSION_BROWSER -Dremote.url=$REMOTE_URL