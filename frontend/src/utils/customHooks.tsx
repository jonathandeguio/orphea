import store from "redux/store";

const myLanguages = {
  welcomeToBosler: {
    en: "Welcome",
    fr: "Bienvenue",
    de: "Willkommen",
    es: "Bienvenido",
    hi: "बोसलर में आपका स्वागत है",
    nl: "Welkom",
  },
  welcomeToBoslerSubTitle: {
    en: "A smart data platform, central location for storing and managing data from various sources, one place for all data needs including AI and ML.",
    fr: "Bienvenue sur la plateforme centralisée de gestion et de visualisation de vos données.",
    de: "Eine moderne Datenplattform, ein zentraler Ort zum Speichern und Verwalten von Daten aus verschiedenen Quellen, ein Ort für alle Datenanforderungen, einschließlich KI und ML.",
    es: "Una plataforma de datos moderna, ubicación central para almacenar y administrar datos de varias fuentes, un lugar para todas las necesidades de datos, incluidos AI y ML.",
    hi: "एक आधुनिक डेटा प्लेटफ़ॉर्म, विभिन्न स्रोतों से डेटा संग्रहीत करने और प्रबंधित करने के लिए केंद्रीय स्थान, AI और ML सहित सभी डेटा आवश्यकताओं के लिए एक स्थान।",
    nl: "Een smart dataplatform, centrale locatie voor het opslaan en beheren van data uit verschillende bronnen, één plek voor alle databehoeften, inclusief AI en ML.",
  },
  builds: {
    en: "Builds",
    fr: "Builds",
    de: "Baut",
    es: "Construye",
    hi: "बनाता",
    nl: "Bouwt",
  },
};

export type AllLabels = keyof typeof myLanguages;

function getLanguageLabel(label: AllLabels) {
  const state = store.getState();
  const detectBrowserLanguage = require("detect-browser-language");
  let userLanguage = state.userDetails?.user?.preferences?.language;
  if (userLanguage == undefined || userLanguage == null) userLanguage = "en";

  const browserLan = detectBrowserLanguage().substring(0, 2);

  if (myLanguages[label] === undefined) {
    return "!! " + label;
  } else {
    const myLanguageLabel = myLanguages[label] as any;
    if (
      userLanguage == "auto" &&
      detectBrowserLanguage != undefined &&
      myLanguageLabel[browserLan] != undefined
    )
      return myLanguageLabel[browserLan];
    else if (myLanguageLabel[userLanguage] === undefined) {
      return myLanguageLabel["en"]; // return default language if not found
    } else {
      return myLanguageLabel[userLanguage];
    }
  }
}

getLanguageLabel("builds");
