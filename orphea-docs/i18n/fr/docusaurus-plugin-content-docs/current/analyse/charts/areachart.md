import ReactPlayer from "react-player"

# Graphique en aires

## **Graphique en aires**

> Un **graphique en aires** est une représentation graphique qui affiche des données quantitatives à l'aide d'une série de points de données connectés par des lignes, avec la zone sous la ligne remplie. Cette visualisation est utile pour illustrer les tendances au fil du temps ou d'autres variables continues. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un graphique en aires dans notre application et vous fournira des instructions pour le créer efficacement.

### **Opérations disponibles dans les graphiques**

> **Axe des X** : L'option **Axe des X** vous permet de configurer l'axe horizontal du graphique en aires en fonction de divers paramètres. Vous pouvez choisir parmi les paramètres suivants :
>> **Unité de temps** : Facilite les modifications de dates et d'années, ce qui est crucial pour les données de séries temporelles.  
>> **Tri** : Propose deux options de tri : croissant et décroissant, utiles pour organiser les points de données.

> **Axe des Y** : L'option **Axe des Y** vous permet de configurer l'axe vertical du graphique en aires. Les paramètres disponibles incluent :
>> **Agrégation** : Fournit des méthodes pour résumer les données, y compris :
>>> - Nombre : Nombre total de lignes.
>>> - Distinct : Nombre de valeurs distinctes/uniques.
>>> - Moyenne : Moyenne de toutes les valeurs.
>>> - Somme : Somme de toutes les valeurs.
>>> - Minimum : Valeur minimale.
>>> - Maximum : Valeur maximale.
>>> - Variance : Variance d'échantillon des valeurs.
>>> - Écart-type : Dispersion d'un ensemble de données par rapport à sa moyenne.
>> **Regrouper par** : Permet de regrouper les données selon des paramètres spécifiques, tels que :
>>> - Catégorie, Date, Symbole, etc., pour afficher les tendances à travers ces groupes.

### **Créer un area chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-areaChart.mp4" width="100%" />
   </div>
