#!/bin/bash

#if [[ ! -v NOTEBOOK_DIR ]]; then
#	NOTEBOOK_DIR=/notebooks
#fi
#
#if [ ! -d "${NOTEBOOK_DIR}" ]; then
#	NOTEBOOK_DIR=/notebooks
#fi

CMD="jupyter lab --allow-root --ip=0.0.0.0 --no-browser --notebook-dir=$NOTEBOOK_DIR"

if [[ -v FRACTAL_TEMPLATES_TOKEN ]]; then
	cd /opt/ || exit
	git clone https://"${FRACTAL_TEMPLATES_TOKEN}"@github.com/Orphea-io/boson >>/dev/null
	cd /opt/boson/funnel/ || exit
	git pull >>/dev/null
	pip3 install -q /opt/boson/funnel/
	cd "${OLDPWD}" || exit
fi
# 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

if [[ -v PASSWORD ]]; then
	PASSWORD=$(python -c "import IPython; print(IPython.lib.security.passwd('$PASSWORD'))")
	CMD="$CMD --NotebookApp.token='' --NotebookApp.password='${PASSWORD}'"
fi

if [[ -v NOTEBOOK_TOKEN ]]; then
  CMD="$CMD --NotebookApp.token='${NOTEBOOK_TOKEN}' --NotebookApp.password=''"
fi

if [[ -v ORPHEA_USERID ]]; then
  CMD="$CMD --NotebookApp.base_url='/api/jupyter/${ORPHEA_USERID}' --NotebookApp.default_url='/api/jupyter/${ORPHEA_USERID}'/disable"
else
  CMD="$CMD --NotebookApp.base_url='/api/jupyter' --NotebookApp.default_url='/api/jupyter/disable"
fi

if [[ -v NOTEBOOK_DIR ]]; then
  if [ ! -f "${NOTEBOOK_DIR}" ]; then
    mkdir -p "${NOTEBOOK_DIR}"
  fi
fi

if [[ -v GIT_URL ]]; then
	git clone "$GIT_URL" "${NOTEBOOK_DIR}"
fi

if [ -f "${NOTEBOOK_DIR}"/packages.txt ]; then
	echo "INFO: Found packages.txt file in folder ${NOTEBOOK_DIR}. Executing it to install apt packages."
	apt-get -qq update
	cat "${NOTEBOOK_DIR}"/packages.txt | grep -v ^# | xargs apt-get install -qq -y
else
	echo "INFO: packages.txt not found in folder ${NOTEBOOK_DIR} --> Continuing"
fi

if [ -f "${NOTEBOOK_DIR}"/requirements.txt ]; then
	echo "INFO: Found requirements.txt file in folder ${NOTEBOOK_DIR} . Installing via \"pip install -r requirements.txt\""
	pip install -q -r "${NOTEBOOK_DIR}"/requirements.txt
else
	echo "INFO: requirements.txt not found in folder ${NOTEBOOK_DIR} --> Continuing"
fi

if [ -f "${NOTEBOOK_DIR}"/extensions.txt ]; then
	echo "INFO: Found extensions.txt file in folder ${NOTEBOOK_DIR} . Installing via \"jupyter extension install --user\""
	cat "${NOTEBOOK_DIR}"/extensions.txt | grep -v ^# | xargs -I {} jupyter {} install --user
else
	echo "INFO: extensions.txt not found in folder ${NOTEBOOK_DIR} --> Continuing"
fi

#echo
#echo "Installed software:"
#python --version
#pip --version
#jupyter --version
#echo "Node $(node --version)"
#echo "NPM $(npm -v)"
#
#echo
#echo "Installed Python packages:"
#pip list -l
#
#echo
#echo "Installed Juypter extensions"
#jupyter labextension list

# Orphea Specific
if [[ -v BACKING_FS ]]; then
  if [ "${BACKING_FS}" == "gs" ]; then
    echo "${GOOGLE_CLOUD_CREDENTIALS}" >>/root/google_creds.json
  fi
fi

# stop jupyter before token expires
curl -s -X 'POST' \
      "$ORPHEA_API/api/fractal/stopJupyter" \
      -H 'accept: */*' \
      -H "Authorization: Bearer $NOTEBOOK_TOKEN" \
      -H 'Content-Type: application/json' \
      -d '{ "buildStatus":"success" }'

curl -X "GET" \
  "{$ORPHEA_API}/api/fractal/stopJupyter" \
  -H "accept: */*" \
  -H "Authorization: Bearer ${NOTEBOOK_TOKEN}"

echo
echo $CMD
exec $CMD "$@"
