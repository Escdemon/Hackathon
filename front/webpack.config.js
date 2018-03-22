const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const fs = require('fs');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
var hash = process.argv.indexOf('--hash') !== -1;

const PATHS = {
  src: path.join(__dirname, '/src'),
  dist: path.join(__dirname,'/dist'),
  nodeModule: path.join(__dirname, './node_modules'),
  angularI18n: path.join(__dirname, './node_modules/angular-i18n')
};

var config = {
  context: PATHS.src,
  entry: {
    framework: ['./entry-framework.js', 'angular-i18n/angular-locale_fr-fr.js'],
    app: './entry.js'
  },
  output: {
    path: PATHS.dist
  },
  devServer : {
    // To use CopyWebpackPlugin
    outputPath: PATHS.dist
  },
  module: {
    loaders: [
      {
        test: /\.template\.html$/,
        loader: 'html'
      }, {
        test: /\.(less|css)$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader!less-loader')
      }, {
        test: /\.(jpg|png)$/,
        loader: 'file'
      }, {
        test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file'
      }, {
        test: /\.(woff|woff2)(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?prefix=font/&limit=5000'
      }, {
        test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'url?limit=10000&mimetype=application/octet-stream'
      }, {
        test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
        loader: 'file'
      }, {
        test: /\.json$/,
        loader: 'json'
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_ENV': JSON.stringify('production')
      }
    }),
    new HtmlWebpackPlugin({
      template: 'index.html',
      inject: 'body',
      favicon: 'img/favicon.png'
    }),
    new CopyWebpackPlugin([
      {
        from: path.join(PATHS.angularI18n, 'angular-locale_fr-fr.js'),
        to: path.resolve(__dirname, PATHS.dist + '/i18n')
      }
    ])
  ]
};

if (hash) {
  config.output.filename = '[name]-[chunkhash:6].js';
  config.plugins.push(new ExtractTextPlugin('[name]-[chunkhash:6].css'));
} else {
  config.output.filename = '[name].js';
  config.plugins.push(new ExtractTextPlugin('[name].css'));
}

module.exports = config;
