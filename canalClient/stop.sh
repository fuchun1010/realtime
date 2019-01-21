#!/usr/bin/env bash
jps -l | grep "canalClient-1.0-allinone.jar" | awk '{print $1}' | xargs kill
