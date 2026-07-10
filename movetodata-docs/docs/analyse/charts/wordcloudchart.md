import ReactPlayer from "react-player"

# Wordcloud

## **Wordcloud Chart**
> A **Wordcloud Chart** is a visual representation of text data where the size of each word indicates its frequency or importance within a dataset. This chart type is useful for quickly identifying the most prominent words in a body of text, making it an effective tool for text analysis and data visualization. This documentation will guide you through the configuration options available for creating a wordcloud chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Text Data Source:** The **Text Data Source** option allows you to configure the source of the text data used in the wordcloud chart. You can choose from the following parameters:
>> **File Upload**: Upload a text file or document from which the words will be extracted.
**Text Input**: Manually input text or paste content into the application.

> **Word Frequencies:** The **Word Frequencies** option allows you to configure how the words are processed and displayed based on their frequency. You can choose from the following parameters:
>> **Frequency Calculation**: Select how word frequency is determined, such as by exact count or term frequency-inverse document frequency (TF-IDF).
**Exclusions and Stop Words**: Specify words to exclude from the wordcloud, such as common stop words (e.g., "the", "and").

> **Appearance and Colors:** The **Appearance and Colors** option allows you to customize the visual style of the wordcloud. You can configure:
>> **Font Styles**: Choose or customize the font style, size, and weight for the words.
**Color Scheme**: Select a color scheme or apply custom colors to words based on their frequency or another metric.
**Layout**: Define the shape and orientation of the wordcloud, such as circular, rectangular, or custom shapes.

### **Creating a wordcloud chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-wordcloudChart.mp4" width="100%" />
   </div>
