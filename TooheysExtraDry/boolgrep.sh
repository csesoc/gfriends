#!/usr/local/bin/bash

if [[ `grep stevec ~/.friends` == 0 ]]
then
    echo "yes"
else
    echo "no"
fi

