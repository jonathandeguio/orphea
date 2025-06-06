import ReactPlayer from "react-player"

# Pie

## **Pie Chart**
> Un graphique **Pie Chart** est un graphique statistique circulaire utilisé pour représenter des proportions numériques. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un graphique circulaire dans notre application et fournira des instructions sur la manière de le créer efficacement.

### **Opérations disponibles dans les graphiques**

> **Size By:** 
>> L'option **Size By** vous permet de déterminer la taille de chaque segment du graphique circulaire en fonction de divers paramètres. Vous pouvez choisir parmi les paramètres suivants :
      - Symbol: Représente différentes catégories ou éléments.
      - Series: Représente différentes séries de données.
      - Open: La valeur d'ouverture des points de données.
      - High: La valeur maximale parmi les points de données.
      - Low: La valeur minimale parmi les points de données.
      - Close: La valeur de clôture des points de données.
      - Volume: Le volume des transactions.

> **Aggregate:** 
>> L'option **Aggregate** propose plusieurs méthodes pour résumer les données. Les méthodes disponibles incluent :
      - Count: Nombre total de lignes.
      - Distinct: Nombre de valeurs distinctes/uniques.
      - Average: Moyenne de toutes les valeurs.
      - Sum: Somme de toutes les valeurs.
      - Min: Valeur minimale.
      - Max: Valeur maximale.
      - Variance: Variance d'échantillon des valeurs.
      - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.

> **Group By:** 
>> L'option **Group By** vous permet de regrouper les données selon des paramètres spécifiques. Cela peut aider à organiser les données en segments significatifs. *Vous pouvez regrouper par* :
      - Symbol
      - Series
      - Open
      - High
      - Low
      - Close
      - Volume

### **Créer un Pie Chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-pieChart.mp4" width="100%" />
   </div>
