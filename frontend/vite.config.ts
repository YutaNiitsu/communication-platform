/// <reference types="vitest" />
import { defineConfig } from 'vitest/config' // ★ vitest/config からインポートする
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import license from 'rollup-plugin-license'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    // ★ as any を挟むことで型エラーを回避
    (license as any)({
      sourcemap: true,
      cwd: process.cwd(),
      thirdParty: {
        output: {
          file: path.join(__dirname, 'dist', 'dependencies-licenses.txt'),
          template(dependencies: any[]) {
            return dependencies.map((dep: any) => {
              return `--------------------------------------------------\n` +
                     `Package: ${dep.name}\n` +
                     `Version: ${dep.version}\n` +
                     `License: ${dep.license}\n` +
                     `Author:  ${dep.author?.name || 'Unknown'}\n` +
                     `--------------------------------------------------\n\n` +
                     `${dep.licenseText || ''}\n\n`;
            }).join('\n');
          },
        },
      },
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  define: {
    global: 'window',
  },
  server: {
    host: true,
    port: 5173
  },
  test: {
    environment: 'jsdom',
  },
})