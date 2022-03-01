const webpack = require('webpack');
const webpackMerge = require('webpack-merge');

const HTMLWebpackPlugin = require('html-webpack-plugin');
const config = require('./webpack.common');

module.exports = webpackMerge(config, {
  cache: true,
  mode: 'development',
  entry: {
    Process_Assignment_Dev: './test/app/App',
    Process_Assignment: './src/app/Process_Assignment',
  },
  devtool: 'cheap-eval-source-map',
  resolve: {
    alias: { 'react-dom': '@hot-loader/react-dom' },
  },
  devServer: {
    inline: true,
    contentBase: utils.root('target/classes/static/'),
    compress: false,
    host: '0.0.0.0',
    overlay: false,
    port: 9071,
    clientLogLevel: 'none',
    disableHostCheck: true,
    historyApiFallback: true,
    proxy: [
      {
        context: ['/'],
        target: `http://localhost:9001`,
        secure: false,
        changeOrigin: true,
        onProxyReq: relayRequestHeaders,
        onProxyRes: relayResponseHeaders,
      },
    ],
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept',
    },
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': '"development"',
    }),
    new HTMLWebpackPlugin({
      inject: 'head',
      filename: 'index.html',
      template: './src/test/webapp/app/_template.js',
    }),
  ],
});
