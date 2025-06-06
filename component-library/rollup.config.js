import peerDepsExternal from "rollup-plugin-peer-deps-external";
import typescript from "rollup-plugin-typescript2";
import pkg from "./package.json";

export default {
  input: "src/index.ts",
  output: [
    {
      dir: "build/esm",
      format: "esm",
      sourcemap: true,
      preserveModules: true,
    },
    {
      dir: "build/cjs",
      format: "cjs",
      sourcemap: true,
      preserveModules: true,
    },
  ],
  external: [
    ...Object.keys(pkg.dependencies || {}),
    ...Object.keys(pkg.peerDependencies || {}),
  ],
  plugins: [
    peerDepsExternal(),
    typescript({ tsconfig: "tsconfig.json", useTsconfigDeclarationDir: true }),
  ],
  onwarn: (message) => {
    // fail build if circular dependencies are found
    if (message.code === "CIRCULAR_DEPENDENCY") {
      console.error("Error: \n", message);
      process.exit(-1);
    }
  },
};
