import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tsconfigPaths from "vite-tsconfig-paths";
import { transform } from "esbuild";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    {
      name: "jsx-in-js",
      enforce: "pre",
      async transform(code, id) {
        if (
          !id.endsWith(".js") ||
          id.includes("node_modules") ||
          id.startsWith("\0")
        ) {
          return null;
        }

        // Allow JSX syntax in .js modules by pre-compiling with esbuild
        return transform(code, {
          loader: "jsx",
          jsx: "automatic",
          sourcemap: true,
          sourcefile: id,
        });
      },
    },
    react(),
    tsconfigPaths(),
  ],
  // Allow JSX syntax in .js modules during dependency optimization and dev transforms
  optimizeDeps: {
    esbuildOptions: {
      loader: {
        ".js": "jsx",
      },
    },
  },
  esbuild: {
    // Parse all JS/TS modules under src as TSX so JSX works anywhere
    loader: "tsx",
    include: /src\/.*\.[jt]sx?$/,
  },
});
