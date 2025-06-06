import ReactPlayer from "react-player"

# Waterfall

## **Waterfall Chart**
> Un **Waterfall Chart** est une représentation graphique qui montre les changements incrémentiels d'une valeur au fil du temps ou des catégories, mettant en évidence l'effet cumulatif des valeurs positives ou négatives successives. Ce type de graphique est utile pour comprendre la contribution des éléments individuels à une valeur totale. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un waterfall chart dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Categories:** L'option **Categories** vous permet de configurer les différentes catégories affichées dans le waterfall chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Steps**: Définissez les étapes ou les catégories séquentielles à afficher, telles que Time Periods, Phases, ou Stages.
**Sorting**: Définissez comment les catégories sont triées, y compris :
        - Ascending: Trier les catégories par ordre croissant.
        - Descending: Trier les catégories par ordre décroissant.

> **Values:** L'option **Values** vous permet de configurer les valeurs de données affichées dans le waterfall chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Data Fields**: Sélectionnez les champs de données spécifiques à afficher, tels que Revenue, Costs, or Profit.   
>> **Aggregate**: Fournit des méthodes pour résumer les données, y compris:
        - Count: Nombre total de lignes.
        - Distinct: Nombre de valeurs distinctes/uniques.
        - Average: Moyenne de toutes les valeurs.
        - Sum: Somme de toutes les valeurs.
        - Min: Valeur minimale.
        - Max: Valeur maximale.
        - Variance: Variance d'échantillon des valeurs.
        - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.

> **Labels and Colors:** L'option **Labels and Colors** vous permet de personnaliser les étiquettes et les couleurs des barres dans le waterfall chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur le graphique, telles qu'au-dessus ou en dessous de chaque barre.   
**Color Scheme**: Choisissez ou personnalisez le schéma de couleurs utilisé pour les différents types de barres, telles que les changements positifs ou négatifs.

### **Créer un waterfall chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-waterfallChart.mp4" width="100%" />
   </div>
