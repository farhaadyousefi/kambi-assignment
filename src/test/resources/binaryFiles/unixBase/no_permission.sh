#!/bin/bash

path=$(realpath "$0")
chmod -x ${path}

ls "$@"