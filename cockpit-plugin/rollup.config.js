// rollup.config.js
import typescript from '@rollup/plugin-typescript';

const production = !process.env.ROLLUP_WATCH;

export default {
  input: 'src/main/webapp/plugin.ts',
  output: {
    file: 'target/classes/plugin-webapp/piviz-plugin/app/plugin.js',
    format: 'es',
    sourcemap: !production,
  },
  watch: {
    exclude: ['node_modules/**']
  },
  plugins: [
    typescript({
      sourceMap: !production,
      inlineSources: !production,
    })
  ],
};

