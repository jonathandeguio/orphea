{{/*
Expand the name of the chart.
*/}}
{{- define "bosler-gke.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "bosler-gke.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "bosler-gke.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "bosler-gke.labels" -}}
helm.sh/chart: {{ include "bosler-gke.chart" . }}
{{ include "bosler-gke.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "bosler-gke.selectorLabels" -}}
app.kubernetes.io/name: {{ include "bosler-gke.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "bosler-gke.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "bosler-gke.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create repo path name
*/}}
{{- define "bosler-gke.repo_path" -}}
{{- printf "bosler.io/" }}
{{- end }}

{{/*
Create allowd origins
*/}}
{{- define "bosler-gke.allowedOrigins" -}}
{{- printf "http://127.0.0.1:5500,http://127.0.0.1:3000,http://localhost:3000,http://localhost:8080,https://dev.bosler.io,https://%s" .Values.fqdn }}
{{- end }}

{{/*
Create baseUrl
*/}}
{{- define "bosler-gke.baseUrl" -}}
{{- printf "https://%s" .Values.fqdn }}
{{- end }}

{{/*
Create gsBucket
*/}}
{{- define "bosler-gke.gsBucket" -}}
{{- printf "datasets_collections_%s" .Values.projectId }}
{{- end }}