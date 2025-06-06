import ReactPlayer from "react-player"

# Treemap

## **Treemap Chart**
> A Treemap Chart est une visualisation hiérarchique qui affiche des données sous forme de rectangles imbriqués. Chaque rectangle représente une catégorie et sa taille est proportionnelle à la valeur qu'il représente. Ce type de graphique est utile pour visualiser de grandes quantités de données hiérarchiques et comprendre les proportions et les relations entre les différentes catégories. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un Treemap Chart dans notre application et vous fournira des instructions pour en créer un efficacement.

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

### **Créer un treemap chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-treemapChart.mp4" width="100%" />
   </div>
