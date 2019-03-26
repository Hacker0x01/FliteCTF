#!/bin/bash
set -e

service mysql start &
python setup.py
rm setup.py
rm prestart.sh
