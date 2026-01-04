import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
    exclude: ['**/node_modules/**', '**/dist/**', 'tests/**'],
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: false,
        bypass: (req) => {
          if (req.method === 'GET') {
            return '/index.html';
          }
        },
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: false,
        bypass: (req) => {
          if (req.method === 'GET') {
            return '/index.html';
          }
        },
      },
    }
  }
})
