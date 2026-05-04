import { globalIgnores } from "eslint/config";
import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";
import pluginVue from "eslint-plugin-vue";
import pluginVitest from "@vitest/eslint-plugin";
import pluginOxlint from "eslint-plugin-oxlint";
import skipFormatting from "eslint-config-prettier/flat";

export default defineConfigWithVueTs(
	{
		name: "app/files-to-lint",
		files: ["**/*.{vue,ts,mts,tsx}"],
		rules: {
			"@typescript-eslint/consistent-type-imports": "error",
			"@typescript-eslint/no-unused-vars": "error",

			"@typescript-eslint/typedef": [
				"error",
				{
					parameter: true,
					variableDeclaration: true,
					propertyDeclaration: true,
					memberVariableDeclaration: true,
					arrowParameter: true,
				},
			],
			"@typescript-eslint/explicit-function-return-type": "error",

			"vue/no-unused-vars": "error",
			"vue/no-unused-components": "error",
			"vue/no-use-v-if-with-v-for": "error",
		},
	},

	globalIgnores(["**/dist/**", "**/dist-ssr/**", "**/coverage/**"]),

	...pluginVue.configs["flat/essential"],
	vueTsConfigs.recommended,

	{
		...pluginVitest.configs.recommended,
		files: ["src/**/__tests__/*"],
	},

	...pluginOxlint.buildFromOxlintConfigFile(".oxlintrc.json"),

	skipFormatting,
);
