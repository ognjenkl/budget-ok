import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const apiUrl = 'http://localhost:8090';

export default defineConfig({
  plugins: [react()],
  envPrefix: 'VITE_',
  server: {
    proxy: {
      '/api': {
        target: apiUrl,
        changeOrigin: true,
        secure: false
      }
    }
  }
})
