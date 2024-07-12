#!/bin/bash

sbt "run -Drun.mode=Dev -Dhttp.port=6700 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes $*"