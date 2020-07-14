#!/bin/bash

# This script depends on json-minify
# https://www.npmjs.com/package/json-minify

# Copyright (c) DaPorkchop_
# 17/6/2017

file_list=()
while IFS= read -d $'\0' -r file ; do
 file_list=("${file_list[@]}" "$file")
 done < <(find "." -type f -name "*.json" -print0)

# echo "${file_list[@]}"

for i in "${file_list[@]}"
 do
  :
 echo $i
 temp=$( json-minify $i )
 echo $temp > $i
done

echo "Done!"
echo "Minified ${#file_list[@]} files!"
