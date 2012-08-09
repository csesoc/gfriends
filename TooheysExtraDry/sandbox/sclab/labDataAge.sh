#!/usr/local/bin/bash

LAB_NAME=$1

LAB_DATA_FILE="/home/stevec/TooheysExtraDry/lab_data/$LAB_NAME.lab"

CUR_TIME=`date +%s`
LAB_DATA_LAST_MOD_TIME=`stat -c %Y $LAB_DATA_FILE`

LAB_DATA_FILE_AGE=`expr $CUR_TIME - $LAB_DATA_LAST_MOD_TIME`

echo "$LAB_DATA_FILE_AGE"
exit 0;

