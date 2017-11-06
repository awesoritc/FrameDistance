#!/usr/bin/env bash

msortf f=roomId%n i=Route_dynamic.csv |
mcount k=roomId a=count |
mcut f=roomId,count o=count.csv