import { defineConfig, Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'
import wasm from 'vite-plugin-wasm'
import topLevelAwait from 'vite-plugin-top-level-await'

export default defineConfig({
  plugins: [
    wasm(),
    topLevelAwait(),
    react(),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      // Alias the problematic polyseg module to avoid WASM issue during build
      '@icr/polyseg-wasm': resolve(__dirname, 'src/utils/polyseg-stub.ts'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
    headers: {
      'Cross-Origin-Opener-Policy': 'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp',
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    target: 'esnext',
    commonjsOptions: {
      include: [/node_modules/],
      transformMixedEsModules: true,
    },
  },
  optimizeDeps: {
    include: [
      '@cornerstonejs/core',
      '@cornerstonejs/tools',
      '@cornerstonejs/dicom-image-loader',
      'dicom-parser',
    ],
    esbuildOptions: {
      target: 'esnext',
    },
  },
  assetsInclude: ['**/*.wasm'],
})
