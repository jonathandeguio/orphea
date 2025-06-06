import ReactPlayer from "react-player"

# Parameter

## **Parameter Chart**
> Un **Parameter Chart** est un type de graphique utilisé pour visualiser les relations entre divers paramètres ou variables. Il affiche souvent les données de manière à mettre en évidence comment les changements dans un paramètre affectent les autres. Ce type de graphique est utile pour analyser les dépendances, les tendances et les corrélations au sein des ensembles de données. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un Parameter Chart dans notre application et vous fournira des instructions pour en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Parameter Selection:** L'option **Parameter Selection** vous permet de configurer quels paramètres ou variables seront visualisés dans le graphique. Vous pouvez choisir parmi les paramètres suivants :
>> **Independent Variables**: Sélectionnez les principaux paramètres qui influencent les changements dans le graphique (par exemple, le temps, la température).   
**Dependent Variables**: Choisissez les paramètres qui réagissent aux changements dans les variables indépendantes (par exemple, les ventes, la performance).

> **Axis Configuration:** L'option **Axis Configuration** vous permet de personnaliser les axes du graphique en fonction des paramètres sélectionnés. Vous pouvez choisir parmi les paramètres suivants :
>> **X-Axis Configuration**: Définissez le paramètre affiché sur l'axe des X, y compris les options pour l'échelle (linéaire, logarithmique) et la personnalisation des étiquettes.

> **Chart Types:** L'option **Chart Types** vous permet de sélectionner le type spécifique de graphique à paramètres pour visualiser vos données. Vous pouvez choisir parmi les types de graphiques suivants :
>> **Line Chart**: Affichez la relation entre les paramètres sous forme de graphique linéaire.   
**Scatter Plot**: Visualisez la corrélation entre deux variables avec des points de données individuels.   
**Bubble Chart**: Représentez les points de données avec des tailles variables en fonction d'un paramètre supplémentaire.

> **Labels and Colors:** L'option **Labels and Colors** vous permet de personnaliser les étiquettes et les couleurs des barres dans le waterfall chart. Vous pouvez configurer :
>> **Label Position**: Définissez où les étiquettes sont positionnées sur le graphique, telles qu'au-dessus ou en dessous de chaque barre.   
**Color Scheme**: Choisissez ou personnalisez le schéma de couleurs utilisé pour les différents types de barres, telles que les changements positifs ou négatifs.

### **Créer un parameter chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-parameterChart.mp4" width="100%" />
   </div>
