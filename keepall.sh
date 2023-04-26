#!/bin/sh

find $1 -type d -empty -not -path "./.git/*" -exec touch {}/.gitkeep \;
