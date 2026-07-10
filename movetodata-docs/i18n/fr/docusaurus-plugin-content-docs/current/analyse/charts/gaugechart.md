import ReactPlayer from "react-player"

# Gauge

## **Gauge Chart**
> Un **Gauge Chart** est une représentation graphique qui utilise un cadran ou un indicateur pour afficher une seule valeur dans une plage, souvent utilisé pour montrer les progrès vers un objectif ou représenter des indicateurs de performance. Ce type de graphique est utile pour visualiser à quel point une valeur est proche d'un objectif. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un gauge chart dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Gauge Range:** L'option **Gauge Range** vous permet de configurer les plages et les seuils affichés sur le gauge chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Min Value**: Définissez la valeur minimale de l'indicateur.
**Max Value**: Définissez la valeur maximale de l'indicateur.
**Thresholds**: Définissez différentes plages pour indiquer divers niveaux, tels que :
        - Low: Plage indiquant une performance faible.
        - Medium: Plage indiquant une performance moyenne.
        - High: Plage indiquant une performance forte.

> **Display Value:** L'option **Display Value** vous permet de choisir la métrique clé ou la valeur qui sera affichée de manière bien visible sur l'indicateur. Vous pouvez sélectionner parmi les paramètres suivants :
>> **Metric**: Choisissez la valeur spécifique à afficher sur l'indicateur, telle que la Valeur Actuelle, la Valeur Cible ou le Pourcentage Complet.

> **Labels and Indicators:** L'option **Labels and Indicators** vous permet de personnaliser les étiquettes et les indicateurs affichés sur le gauge chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur l'indicateur, telles que à l'intérieur, à l'extérieur ou à des points spécifiques.
**Indicator Style**: Personnalisez l'apparence de l'indicateur ou de l'aiguille, y compris la couleur et l'épaisseur.

### **Créer un gauge chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-gaugeChart.mp4" width="100%" />
   </div>
