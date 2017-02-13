#!/usr/bin/env bash
set -e

PUBLIC="sample-app/public"
SRC_PATH="$PUBLIC/js/components/"
IMAGES_PATH="../opentok.js-ss-annotation/dist/images"
TEMPLATES_PATH="../opentok.js-ss-annotation/dist/templates/"
CSS_PATH="../opentok.js-ss-annotation/css"
ANNOTATIONS_PATH="../opentok.js-ss-annotation/annotations"
NPM_MODULES="sample-app/node_modules"

function fetchNpmPackages()
{
  echo "Fetching NPM Packages"
	npm i
	echo "Checking for NPM Updates"
	npm update

  copyDependencies
}

function copyDependencies()
{
	echo "Copying NPM Packages"
	cp -v $NPM_MODULES/opentok-accelerator-core/browser/opentok-acc-core.js $SRC_PATH
	cp -v $NPM_MODULES/opentok-annotation/dist/opentok-annotation.js $SRC_PATH
	cp -v $NPM_MODULES/opentok-screen-sharing/dist/opentok-screen-sharing.js $SRC_PATH
	cp -v $NPM_MODULES/opentok-solutions-logging/dist/opentok-solutions-logging.js $SRC_PATH
}

if [[ -d sample-app ]]
then
	cd sample-app
	npm install

	cd ..
	fetchNpmPackages
else
	echo "Please make sure the sample-app folder exist."
	exit 1
fi
