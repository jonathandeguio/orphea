import ReactPlayer from "react-player"

# Bar

## **Graphique à barres**

> Un **graphique à barres** est une représentation graphique qui utilise des barres pour montrer des comparaisons entre catégories ou éléments. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un graphique à barres dans notre application et vous fournira des instructions sur la manière de le créer efficacement.

### **Opérations disponibles dans les graphiques**

> **Axe des X :** 
>> L'option **Axe des X** vous permet de configurer l'axe horizontal du graphique à barres en fonction de divers paramètres. Vous pouvez choisir parmi les paramètres suivants :
>>> - Unité de temps : Facilite les modifications de dates et d'années.
>>> - Tri : Propose deux options de tri : croissant et décroissant.

> **Axe des Y :** 
>> L'option **Axe des Y** vous permet de configurer l'axe vertical du graphique à barres. Les paramètres disponibles incluent :
>>> - Agrégation : Fournit des méthodes pour résumer les données, y compris :
>>>> - Nombre : Nombre total de lignes.
>>>> - Distinct : Nombre de valeurs distinctes/uniques.
>>>> - Moyenne : Moyenne de toutes les valeurs.
>>>> - Somme : Somme de toutes les valeurs.
>>>> - Minimum : Valeur minimale.
>>>> - Maximum : Valeur maximale.
>>>> - Variance : Variance d'échantillon des valeurs.
>>>> - Écart-type : Dispersion d'un ensemble de données par rapport à sa moyenne.
>>> - Regrouper par : Permet de regrouper les données selon des paramètres spécifiques similaires à ceux du graphique en secteurs, tels que Symbole, Série, Ouverture, Haut, Bas, Clôture, et Volume.

### **Créer un bar chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-barChart.mp4" width="100%" />
   </div>
