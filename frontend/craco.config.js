// @ts-ignore
// const { removeConsolePlugin } = require('babel-plugin-transform-remove-console');


module.exports = {
	webpack: {
		alias: {
			vscode: "@codingame/monaco-languageclient/lib/vscode-compatibility",
		},
	},
	// babel: {
	// 	"plugins": process.env.NODE_ENV === 'production' ? ["transform-remove-console"] : []
	//   },
};
