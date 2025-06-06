// craco.config.js

module.exports = {
	webpack: {
		alias: {
			vscode: "@codingame/monaco-languageclient/lib/vscode-compatibility",
		},
	},
	babel: {
		plugins: [
			...(process.env.NODE_ENV === 'production'
				? [["transform-remove-console", { "exclude": ["error", "warn"] }]]
				: []),
		],
	},
};
