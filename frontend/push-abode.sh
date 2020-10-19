#!/bin/sh -x

mvn clean install -DskipTests

podman build --format docker -t gambol-wf20 --rm --squash . 

podman push gambol-wf20 sandum.net:5000/osa/gambol:wf20

# kubectl -n osa patch deployment gambol -p   "{\"spec\":{\"template\":{\"metadata\":{\"labels\":{\"date\":\"`date +'%s'`\"}}}}}"
 
