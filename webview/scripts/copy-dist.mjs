import { copyFile, mkdir, readdir, rm } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const webviewDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(webviewDir, '..', '..');
const distDir = path.join(rootDir, 'webview', 'dist');
const resourceDir = path.join(rootDir, 'src', 'main', 'resources', 'webview');

await rm(resourceDir, { recursive: true, force: true });
await mkdir(resourceDir, { recursive: true });

async function copyRecursive(from, to) {
  await mkdir(to, { recursive: true });
  const entries = await readdir(from, { withFileTypes: true });
  for (const entry of entries) {
    const source = path.join(from, entry.name);
    const target = path.join(to, entry.name);
    if (entry.isDirectory()) {
      await copyRecursive(source, target);
    } else {
      await copyFile(source, target);
    }
  }
}

await copyRecursive(distDir, resourceDir);

