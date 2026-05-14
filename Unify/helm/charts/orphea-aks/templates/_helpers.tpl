{{/*
Expand the name of the chart.
*/}}
{{- define "bosler-aks.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "bosler-aks.fullname" -}}
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
{{- define "bosler-aks.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "bosler-aks.labels" -}}
helm.sh/chart: {{ include "bosler-aks.chart" . }}
{{ include "bosler-aks.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "bosler-aks.selectorLabels" -}}
app.kubernetes.io/name: {{ include "bosler-aks.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "bosler-aks.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "bosler-aks.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create repo path name
*/}}
{{- define "bosler-aks.repo_path" -}}
{{- printf "%s.azurecr.io/" .Values.containerRegistery }}
{{- end }}

{{/*
Create allowd origins
*/}}
{{- define "bosler-aks.allowedOrigins" -}}
{{- printf "http://127.0.0.1:5500,http://127.0.0.1:3000,http://localhost:3000,http://localhost:8080,https://dev.bosler.io,https://%s" .Values.fqdn }}
{{- end }}

{{/*
Create baseUrl
*/}}
{{- define "bosler-aks.baseUrl" -}}
{{- printf "https://%s" .Values.fqdn }}
{{- end }}
