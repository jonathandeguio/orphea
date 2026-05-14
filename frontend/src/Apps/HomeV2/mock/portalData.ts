// TODO: brancher sur l'API réelle quand les endpoints seront disponibles

export interface StatItem {
  value: number;
  delta: string;
  trend: "up" | "down";
}

export interface ActivityItem {
  id: number;
  type: "success" | "info" | "warning" | "error";
  text: string;
  time: string;
}

export interface PipelineItem {
  id: string;
  name: string;
  status: "ok" | "running" | "error";
  lastRun: string;
}

export const MOCK_STATS: Record<string, StatItem> = {
  datasets:  { value: 1248, delta: "+34 cette semaine", trend: "up"   },
  pipelines: { value: 87,   delta: "+5 ce mois",        trend: "up"   },
  jobs:      { value: 3904, delta: "+12% vs hier",       trend: "up"   },
  alerts:    { value: 3,    delta: "+2 depuis hier",     trend: "down" },
};

export const MOCK_ACTIVITY: ActivityItem[] = [
  { id: 1, type: "success", text: "Pipeline orders_daily exécuté — 2.3M lignes traitées",  time: "Il y a 4 min"  },
  { id: 2, type: "info",    text: "Dataset crm_contacts documenté par Marie Dupont",        time: "Il y a 18 min" },
  { id: 3, type: "warning", text: "Alerte qualité sur sales_raw — taux de nulls > seuil",  time: "Il y a 42 min" },
  { id: 4, type: "info",    text: "Connecteur Snowflake configuré par l'admin",             time: "Il y a 1h"     },
  { id: 5, type: "error",   text: "Pipeline marketing_sync échoué — timeout connexion",    time: "Il y a 2h"     },
];

export const MOCK_PIPELINES: PipelineItem[] = [
  { id: "p1", name: "orders_daily",       status: "ok",      lastRun: "Il y a 2 min" },
  { id: "p2", name: "user_events_stream", status: "running", lastRun: "En cours"     },
  { id: "p3", name: "crm_sync",           status: "ok",      lastRun: "Il y a 1h"    },
  { id: "p4", name: "marketing_sync",     status: "error",   lastRun: "Il y a 2h"    },
  { id: "p5", name: "datalake_export",    status: "running", lastRun: "En cours"     },
  { id: "p6", name: "finance_monthly",    status: "ok",      lastRun: "Il y a 3h"    },
];
