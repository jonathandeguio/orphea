import ReactPlayer from "react-player"

# Sunburst

## **Sunburst Chart**
> Un **Sunburst Chart** est une visualisation hiérarchique qui affiche les données sous forme de séries d'anneaux concentriques. Chaque anneau représente un niveau dans la hiérarchie, le centre représentant le niveau racine et les anneaux extérieurs représentant les sous-niveaux. Ce type de graphique est utile pour visualiser des données hiérarchiques et comprendre les relations entre les différents niveaux de la hiérarchie. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un sunburst chart dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Hierarchy Levels:** L'option **Hierarchy Levels** vous permet de configurer les différents niveaux de la hiérarchie affichés dans le sunburst chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Root Level**: Définissez le niveau racine ou le niveau de départ de la hiérarchie.
**Sub-Level Configuration**: Personnalisez l'apparence et les étiquettes de chaque sous-niveau, y compris :
        - Level 1: Premier niveau de la hiérarchie.
        - Level 2: Deuxième niveau de la hiérarchie, et ainsi de suite.

> **Segment Values:** L'option **Segment Values** vous permet de configurer les données affichées dans chaque segment du sunburst chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Values**: Sélectionnez les champs de données ou les métriques à afficher dans chaque segment, telles que les Ventes, le Revenu ou le Nombre.   
>> **Aggregate**: Fournit des méthodes pour résumer les données, y compris:
        - Count: Nombre total de lignes.
        - Distinct: Nombre de valeurs distinctes/uniques.
        - Average: Moyenne de toutes les valeurs.
        - Sum: Somme de toutes les valeurs.
        - Min: Valeur minimale.
        - Max: Valeur maximale.
        - Variance: Variance d'échantillon des valeurs.
        - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.

> **Labels and Colors:** L'option **Labels and Colors** vous permet de personnaliser les étiquettes et les couleurs des segments dans le sunburst chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur le graphique, telles qu'à l'intérieur ou à l'extérieur de chaque segment.   
**Color Scheme**: Choisissez ou personnalisez le schéma de couleurs utilisé pour les différents segments et niveaux.

### **Créer un sunburst chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-sunburstChart.mp4" width="100%" />
   </div>
