import ReactPlayer from "react-player"

# Map

## **Map Chart**
> A Map Chart est une visualisation géographique qui affiche des données sur une carte, représentant différents points de données ou zones. Ce type de graphique est idéal pour visualiser les distributions spatiales, les motifs et les tendances à travers différentes régions. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un Map Chart dans notre application et vous fournira des instructions pour en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Geographic Levels:** L'option **Geographic Levels** vous permet de configurer les différents niveaux de géographie affichés sur le Map Chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Country Level**: Définissez le pays ou la région de niveau supérieur.   
**Region/Sub-Region Configuration**: Personnalisez l'apparence et les étiquettes de chaque niveau géographique, y compris :
        - State/Province: Le niveau suivant de la hiérarchie géographique.
        - City/Town: Un niveau plus granulaire au sein de la région sélectionnée.

> **Data Points:** L'option **Data Points** vous permet de configurer la manière dont les données sont représentées sur la carte. Vous pouvez choisir parmi les paramètres suivants :
>> **Metrics**: Sélectionnez les champs de données ou les indicateurs à représenter sur la carte, tels que Population, Sales, ou Crime Rates.   
**Aggregation Method**: Fournit des méthodes pour résumer les données, y compris :
        - None: Affiche les données brutes.
        - Sum: Somme totale de l'indicateur sélectionné.
        - Average: Valeur moyenne de l'indicateur sélectionné.
        - Count: Nombre total des points de données.

> **Labels and Colors:** L'option **Labels and Colors** vous permet de personnaliser les étiquettes et les couleurs des barres dans le waterfall chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur le graphique, telles qu'au-dessus ou en dessous de chaque barre.   
**Color Scheme**: Choisissez ou personnalisez le schéma de couleurs utilisé pour les différents types de barres, telles que les changements positifs ou négatifs.

### **Créer un map chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-mapChart.mp4" width="100%" />
   </div>
