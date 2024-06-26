#!/bin/sh -x

mvn clean install -DskipTests

podman build --format docker -t gambol-wf25 --rm --squash .

podman push gambol-wf25 sandum.net:5000/osa/gambol:wf25-11

# kubectl -n osa patch deployment gambol -p   "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
