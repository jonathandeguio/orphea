import ReactPlayer from "react-player"

# Table

## **Table Chart**
> Un **Table Chart** est une représentation tabulaire des données où chaque cellule affiche une valeur, permettant une comparaison facile et un examen détaillé des points de données à travers différentes dimensions. Ce type de graphique est utile pour afficher des données brutes dans un format structuré. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un table chart dans notre application et fournira des instructions sur la manière d'en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Columns:** L'option Columns vous permet de configurer les champs de données qui seront affichés en tant que colonnes dans le table chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Fields**: Sélectionnez les champs ou métriques spécifiques à afficher en tant que colonnes, tels que Date, Symbol, Open, High, Low, Close, Volume, etc.
**Sorting**: Définissez comment les colonnes sont triées, comprennant :
        - Ascending: Trier les colonnes par ordre croissant.
        - Descending: Trier les colonnes par ordre décroissant.

> **Rows:** L'option **Rows** vous permet de configurer les champs de données qui seront affichés en tant que lignes dans le table chart. Vous pouvez choisir parmi les paramètres suivants :
>> **Fields**: Sélectionnez les champs ou métriques spécifiques à afficher en tant que lignes, de manière similaire aux colonnes.

>> **Pagination**: Gérez le nombre de lignes affichées par page si l'ensemble de données est volumineux, y compris :
        - Page Size: Définissez le nombre de lignes par page.
        - Navigation: Options pour naviguer entre les pages.
>> **Aggregate**: Fournit des méthodes pour résumer les données, y compris:
        - Count: Nombre total de lignes.
        - Distinct: Nombre de valeurs distinctes/uniques.
        - Average: Moyenne de toutes les valeurs.
        - Sum: Somme de toutes les valeurs.
        - Min: Valeur minimale.
        - Max: Valeur maximale.
        - Variance: Variance d'échantillon des valeurs.
        - Standard Deviation: Dispersion d'un ensemble de données par rapport à sa moyenne.
> **Filters:** L'option Filters vous permet d'appliquer des conditions pour affiner les données affichées. Vous pouvez filtrer en fonction de paramètres tels que :   
>> **Date Range**: Sélectionnez une période spécifique pour afficher les données correspondant à cette plage.   
**Category**: Filtrer par catégories pertinentes pour les données, telles que les gammes de produits, les régions, etc.

### **Créer un table chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-tableChart.mp4" width="100%" />
   </div>
