import ReactPlayer from "react-player"

# Horizontal

## **Horizontal Chart**
> Un **Horizontal Chart** est une représentation graphique où les barres sont affichées horizontalement pour montrer des comparaisons entre des catégories ou des éléments. Ce type de graphique est utile lorsque vous avez des noms de catégories longs ou lorsque vous comparez des catégories avec des valeurs distinctes. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un graphique à barres horizontales dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **X-Axis** : L'option **X-Axis** vous permet de configurer l'axe horizontal du line chart en fonction de divers paramètres. Vous pouvez choisir parmi les paramètres suivants :
>> **Time Unit** : Facilite les modifications de date et d'année, crucial pour les données de séries temporelles.  
**Sort**: Propose deux options de tri : croissant et décroissant, utiles pour organiser les points de données.

> **Y-Axis:**  L'option **Y-Axis** vous permet de configurer l'axe vertical du line chart. Les paramètres disponibles incluent :
>> **Aggregate**: Fournit des méthodes pour résumer les données, y compris:
        - Count: Nombre total de lignes.
        - Distinct: Nombre de valeurs distinctes/uniques.
        - Average: Moyenne de toutes les valeurs.
        - Sum: Somme de toutes les valeurs.
        - Min: Valeur minimale.
        - Max: Valeur maximale.
        - Variance: Variance d'échantillon des valeurs.
        - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.
>> **Group By**: Permet de regrouper les données selon des paramètres spécifiques, tels que :
        - Category, Date, Symbol, etc., pour afficher les tendances à travers ces groupes.

### **Créer un horizontal chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-horizontalChart.mp4" width="100%" />
   </div>
