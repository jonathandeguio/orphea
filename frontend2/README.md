# **Frontend for MoveToData**

## **This project is FrontEnd for MoveToData.io**

### **How to get started ?**

1. Clone this repository on your local system <br>
   `https://github.com/MoveToData-io/frontend.git`

2. Then install Node Modules <br>
   `yarn install `
3. If dependency conflicts arises then  <br>
   ` yarn install --legacy peer-deeps`
4. After sucesssfull installation of dependency install the Node Modules again <br>
   `yarn install`
5. If you get heap out of memory error run following command in terminal <br>
   for windows <br>
   `set NODE_OPTIONS=--max_old_space_size=4096` <br>
   for linux/macOS <br>
   `export NODE_OPTIONS=--max_old_space_size=4096` <br>
6. Start the application <br>
`yarn start`
7. If you want to use local backend set then set **API VARIABLES**
<hr>

Remove un-used imports and variables

` npx eslint ./src --fix`


Do not upgrade below, it breaks lsp in code editor

"@codingame/monaco-jsonrpc": "^0.3.1",
