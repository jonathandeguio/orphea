import ReactPlayer from "react-player"

# Radar

## **Radar Chart**
> Un **Radar Chart** est une représentation graphique qui utilise une disposition circulaire pour afficher des données multivariables, chaque axe représentant une variable différente. Ce type de graphique est utile pour comparer plusieurs variables à travers différentes catégories ou entités. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un radar chart dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Axes:** L'option **Axes** vous permet de configurer les axes individuels du radar chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Variables**: Sélectionnez les variables spécifiques à afficher en tant qu'axes, telles que Performance Metrics, Categories, ou Attributes.
**Scale**: Définissez l'échelle de chaque axe, y compris :
        - Min Value: Définissez la valeur minimale pour l'axe.
        - Max Value: Définissez la valeur maximale pour l'axe.
        - Step Size: Définissez l'intervalle entre les valeurs de l'échelle.
>> **Aggregate**: Fournit des méthodes pour résumer les données, y compris:
        - Count: Nombre total de lignes.
        - Distinct: Nombre de valeurs distinctes/uniques.
        - Average: Moyenne de toutes les valeurs.
        - Sum: Somme de toutes les valeurs.
        - Min: Valeur minimale.
        - Max: Valeur maximale.
        - Variance: Variance d'échantillon des valeurs.
        - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.
> **Series:** L'option **Series** vous permet de configurer les séries de données affichées sur le radar chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Data Series**: Sélectionnez les séries de données à afficher, telles que différentes catégories ou groupes.
**Color and Style**: Personnalisez l'apparence de chaque série, y compris la couleur, le style de ligne et le style de marqueur.

> **Labels and Legends:** L'option **Labels and Legends** vous permet de personnaliser les étiquettes et les légendes affichées sur le radar chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur le radar chart, telles que sur les axes ou à l'intérieur du graphique.
**Legend**: Personnalisez la légende pour indiquer ce que représente chaque série, y compris la position et le style.

### **Créer un radar chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-radarChart.mp4" width="100%" />
   </div>
