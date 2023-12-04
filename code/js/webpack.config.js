const ESLintPlugin = require("eslint-webpack-plugin");
module.exports = {
  mode: "development",
  devServer: {
    historyApiFallback: true,
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        router: () => 'http://localhost:8080',
      },
    },
  },
  resolve: {
    extensions: [".js", ".ts", ".tsx"],
  },
  plugins: [
    new ESLintPlugin({
      extensions: ["js", "jsx", "ts", "tsx"],
    }),
  ],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: "ts-loader",
        exclude: /node_modules/,
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      }
    ],
  },
};
