import { instantiate } from './minigameswasmapp.uninstantiated.mjs';

await wasmSetup;

instantiate({ skia: Module['asm'] });
