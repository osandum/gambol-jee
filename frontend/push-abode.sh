#!/bin/sh -x

mvn clean install -DskipTests

podman build --format docker -t gambol-wip --rm --squash .

podman push gambol-wip sandum.net:5000/osa/gambol:wf27

# kubectl -n osa patch deployment gambol -p   "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
