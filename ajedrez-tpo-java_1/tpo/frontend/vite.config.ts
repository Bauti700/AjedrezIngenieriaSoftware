import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // En desarrollo, las llamadas a /api se redirigen al backend Java (ChessHttpServer).
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
