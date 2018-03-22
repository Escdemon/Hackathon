//jshint strict: false
module.exports = function(config) {
  'use strict';
  config.set({
    frameworks: ['jasmine'],

    reporters: ['progress','coverage'],

    files: ['src/tests.webpack.js'],

    preprocessors: {
      'src/tests.webpack.js': ['webpack']
    },

    browsers: ['Chrome'],

    singleRun: true,

    webpack: require('./webpack.config.js'),

    webpackMiddleware: {
      noInfo: 'errors-only'
    }
  });
};
