import ReactPlayer from "react-player"

# Wordcloud

## **Wordcloud Chart**
> A Wordcloud Chart est une représentation visuelle des données textuelles où la taille de chaque mot indique sa fréquence ou son importance au sein d'un ensemble de données. Ce type de graphique est utile pour identifier rapidement les mots les plus importants dans un texte, ce qui en fait un outil efficace pour l'analyse textuelle et la visualisation des données. Cette documentation vous guidera à travers les options de configuration disponibles pour créer un Wordcloud Chart dans notre application et vous fournira des instructions pour en créer un efficacement.

### **Opérations disponibles dans les graphiques**

> **Text Data Source:** L'option **Text Data** Source vous permet de configurer la source des données textuelles utilisées dans le Wordcloud Chart. Vous pouvez choisir parmi les paramètres suivants :
>> **File Upload**: Importez un fichier texte ou un document à partir duquel les mots seront extraits.   
**Text Input**: Saisissez manuellement du texte ou collez du contenu dans l'application.

> **Word Frequencies:** L'option **Word Frequencies** vous permet de configurer la manière dont les mots sont traités et affichés en fonction de leur fréquence. Vous pouvez choisir parmi les paramètres suivants :
>> **Frequency Calculation**: Sélectionnez comment la fréquence des mots est déterminée, par exemple par comptage exact ou par fréquence des termes-inverse fréquence des documents (TF-IDF).   
**Exclusions and Stop Words**: Spécifiez les mots à exclure du wordcloud, tels que les mots courants. (e.g., "the", "and").

> **Appearance and Colors:** L'option **Appearance and Colors** vous permet de personnaliser le style visuel du wordcloud. Vous pouvez configurer :
>> **Font Styles**: Choisissez ou personnalisez le style de police, la taille et le poids des mots.   
**Color Scheme**: Sélectionnez un schéma de couleurs ou appliquez des couleurs personnalisées aux mots en fonction de leur fréquence ou d'un autre indicateur.   
**Layout**: Définissez la forme et l'orientation du wordcloud, telles que circulaire, rectangulaire ou formes personnalisées.

### **Créer un wordcloud chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-wordcloudChart.mp4" width="100%" />
   </div>
