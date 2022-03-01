const { TsConfigPathsPlugin } = require('awesome-typescript-loader');

module.exports = {
  target: 'web',
  entry: {
    main: './src',
  },
  output: {
    path: utils.root(`dist`),
    filename: '[name].[hash].bundle.js',
    chunkFilename: '[name].[hash].chunk.js',
  },
  module: {
    rules: [
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader"]
      },
      {
        test: /\.tsx?$/,
        loader: 'awesome-typescript-loader',
        options: {
          transpileOnly: true,
        },
      },
      {
        enforce: 'pre',
        test: /\.js$/,
        loader: 'source-map-loader',
      },
      { test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "url-loader?limit=10000&mimetype=application/font-woff" },
      { test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/, loader: "file-loader" }
    ],
  },
  resolve: {
    modules: ['node_modules', 'src'],
    extensions: ['*', '.ts', '.tsx', '.js', '.json'],
    plugins: [new TsConfigPathsPlugin()],
    alias: {
      'react': 'preact/compat',
      'react-dom': 'react-dom',
    },
  },
  performance: { hints: false },
  optimization: {
    splitChunks: {
      cacheGroups: {
        commons: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
        },
      },
    },
  }
};
