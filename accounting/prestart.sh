#!/bin/bash
set -e

python setup.py
rm setup.py
export FLAG='nice try'
rm prestart.sh
