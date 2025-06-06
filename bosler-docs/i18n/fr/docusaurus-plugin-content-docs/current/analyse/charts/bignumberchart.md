import ReactPlayer from "react-player"

# Big Number

## **Graphique Big Number**

> Un **graphique Big Number** est une représentation graphique qui affiche une seule valeur numérique importante pour mettre en évidence des métriques clés ou des indicateurs de performance (KPI). Ce type de graphique est utile pour montrer des statistiques résumées ou des valeurs significatives d'un seul coup d'œil. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un graphique Big Number dans notre application et vous fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Valeur à afficher :** L'option **Valeur à afficher** vous permet de choisir la métrique clé ou le nombre qui sera affiché de manière bien visible dans le graphique Big Number. Vous pouvez sélectionner parmi les paramètres suivants :
>> **Métrique :** Choisissez la valeur numérique spécifique à afficher, telle que le revenu total, les ventes totales ou le score moyen.   
>> **Méthode d'agrégation :** Fournit des méthodes pour résumer les données, y compris :
>>> - Nombre : Nombre total de lignes.
>>> - Distinct : Nombre de valeurs distinctes/uniques.

> **Filtres :** L'option **Filtres** vous permet d'appliquer des conditions pour affiner les données affichées. Vous pouvez filtrer en fonction de paramètres tels que :
>> **Plage de dates :** Sélectionnez une période spécifique pour afficher les données correspondant à cette plage.   
>> **Catégorie :** Filtrer par catégories pertinentes pour la métrique, telles que les gammes de produits, les régions, etc.

### **Créer un big number chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-bigNumberChart.mp4" width="100%" />
   </div>
