import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      prez: path.resolve(__dirname, 'node_modules/prez/src/index.ts'),
    },
  },
  server: {
    port: 5173,
    open: false,
  },
})
